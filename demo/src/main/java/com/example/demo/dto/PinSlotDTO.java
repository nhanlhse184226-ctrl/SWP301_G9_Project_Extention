package com.example.demo.dto;

public class PinSlotDTO {
    private int pinID;
    private int pinPercent;
    private int pinStatus;
    private int status; // Thêm trường status mới
    private Integer userID; // Thêm userID cho chức năng reserve
    private int stationID; // Thêm stationID để biết pin thuộc station nào

    public PinSlotDTO() {
        this.pinID = 0;
        this.pinPercent = 0;
        this.pinStatus = 0;
        this.status = 0;
        this.userID = null;
        this.stationID = 0;
    }

    public PinSlotDTO(int pinID, int pinPercent, int pinStatus, int status, Integer userID) {
        this.pinID = pinID;
        this.pinPercent = pinPercent;
        this.pinStatus = pinStatus;
        this.status = status;
        this.userID = userID;
        this.stationID = 0;
    }

    // Constructor with userID and stationID
    public PinSlotDTO(int pinID, int pinPercent, int pinStatus, int status, Integer userID, int stationID) {
        this.pinID = pinID;
        this.pinPercent = pinPercent;
        this.pinStatus = pinStatus;
        this.status = status;
        this.userID = userID;
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
    
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }
    
    public int getStationID() {
        return stationID;
    }

    public void setStationID(int stationID) {
        this.stationID = stationID;
    }
}
