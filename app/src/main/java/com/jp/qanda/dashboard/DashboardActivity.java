package com.jp.qanda.dashboard;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.jp.qanda.BaseActivity;
import com.jp.qanda.R;
import com.jp.qanda.fragment.HotQuestionsFragment;
import com.jp.qanda.fragment.MeProfileFragment;
import com.jp.qanda.fragment.UserListFragment;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author jpwang
 * @since 6/2/16
 */
public class DashboardActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.container)
    ViewPager viewPager;

    private FragmentPagerAdapter pagerAdapter;

    private GoogleApiClient googleApiClient;

    public static Intent createIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, DashboardActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        ButterKnife.bind(this);
        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] fragments = new Fragment[] {
                    new HotQuestionsFragment(),
                    new UserListFragment(),
                    new MeProfileFragment()
            };

            private final String[] fragmentNames = new String[] {
                    "Hot",
                    "People",
                    "Me"
            };

            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public int getCount() {
                return fragments.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return fragmentNames[position];
            }
        };

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)
                .enableAutoManage(this, this)
                .build();

        handleInvitation();
    }

    private void handleInvitation() {
        AppInvite.AppInviteApi.getInvitation(googleApiClient, this, true).setResultCallback(new ResultCallback<AppInviteInvitationResult>() {
            @Override
            public void onResult(@NonNull AppInviteInvitationResult appInviteInvitationResult) {

            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @OnClick(R.id.fab)
    void feedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"neiyo.wang@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback on " + SimpleDateFormat.getInstance().format(new Date()));
        startActivity(intent);
    }
}
