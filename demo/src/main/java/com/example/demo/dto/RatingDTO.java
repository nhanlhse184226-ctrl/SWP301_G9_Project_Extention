package com.example.demo.dto;

import java.util.Date;

public class RatingDTO {
    private int ratingID;
    private int stationID;
    private Integer userID; // Có thể null cho rating ẩn danh
    private int rating; // 1-5 sao
    private Date createAt;
    
    // Default constructor
    public RatingDTO() {}
    
    // Constructor with all parameters
    public RatingDTO(int ratingID, int stationID, Integer userID, int rating, Date createAt) {
        this.ratingID = ratingID;
        this.stationID = stationID;
        this.userID = userID;
        this.rating = rating;
        this.createAt = createAt;
    }
    
    // Constructor for creating new rating (without ID and createAt)
    public RatingDTO(int stationID, Integer userID, int rating) {
        this.stationID = stationID;
        this.userID = userID;
        this.rating = rating;
    }
    
    // Constructor for simple rating (stationID and rating only)
    public RatingDTO(int stationID, int rating) {
        this.stationID = stationID;
        this.rating = rating;
    }
    
    // Getters and Setters
    public int getRatingID() { return ratingID; }
    public void setRatingID(int ratingID) { this.ratingID = ratingID; }
    
    public int getStationID() { return stationID; }
    public void setStationID(int stationID) { this.stationID = stationID; }
    
    public Integer getUserID() { return userID; }
    public void setUserID(Integer userID) { this.userID = userID; }
    
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    
    public Date getCreateAt() { return createAt; }
    public void setCreateAt(Date createAt) { this.createAt = createAt; }
    
    @Override
    public String toString() {
        return "RatingDTO{" +
                "ratingID=" + ratingID +
                ", stationID=" + stationID +
                ", userID=" + userID +
                ", rating=" + rating +
                ", createAt=" + createAt +
                '}';
    }
}