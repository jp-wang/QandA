package com.jp.qanda.vo;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * @author jpwang
 * @since 6/2/16
 */
@IgnoreExtraProperties
public class Question {
    public String content;
    public float questFee;
    public String from;
    public String to;
    public long timestamp;
    public Answer answer;

    public Question() {

    }

    public Question(String content, float questfee, String from, String to) {
        this.content = content;
        this.questFee = questfee;
        this.from = from;
        this.to = to;
        this.timestamp = System.currentTimeMillis();
    }
}
