package com.jp.qanda.vo;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * @author jpwang
 * @since 6/2/16
 */
@IgnoreExtraProperties
public class Answer {
    public String content;
    public int rating;
    public int secretListeners;
    public long timestamp;

    public Answer() {

    }

    public Answer(String content) {
        this.content = content;
    }
}
