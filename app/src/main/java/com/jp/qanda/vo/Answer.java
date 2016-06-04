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
public class Answer {
    public String content;
    public int rating;
    public int secretListeners;
    public long timestamp = System.currentTimeMillis();

    public Answer() {

    }

    public Answer(String content) {
        this.content = content;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("content", content);
        map.put("rating", rating);
        map.put("secretListeners", secretListeners);
        map.put("timestamp", timestamp);
        return map;
    }
}
