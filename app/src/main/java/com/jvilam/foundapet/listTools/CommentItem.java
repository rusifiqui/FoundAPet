package com.jvilam.foundapet.listTools;

/**
 * Created by jvilam on 23/06/2016.
 *
 */
public class CommentItem {
    private String title;
    private String comment;

    public CommentItem(String t, String c){
        this.title = t;
        this.comment = c;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
