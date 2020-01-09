package com.joonasniemi.joonaksenpainikepeli;

public class User {
    private String username;
    private String password;
    private int points;

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        points = 20;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getPoints() {
        return points;
    }
}
