package com.example.demo.dto;

public class SubscriptionDTO {
    private int userID;
    private int total;

    public SubscriptionDTO() {}

    public SubscriptionDTO(int userID, int total) {
        this.userID = userID;
        this.total = total;
    }

    // Getters and Setters
    public int getUserID() { return userID; }
    public void setUserID(int userID) { this.userID = userID; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}