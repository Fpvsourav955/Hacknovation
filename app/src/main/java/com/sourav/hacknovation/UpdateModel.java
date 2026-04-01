package com.sourav.hacknovation;

public class UpdateModel {
    public String sender;
    public String message;
    public long timestamp;
    public String photoUrl;

    public UpdateModel() {}

    public UpdateModel(String sender, String message, long timestamp,String photoUrl) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
        this.photoUrl = photoUrl;
    }
}
