package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDTO {
    private int paymentID;
    private int userID;
    private BigDecimal amount;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime createdTime;
    private String description;
    private String transactionID; // ID từ payment gateway
    
    public PaymentDTO() {
        this.paymentID = 0;
        this.userID = 0;
        this.amount = BigDecimal.ZERO;
        this.paymentStatus = "pending";
        this.paymentMethod = "";
        this.createdTime = LocalDateTime.now();
        this.description = "";
        this.transactionID = "";
    }
    
    public PaymentDTO(int userID, BigDecimal amount, String paymentMethod, String description) {
        this.paymentID = 0;
        this.userID = userID;
        this.amount = amount;
        this.paymentStatus = "pending";
        this.paymentMethod = paymentMethod;
        this.createdTime = LocalDateTime.now();
        this.description = description;
        this.transactionID = "";
    }
    
    public PaymentDTO(int paymentID, int userID, BigDecimal amount, String paymentStatus, 
                     String paymentMethod, LocalDateTime createdTime, String description, String transactionID) {
        this.paymentID = paymentID;
        this.userID = userID;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.createdTime = createdTime;
        this.description = description;
        this.transactionID = transactionID;
    }

    // Getters and Setters
    public int getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(int paymentID) {
        this.paymentID = paymentID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }
}