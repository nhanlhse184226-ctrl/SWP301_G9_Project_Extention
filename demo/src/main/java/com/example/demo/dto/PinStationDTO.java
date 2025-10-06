package com.example.demo.dto;

import java.util.Date;

public class PinStationDTO {
    private int stationID;
    private String stationName;
    private String location;
    private int status;
    private Date createAt;
    private int x;
    private int y;
    
    // Default constructor
    public PinStationDTO() {}
    
    // Constructor with parameters
    public PinStationDTO(int stationID, String stationName, String location, int status, Date createAt, int x, int y) {
        this.stationID = stationID;
        this.stationName = stationName;
        this.location = location;
        this.status = status;
        this.createAt = createAt;
        this.x = x;
        this.y = y;
    }
    
    // Constructor for creating new station (without ID and createAt)
    public PinStationDTO(String stationName, String location, int status, int x, int y) {
        this.stationName = stationName;
        this.location = location;
        this.status = status;
        this.x = x;
        this.y = y;
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
    
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    
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
                '}';
    }
}