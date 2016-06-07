package com.jp.qanda;

import android.support.v7.app.AppCompatActivity;

import butterknife.OnClick;
import butterknife.Optional;

/**
 * @author jpwang
 * @since 6/6/16
 */
public class BaseActivity extends AppCompatActivity {
    @OnClick(R.id.back)
    @Optional
    void back() {
        super.onBackPressed();
    }

    @OnClick(R.id.share)
    @Optional
    void share() {

    }
}
