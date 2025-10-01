package com.example.demo.dto;

import java.time.LocalDateTime;

public class PinSlotDTO {
    private int pinID;
    private int pinPercent;
    private String pinStatus;
    private String reserveStatus;
    private LocalDateTime reserveTime;

    public PinSlotDTO() {
        this.pinID = 0;
        this.pinPercent = 0;
        this.pinStatus = "";
        this.reserveStatus = "ready";
        this.reserveTime = null;
    }

    public PinSlotDTO(int pinID, int pinPercent, String pinStatus) {
        this.pinID = pinID;
        this.pinPercent = pinPercent;
        this.pinStatus = pinStatus;
        this.reserveStatus = "ready";
        this.reserveTime = null;
    }

    public PinSlotDTO(int pinID, int pinPercent, String pinStatus, String reserveStatus, LocalDateTime reserveTime) {
        this.pinID = pinID;
        this.pinPercent = pinPercent;
        this.pinStatus = pinStatus;
        this.reserveStatus = reserveStatus;
        this.reserveTime = reserveTime;
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
}
