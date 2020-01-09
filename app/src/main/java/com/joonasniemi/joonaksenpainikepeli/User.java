package com.joonasniemi.joonaksenpainikepeli;

public class User {
    private String userId;
    private String username;
    private String password;
    private int points;

    public User() {
    }

    public User(String userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        points = 20;
    }
}
