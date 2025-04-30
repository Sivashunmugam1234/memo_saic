package com.example.memo_saic;

public class PhotoData {
    private String imageUrl;
    private String timestamp;
    private String uploadDate;
    private String userId;
    private String district;
    private String state;

    // Default constructor for Firestore deserialization
    public PhotoData() {
    }

    public PhotoData(String imageUrl, String district, String state, String uploadDate, String userId) {
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.district = district;
        this.state = state;
        this.uploadDate = uploadDate;
        this.userId = userId;
    }

    // Constructor with fewer parameters for backward compatibility
    public PhotoData(String imageUrl, String timestamp, String district, String state) {
        this(imageUrl, district, state, null, null);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Helper method to get a caption-like string
    public String getCaption() {
        return district + ", " + state;
    }
}