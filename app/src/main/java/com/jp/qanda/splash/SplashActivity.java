package com.jp.qanda.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jp.qanda.R;
import com.jp.qanda.TableConstants;
import com.jp.qanda.dashboard.DashboardActivity;
import com.jp.qanda.util.AudioHandleUtil;
import com.jp.qanda.vo.User;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author jpwang
 * @since 6/1/16
 */
public class SplashActivity extends AppCompatActivity {
    private static final String UNCHANGED_CONFIG_VALUE = "CHANGE-ME";
    private static final int RC_SIGN_IN = 1000;

    @BindView(android.R.id.content)
    View rootView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AudioHandleUtil.init(20, this.getApplicationContext());

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
        updatedUserFields.put("photoUrl", user.getPhotoUrl());

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