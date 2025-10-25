package com.example.demo.dto;

public class PinSlotDTO {
    private int pinID;
    private int pinPercent;
    private int pinStatus;
    private int pinHealth; // Thêm trường pinHealth mới
    private int status; // Thêm trường status mới
    private Integer vehicleID; // Thay đổi từ userID sang vehicleID cho chức năng reserve
    private int stationID; // Thêm stationID để biết pin thuộc station nào

    public PinSlotDTO() {
        this.pinID = 0;
        this.pinPercent = 0;
        this.pinStatus = 0;
        this.pinHealth = 0;
        this.status = 0;
        this.vehicleID = null;
        this.stationID = 0;
    }

    public PinSlotDTO(int pinID, int pinPercent, int pinStatus, int status, Integer vehicleID) {
        this.pinID = pinID;
        this.pinPercent = pinPercent;
        this.pinStatus = pinStatus;
        this.pinHealth = 0; // Default value
        this.status = status;
        this.vehicleID = vehicleID;
        this.stationID = 0;
    }

    // Constructor with vehicleID and stationID
    public PinSlotDTO(int pinID, int pinPercent, int pinStatus, int status, Integer vehicleID, int stationID) {
        this.pinID = pinID;
        this.pinPercent = pinPercent;
        this.pinStatus = pinStatus;
        this.pinHealth = 0; // Default value
        this.status = status;
        this.vehicleID = vehicleID;
        this.stationID = stationID;
    }
    
    // Constructor với đầy đủ fields bao gồm pinHealth
    public PinSlotDTO(int pinID, int pinPercent, int pinHealth, int pinStatus, int status, Integer vehicleID, int stationID) {
        this.pinID = pinID;
        this.pinPercent = pinPercent;
        this.pinHealth = pinHealth;
        this.pinStatus = pinStatus;
        this.status = status;
        this.vehicleID = vehicleID;
        this.stationID = stationID;
    }

    public int getPinID() {
        return pinID;
    }

    public void setPinID(int pinID) {
        this.pinID = pinID;
    }

    public int getPinPercent() {
        return pinPercent;
    }

    public void setPinPercent(int pinPercent) {
        this.pinPercent = pinPercent;
    }

    public int getPinStatus() {
        return pinStatus;
    }

    public void setPinStatus(int pinStatus) {
        this.pinStatus = pinStatus;
    }
    
    public int getPinHealth() {
        return pinHealth;
    }

    public void setPinHealth(int pinHealth) {
        this.pinHealth = pinHealth;
    }
    
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public Integer getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(Integer vehicleID) {
        this.vehicleID = vehicleID;
    }
    
    public int getStationID() {
        return stationID;
    }

    public void setStationID(int stationID) {
        this.stationID = stationID;
    }
}
