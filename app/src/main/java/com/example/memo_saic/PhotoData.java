package com.example.memo_saic;

public class PhotoData {
    private final String imageUrl;
    private final String caption;
    private final String timestamp;

    public PhotoData(String imageUrl, String caption, String timestamp) {
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public String getTimestamp() {
        return timestamp;
    }
}