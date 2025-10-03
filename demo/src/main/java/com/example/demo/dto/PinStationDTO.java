package com.example.demo.dto;

import java.util.Date;

public class PinStationDTO {
    private int stationID;
    private String stationName;
    private String location;
    private String status;
    private Date createAt;
    
    // Default constructor
    public PinStationDTO() {}
    
    // Constructor with parameters
    public PinStationDTO(int stationID, String stationName, String location, String status, Date createAt) {
        this.stationID = stationID;
        this.stationName = stationName;
        this.location = location;
        this.status = status;
        this.createAt = createAt;
    }
    
    // Constructor for creating new station (without ID and createAt)
    public PinStationDTO(String stationName, String location, String status) {
        this.stationName = stationName;
        this.location = location;
        this.status = status;
    }
    
    // Getters and Setters
    public int getStationID() { return stationID; }
    public void setStationID(int stationID) { this.stationID = stationID; }
    
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getCreateAt() { return createAt; }
    public void setCreateAt(Date createAt) { this.createAt = createAt; }
    
    @Override
    public String toString() {
        return "PinStationDTO{" +
                "stationID=" + stationID +
                ", stationName='" + stationName + '\'' +
                ", location='" + location + '\'' +
                ", status='" + status + '\'' +
                ", createAt=" + createAt +
                '}';
    }
}