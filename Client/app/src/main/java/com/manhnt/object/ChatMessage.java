package com.manhnt.object;

import java.io.Serializable;

public class ChatMessage implements Serializable {

    private int id;
    private int chat_id;
    private int from_id, to_id;
    private String message;
    private String created;
    private int status;
    private String userName;

    public ChatMessage(int id, int chat_id,int from_id, int to_id, String message, String created, int status,
        String UserName){
        this.id = id;
        this.chat_id = chat_id;
        this.from_id = from_id;
        this.to_id = to_id;
        this.message = message;
        this.status = status;
        this.created = created;
        this.userName = UserName;
    }

    public int getId() {
        return id;
    }

    public int getFrom_id() {
        return from_id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTo_id() {
        return to_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreated() {
        return created;
    }

    public int getStatus() {
        return status;
    }

    public String getUserName() {
        return userName;
    }

    public int getChat_id() {
        return chat_id;
    }

}
