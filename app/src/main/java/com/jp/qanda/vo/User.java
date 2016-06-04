package com.jp.qanda.vo;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * @author jpwang
 * @since 6/2/16
 */
@IgnoreExtraProperties
public class User {
    public String username;
    public String title;
    public String desc;
    public double questFee = 3.0;
    public String photoUrl;
    public String email;
    public int followers;
    public int superiors;
    public int questions;
    public int answers;
    public double totalRevenue;

    public User() {
    }

}
