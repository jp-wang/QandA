package com.jp.qanda.vo;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jpwang
 * @since 6/2/16
 */
@IgnoreExtraProperties
public class Question {
    public String content;
    public double questFee;
    public String from;
    public String to;
    public long timestamp = System.currentTimeMillis();
    public Answer answer;

    public Question() {

    }

    public Question(String content, double questfee, String from, String to) {
        this.content = content;
        this.questFee = questfee;
        this.from = from;
        this.to = to;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("content", content);
        map.put("questFee", questFee);
        map.put("from", from);
        map.put("to", to);
        map.put("timestamp", timestamp);
        if (answer != null) {
            map.put("answer", answer.toMap());
        }
        return map;
    }
}
