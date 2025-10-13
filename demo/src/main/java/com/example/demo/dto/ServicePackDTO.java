package com.example.demo.dto;

import java.sql.Timestamp;

public class ServicePackDTO {
    private int packID;
    private String packName;
    private String status;
    private String description;
    private int total;
    private int price;
    private Timestamp createDate;

    // Default constructor
    public ServicePackDTO() {}

    // Constructor cho create (không có packID và createDate vì auto-generated)
    public ServicePackDTO(String packName, String status, String description, int total, int price) {
        this.packName = packName;
        this.status = status;
        this.description = description;
        this.total = total;
        this.price = price;
    }

    // Constructor đầy đủ (cho response từ database)
    public ServicePackDTO(int packID, String packName, String status, String description, int total, int price, Timestamp createDate) {
        this.packID = packID;
        this.packName = packName;
        this.status = status;
        this.description = description;
        this.total = total;
        this.price = price;
        this.createDate = createDate;
    }

    // Getters and Setters
    public int getPackID() {
        return packID;
    }

    public void setPackID(int packID) {
        this.packID = packID;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "ServicePackDTO{" +
                "packID=" + packID +
                ", packName='" + packName + '\'' +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", total=" + total +
                ", price=" + price +
                ", createDate=" + createDate +
                '}';
    }
}