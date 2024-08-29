package com.manhnt.object;

import java.io.Serializable;

public class Conversation implements Serializable{

    private String user_avatar, user_name;
    private int user_id, status;

    public Conversation(int user_id, String user_avatar, String user_name, int status){
        this.user_id = user_id;
        this.user_avatar = user_avatar;
        this.user_name = user_name;
        this.status = status;
    }

    public String getUser_avatar() {
        return user_avatar;
    }

    public String getUser_name() {
        return user_name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getUser_id() {
        return user_id;
    }

}
