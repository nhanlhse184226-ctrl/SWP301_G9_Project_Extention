package com.example.demo.dto;

import java.time.LocalDateTime;

public class PinSlotDTO {
    private int pinID;
    private int pinPercent;
    private String pinStatus;
    private String reserveStatus;
    private LocalDateTime reserveTime;
    private Integer stationID; // Thêm stationID để biết pin thuộc station nào

    public PinSlotDTO() {
        this.pinID = 0;
        this.pinPercent = 0;
        this.pinStatus = "";
        this.reserveStatus = "ready";
        this.reserveTime = null;
        this.stationID = null;
    }

    public PinSlotDTO(int pinID, int pinPercent, String pinStatus) {
        this.pinID = pinID;
        this.pinPercent = pinPercent;
        this.pinStatus = pinStatus;
        this.reserveStatus = "ready";
        this.reserveTime = null;
        this.stationID = null;
    }

    public PinSlotDTO(int pinID, int pinPercent, String pinStatus, String reserveStatus, LocalDateTime reserveTime) {
        this.pinID = pinID;
        this.pinPercent = pinPercent;
        this.pinStatus = pinStatus;
        this.reserveStatus = reserveStatus;
        this.reserveTime = reserveTime;
        this.stationID = null;
    }
    
    // Constructor with stationID
    public PinSlotDTO(int pinID, int pinPercent, String pinStatus, String reserveStatus, LocalDateTime reserveTime, Integer stationID) {
        this.pinID = pinID;
        this.pinPercent = pinPercent;
        this.pinStatus = pinStatus;
        this.reserveStatus = reserveStatus;
        this.reserveTime = reserveTime;
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

    public String getPinStatus() {
        return pinStatus;
    }

    public void setPinStatus(String pinStatus) {
        this.pinStatus = pinStatus;
    }

    public String getReserveStatus() {
        return reserveStatus;
    }

    public void setReserveStatus(String reserveStatus) {
        this.reserveStatus = reserveStatus;
    }

    public LocalDateTime getReserveTime() {
        return reserveTime;
    }

    public void setReserveTime(LocalDateTime reserveTime) {
        this.reserveTime = reserveTime;
    }
    
    public Integer getStationID() {
        return stationID;
    }

    public void setStationID(Integer stationID) {
        this.stationID = stationID;
    }
}
