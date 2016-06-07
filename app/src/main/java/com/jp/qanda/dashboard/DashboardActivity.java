package com.jp.qanda.dashboard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.jp.qanda.BaseActivity;
import com.jp.qanda.R;
import com.jp.qanda.fragment.HotQuestionsFragment;
import com.jp.qanda.fragment.MeProfileFragment;
import com.jp.qanda.fragment.UserListFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author jpwang
 * @since 6/2/16
 */
public class DashboardActivity extends BaseActivity {

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.container)
    ViewPager viewPager;

    private FragmentPagerAdapter pagerAdapter;

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
    }
}
