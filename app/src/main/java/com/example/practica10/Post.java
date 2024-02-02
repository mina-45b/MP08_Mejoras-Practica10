package com.example.practica10;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Post {
    public String postId;
    public String uid;
    public String author;
    public String authorPhotoUrl;
    public String content;
    public String mediaUrl;
    public String mediaType;
    public  long timeStamp;

    public Map<String, Boolean> bookMarks = new HashMap<>();
    public Map<String, Boolean> likes = new HashMap<>();

    // Constructor vacio requerido por Firestore
    public Post() {
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public Post(String uid, String author, String authorPhotoUrl, String content, long timeStamp, String mediaUrl, String mediaType) {
        this.uid = uid;
        this.author = author;
        this.authorPhotoUrl = authorPhotoUrl;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.timeStamp = timeStamp;
    }
}
