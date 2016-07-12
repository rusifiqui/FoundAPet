package com.jvilam.foundapet.entities;

import java.io.Serializable;

/**
 * Created by jvilam on 22/06/2016.
 *
 */
public class Comment implements Serializable {
    private int id;
    private String userName;
    private String date;
    private String comment;

    public Comment(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
