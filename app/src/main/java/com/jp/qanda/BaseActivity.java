package com.jp.qanda;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.appinvite.AppInviteInvitation;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * @author jpwang
 * @since 6/6/16
 */
public class BaseActivity extends AppCompatActivity {
    private final static int RQ_INVITATION = 999;
    @BindView(android.R.id.content)
    View rootView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick(R.id.back)
    @Optional
    void back() {
        super.onBackPressed();
    }

    @OnClick(R.id.share)
    @Optional
    void share() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link_domain, getShareLinkPath())))
                .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                .setCallToActionText(getString(R.string.invitation_action_call)).build();
        startActivityForResult(intent, RQ_INVITATION);
    }

    protected String getShareLinkPath() {
        return "welcome";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RQ_INVITATION) {
            if (resultCode == RESULT_OK) {
//                Snackbar.make(rootView, R.string.invitation_successful, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(rootView, R.string.invitation_failed, Snackbar.LENGTH_LONG)
                        .setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                share();
                            }
                        }).show();
            }
        }
    }
}
