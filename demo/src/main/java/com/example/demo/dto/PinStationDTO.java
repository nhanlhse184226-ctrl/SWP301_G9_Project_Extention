package com.example.demo.dto;

import java.util.Date;

public class PinStationDTO {
    private int stationID;
    private String stationName;
    private String location;
    private int status;
    private Date createAt;
    private float x;
    private float y;
    private Integer userID;  // Nullable for station owner/manager
    
    // Default constructor
    public PinStationDTO() {}
    
    // Constructor with parameters
    public PinStationDTO(int stationID, String stationName, String location, int status, Date createAt, float x, float y, Integer userID) {
        this.stationID = stationID;
        this.stationName = stationName;
        this.location = location;
        this.status = status;
        this.createAt = createAt;
        this.x = x;
        this.y = y;
        this.userID = userID;
    }
    
    // Constructor for creating new station (without ID and createAt)
    public PinStationDTO(String stationName, String location, int status, float x, float y, Integer userID) {
        this.stationName = stationName;
        this.location = location;
        this.status = status;
        this.x = x;
        this.y = y;
        this.userID = userID;
    }
    
    // Getters and Setters
    public int getStationID() { return stationID; }
    public void setStationID(int stationID) { this.stationID = stationID; }
    
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    
    public Date getCreateAt() { return createAt; }
    public void setCreateAt(Date createAt) { this.createAt = createAt; }
    
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    
    public Integer getUserID() { return userID; }
    public void setUserID(Integer userID) { this.userID = userID; }
    
    @Override
    public String toString() {
        return "PinStationDTO{" +
                "stationID=" + stationID +
                ", stationName='" + stationName + '\'' +
                ", location='" + location + '\'' +
                ", status=" + status +
                ", createAt=" + createAt +
                ", x=" + x +
                ", y=" + y +
                ", userID=" + userID +
                '}';
    }
}