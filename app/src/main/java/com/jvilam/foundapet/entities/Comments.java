package com.jvilam.foundapet.entities;

import java.io.Serializable;
import java.util.Vector;

/**
 * Created by jvilam on 22/06/2016.
 *
 */
public class Comments implements Serializable {

    private Vector<Comment> comments;

    public Comments(){
        comments = new Vector<>();
    }

    public Vector<Comment> getComments() {
        return comments;
    }

    public void setComments(Vector<Comment> comments) {
        this.comments = comments;
    }

    public void addComment(Comment c){
        comments.add(c);
    }

    public int size(){
        return comments.size();
    }

    public Comment get(int i){
        return comments.get(i);
    }

}
