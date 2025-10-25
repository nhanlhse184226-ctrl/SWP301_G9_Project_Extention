package com.example.demo.dto;

import java.util.Date;

public class TransactionDTO {
    private int transactionID;
    private int vehicleID;  // Thay đổi từ userID sang vehicleID
    private int amount;
    private int pack;
    private int stationID;
    private int pinID;
    private int status;
    private Date createAt;
    private Date expireAt;

    // Constructor rỗng
    public TransactionDTO() {
    }

    // Constructor đầy đủ
    public TransactionDTO(int transactionID, int vehicleID, int amount, int pack, int stationID, 
                         int pinID, int status, Date createAt, Date expireAt) {
        this.transactionID = transactionID;
        this.vehicleID = vehicleID;
        this.amount = amount;
        this.pack = pack;
        this.stationID = stationID;
        this.pinID = pinID;
        this.status = status;
        this.createAt = createAt;
        this.expireAt = expireAt;
    }

    // Constructor để tạo transaction mới (không có transactionID)
    public TransactionDTO(int vehicleID, int amount, int pack, int stationID, 
                         int pinID, int status, Date createAt, Date expireAt) {
        this.vehicleID = vehicleID;
        this.amount = amount;
        this.pack = pack;
        this.stationID = stationID;
        this.pinID = pinID;
        this.status = status;
        this.createAt = createAt;
        this.expireAt = expireAt;
    }

    // Constructor cơ bản cho việc tạo transaction
    public TransactionDTO(int vehicleID, int amount, int pack, int stationID, int pinID) {
        this.vehicleID = vehicleID;
        this.amount = amount;
        this.pack = pack;
        this.stationID = stationID;
        this.pinID = pinID;
        this.status = 0; // Default: pending
        this.createAt = new Date(); // Tự động set thời gian hiện tại
    }

    // Getters và Setters
    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public int getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(int vehicleID) {
        this.vehicleID = vehicleID;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getPack() {
        return pack;
    }

    public void setPack(int pack) {
        this.pack = pack;
    }

    public int getStationID() {
        return stationID;
    }

    public void setStationID(int stationID) {
        this.stationID = stationID;
    }

    public int getPinID() {
        return pinID;
    }

    public void setPinID(int pinID) {
        this.pinID = pinID;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
    }

    // ToString method để debug
    @Override
    public String toString() {
        return "TransactionDTO{" +
                "transactionID=" + transactionID +
                ", vehicleID=" + vehicleID +
                ", amount=" + amount +
                ", pack=" + pack +
                ", stationID=" + stationID +
                ", pinID=" + pinID +
                ", status=" + status +
                ", createAt=" + createAt +
                ", expireAt=" + expireAt +
                '}';
    }

}