package com.example.demo.dto;

/**
 * VNPay Payment DTO - Đơn giản, chỉ cần những field cơ bản
 */
public class VNPayPaymentDTO {
    
    // Basic payment info
    private Integer paymentID;
    private Integer userID;
    private Integer packID;
    private Integer stationID;
    private Integer pinID;
    
    // VNPay fields
    private String vnp_TxnRef;           // Mã giao dịch
    private String vnp_OrderInfo;        // Thông tin đơn hàng
    private Long vnp_Amount;             // Số tiền (VND * 100)
    
    // VNPay response (after payment)
    private String vnp_TransactionNo;    // Mã giao dịch VNPay
    private String vnp_ResponseCode;     // Mã phản hồi
    private String vnp_TransactionStatus; // Trạng thái giao dịch
    private String vnp_PayDate;          // Thời gian thanh toán
    private String vnp_BankCode;         // Mã ngân hàng
    
    // Simple status
    private int status;               //0 PENDING, 1 SUCCESS, 2 FAILED, 3 EXPIRED
    
    // Timestamps
    private String createdAt;
    private String updatedAt;
    private String expiredAt;
    
    // Default constructor
    public VNPayPaymentDTO() {}
    
    // Constructor for creation
    public VNPayPaymentDTO(Integer userID, Integer packID, String orderInfo, Long amount, Integer stationID, Integer pinID) {
        this.userID = userID;
        this.packID = packID;
        this.vnp_OrderInfo = orderInfo;
        this.vnp_Amount = amount;
        this.status = 0;
        this.stationID = stationID;
        this.pinID = pinID;
    }
    
    // Getters and Setters
    public Integer getPaymentID() { return paymentID; }
    public void setPaymentID(Integer paymentID) { this.paymentID = paymentID; }
    
    public Integer getUserID() { return userID; }
    public void setUserID(Integer userID) { this.userID = userID; }
    
    public Integer getPackID() { return packID; }
    public void setPackID(Integer packID) { this.packID = packID; }
    
    public Integer getStationID() { return stationID; }
    public void setStationID(Integer stationID) { this.stationID = stationID; }

    public Integer getPinID() { return pinID; }
    public void setPinID(Integer pinID) { this.pinID = pinID; }

    public String getVnp_TxnRef() { return vnp_TxnRef; }
    public void setVnp_TxnRef(String vnp_TxnRef) { this.vnp_TxnRef = vnp_TxnRef; }
    
    public String getVnp_OrderInfo() { return vnp_OrderInfo; }
    public void setVnp_OrderInfo(String vnp_OrderInfo) { this.vnp_OrderInfo = vnp_OrderInfo; }
    
    public Long getVnp_Amount() { return vnp_Amount; }
    public void setVnp_Amount(Long vnp_Amount) { this.vnp_Amount = vnp_Amount; }
    
    public String getVnp_TransactionNo() { return vnp_TransactionNo; }
    public void setVnp_TransactionNo(String vnp_TransactionNo) { this.vnp_TransactionNo = vnp_TransactionNo; }
    
    public String getVnp_ResponseCode() { return vnp_ResponseCode; }
    public void setVnp_ResponseCode(String vnp_ResponseCode) { this.vnp_ResponseCode = vnp_ResponseCode; }
    
    public String getVnp_TransactionStatus() { return vnp_TransactionStatus; }
    public void setVnp_TransactionStatus(String vnp_TransactionStatus) { this.vnp_TransactionStatus = vnp_TransactionStatus; }
    
    public String getVnp_PayDate() { return vnp_PayDate; }
    public void setVnp_PayDate(String vnp_PayDate) { this.vnp_PayDate = vnp_PayDate; }
    
    public String getVnp_BankCode() { return vnp_BankCode; }
    public void setVnp_BankCode(String vnp_BankCode) { this.vnp_BankCode = vnp_BankCode; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    public String getExpiredAt() { return expiredAt; }
    public void setExpiredAt(String expiredAt) { this.expiredAt = expiredAt; }
    
    // Helper methods
    public boolean isSuccessful() {
        return status == 1 && 
               "00".equals(vnp_ResponseCode) && 
               "00".equals(vnp_TransactionStatus);
    }
    
    public boolean isPending() {
        return status == 0;
    }
    
    public boolean isFailed() {
        return status == 2;
    }
    
    // Get amount in VND (divide by 100)
    public Double getAmountVND() {
        return vnp_Amount != null ? vnp_Amount / 100.0 : 0.0;
    }
    
    // Set amount in VND (multiply by 100)
    public void setAmountVND(Double amountVND) {
        this.vnp_Amount = amountVND != null ? Math.round(amountVND * 100) : 0L;
    }
}