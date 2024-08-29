package com.manhnt.object;


public class Comments {

    private int id, user_id;
    private String comment, avatar, userName, commentTime;

    public Comments(int id, int user_id, String comment, String avatar, String userName, String commentTime){
        this.id = id;
        this.user_id = user_id;
        this.comment = comment;
        this.avatar = avatar;
        this.userName = userName;
        this.commentTime = commentTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getUserName() {
        return userName;
    }

    public String getCommentTime() {
        return commentTime;
    }

    public int getUser_id() {
        return user_id;
    }

}
