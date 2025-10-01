package com.example.demo.dto;

public class PinSlotDTO {
    private int pinID;
    private int pinPercent;
    private String pinStatus;

    public PinSlotDTO() {
        this.pinID = 0;
        this.pinPercent = 0;
        this.pinStatus = "";
    }

    public PinSlotDTO(int pinID, int pinPercent, String pinStatus) {
        this.pinID = pinID;
        this.pinPercent = pinPercent;
        this.pinStatus = pinStatus;
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
}
