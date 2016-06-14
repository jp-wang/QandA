package com.jp.qanda.splash;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.jp.qanda.BaseActivity;
import com.jp.qanda.BuildConfig;
import com.jp.qanda.R;
import com.jp.qanda.TableConstants;
import com.jp.qanda.dashboard.DashboardActivity;
import com.jp.qanda.util.AudioHandleUtil;
import com.jp.qanda.vo.User;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author jpwang
 * @since 6/1/16
 */
public class SplashActivity extends BaseActivity {
    private static final String UNCHANGED_CONFIG_VALUE = "CHANGE-ME";
    private static final int RC_SIGN_IN = 1000;

    @BindView(android.R.id.content)
    View rootView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ButterKnife.bind(this);

        if (!ImageLoader.getInstance().isInited()) {
            init();
        }

        Observable.just(FirebaseAuth.getInstance().getCurrentUser())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<FirebaseUser>() {
                    @Override
                    public void call(final FirebaseUser firebaseUser) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (firebaseUser == null) {
                            startAuthUI();
                        } else {
                            FirebaseDatabase.getInstance().getReference(TableConstants.TABLE_USERS)
                                    .child(firebaseUser.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            User user = dataSnapshot.getValue(User.class);
                                            if (user == null) {
                                                createOrUpdateUserInfo(firebaseUser);
                                            } else {
                                                startActivity(DashboardActivity.createIntent(SplashActivity.this));
                                                SplashActivity.this.finish();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            showErrorHandling();
                                        }
                                    });
                        }
                    }
                });
    }

    private void init() {
        initImageLoader(this.getApplicationContext());
        AudioHandleUtil.init(20, this.getApplicationContext());

        //Firebase related initialization
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        initFirebaseRemoteConfig();
    }

    private void initImageLoader(Context context) {
        if (ImageLoader.getInstance().isInited()) {
            return;
        }
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        if (BuildConfig.DEBUG) {
            config.writeDebugLogs(); // Remove for release app
        }

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }

    private void initFirebaseRemoteConfig() {
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setConfigSettings(settings);
        remoteConfig.setDefaults(R.xml.remote_config_defaults);
        remoteConfig.fetch(settings.isDeveloperModeEnabled() ? 0L : 43200L).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                remoteConfig.activateFetched();
            }
        });
    }

    private void startAuthUI() {
        ArrayList<String> selectedProviders = new ArrayList<>();
        selectedProviders.add(AuthUI.EMAIL_PROVIDER);
        if (isGoogleConfigured()) {
            selectedProviders.add(AuthUI.GOOGLE_PROVIDER);
        }
        if (isFacebookConfigured()) {
            selectedProviders.add(AuthUI.FACEBOOK_PROVIDER);
        }
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setLogo(R.mipmap.app_icon)
                .setProviders(selectedProviders.toArray(new String[selectedProviders.size()]))
                .build(), RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
        }
    }

    private void handleSignInResponse(int resultCode, Intent data) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (resultCode == RESULT_OK && user != null) {
            createOrUpdateUserInfo(user);
            return;
        }
        showErrorHandling();
    }

    private void showErrorHandling() {
        Snackbar.make(rootView, R.string.sign_in_failed, Snackbar.LENGTH_LONG)
                .setAction(R.string.try_again, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startAuthUI();
                    }
                }).show();
    }

    private void createOrUpdateUserInfo(FirebaseUser user) {
        HashMap<String, Object> updatedUserFields = new HashMap<>();
        updatedUserFields.put("username", user.getDisplayName());
        updatedUserFields.put("email", user.getEmail());
        updatedUserFields.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");

        FirebaseDatabase.getInstance().getReference(TableConstants.TABLE_USERS)
                .child(user.getUid())
                .updateChildren(updatedUserFields)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startActivity(DashboardActivity.createIntent(SplashActivity.this));
                        SplashActivity.this.finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showErrorHandling();
                    }
                });
    }

    private boolean isFacebookConfigured() {
        return !UNCHANGED_CONFIG_VALUE.equals(
                getResources().getString(R.string.facebook_application_id));
    }

    private boolean isGoogleConfigured() {
        return !UNCHANGED_CONFIG_VALUE.equals(
                getResources().getString(R.string.default_web_client_id));
    }
}
