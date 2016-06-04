package com.jp.qanda.question;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * @author jpwang
 * @since 6/2/16
 */
public class QuestionDetailActivity extends AppCompatActivity {
    private final static String Q_KEY = "q_key";
    public static Intent createIntent(Context context, String questionKey) {
        Intent intent = new Intent();
        intent.setClass(context, QuestionDetailActivity.class);
        intent.putExtra(Q_KEY, questionKey);
        return intent;
    }
}
