package com.example.demo.dto;

public class PinSlotDTO {
    private int pinID;
    private int pinPercent;
    private int pinStatus;
    private int status; // Thêm trường status mới
    private Integer stationID; // Thêm stationID để biết pin thuộc station nào

    public PinSlotDTO() {
        this.pinID = 0;
        this.pinPercent = 0;
        this.pinStatus = 0;
        this.status = 0;
        this.stationID = null;
    }

    public PinSlotDTO(int pinID, int pinPercent, int pinStatus, int status) {
        this.pinID = pinID;
        this.pinPercent = pinPercent;
        this.pinStatus = pinStatus;
        this.status = status;
        this.stationID = null;
    }

    // Constructor with stationID and status
    public PinSlotDTO(int pinID, int pinPercent, int pinStatus, int status, Integer stationID) {
        this.pinID = pinID;
        this.pinPercent = pinPercent;
        this.pinStatus = pinStatus;
        this.status = status;
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
    
    public Integer getStationID() {
        return stationID;
    }

    public void setStationID(Integer stationID) {
        this.stationID = stationID;
    }
}
