package com.example.demo.dto;

public class VehicleDTO {
    private int vehicleID;
    private int userID;
    private String licensePlate;
    private String vehicleType;
    private int pinPercent;
    private int pinHealth;

    // Default constructor
    public VehicleDTO() {
    }

    // Constructor with all fields
    public VehicleDTO(int vehicleID, int userID, String licensePlate, String vehicleType, int pinPercent, int pinHealth) {
        this.vehicleID = vehicleID;
        this.userID = userID;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.pinPercent = pinPercent;
        this.pinHealth = pinHealth;
    }

    // Getters and Setters
    public int getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(int vehicleID) {
        this.vehicleID = vehicleID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public int getPinPercent() {
        return pinPercent;
    }

    public void setPinPercent(int pinPercent) {
        this.pinPercent = pinPercent;
    }

    public int getPinHealth() {
        return pinHealth;
    }

    public void setPinHealth(int pinHealth) {
        this.pinHealth = pinHealth;
    }

    @Override
    public String toString() {
        return "VehicleDTO{" +
                "vehicleID=" + vehicleID +
                ", userID=" + userID +
                ", licensePlate='" + licensePlate + '\'' +
                ", vehicleType='" + vehicleType + '\'' +
                ", pinPercent=" + pinPercent +
                ", pinHealth=" + pinHealth +
                '}';
    }
}