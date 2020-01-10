package com.joonasniemi.joonaksenpainikepeli;

public class User {
    private int points;

    public User(){

    }

    public User(int points){
        this.points = points;
    }

    public void addPoints(int points){
        this.points += points;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
