package com.trashtag.app;

import java.io.Serializable;

public class User  implements Serializable {

    static User user;

    public User(FireUser fUser){
        username = fUser.getUsername();
        recyclePins = fUser.getRecyclePins();
        trashPins = fUser.getTrashPins();
        score = fUser.getScore();
    }

    public User(String s){
        username = s;
        recyclePins = 0;
        trashPins = 0;
        score = 0;
    }

    private String username;
    private int recyclePins;
    private int trashPins;
    private int score;

    public int getScore() {return score;}

    public void setScore(int score) {this.score = score;}

    public String getUsername() {return username;}

    public void setUsername(String username) {this.username = username;}

    public int getRecyclePins() {return recyclePins;}

    public void setRecyclePins(int recyclePins) {this.recyclePins = recyclePins;}

    public int getTrashPins() {return trashPins;}

    public void setTrashPins(int trashPins) {this.trashPins = trashPins;}

    public void updatePinScore(String type){
        switch (type) {
            case "Trash":
                trashPins++;
                break;
            case "Recycling":
                recyclePins++;
                break;
            case "Pickup":
                score++;
                break;
        }

        AppResources.databaseReference().child("Users/"+AppResources.mAuth().getCurrentUser().getUid()).setValue(new FireUser(User.user));
    }
}

class FireUser implements Serializable {

    private String username;
    private int recyclePins;
    private int trashPins;
    private int score;

    public String getUsername() {return username;}

    public void setUsername(String username) {this.username = username;}

    public int getRecyclePins() {return recyclePins;}

    public void setRecyclePins(int recyclePins) {this.recyclePins = recyclePins;}

    public int getTrashPins() {return trashPins;}

    public void setTrashPins(int trashPins) {this.trashPins = trashPins;}

    public int getScore() {return score;}

    public void setScore(int score) {this.score = score;}

    public FireUser(User u){
        username = u.getUsername();
        recyclePins = u.getRecyclePins();
        trashPins = u.getTrashPins();
        score = u.getScore();
    }

    public FireUser(){

    }
}

