package com.example.demo.dto;

/**
 * VNPay Payment Response DTO - Cho API trả về QR code
 */
public class VNPayPaymentResponseDTO {
    
    private String txnRef;           // Mã giao dịch
    private String paymentUrl;       // URL thanh toán VNPay
    private String qrCodeData;       // Data để tạo QR code
    private String qrCodeBase64;     // QR code dạng base64 image
    private Long amount;             // Số tiền (VND)
    private String orderInfo;        // Thông tin đơn hàng
    private String expiredAt;        // Thời gian hết hạn
    private String status;           // Trạng thái payment
    
    // Constructor
    public VNPayPaymentResponseDTO() {}
    
    public VNPayPaymentResponseDTO(String txnRef, String paymentUrl, String qrCodeData, 
                                  Long amount, String orderInfo, String status) {
        this.txnRef = txnRef;
        this.paymentUrl = paymentUrl;
        this.qrCodeData = qrCodeData;
        this.amount = amount;
        this.orderInfo = orderInfo;
        this.status = status;
    }
    
    // Getters and Setters
    public String getTxnRef() { return txnRef; }
    public void setTxnRef(String txnRef) { this.txnRef = txnRef; }
    
    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
    
    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }
    
    public String getQrCodeBase64() { return qrCodeBase64; }
    public void setQrCodeBase64(String qrCodeBase64) { this.qrCodeBase64 = qrCodeBase64; }
    
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    
    public String getOrderInfo() { return orderInfo; }
    public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }
    
    public String getExpiredAt() { return expiredAt; }
    public void setExpiredAt(String expiredAt) { this.expiredAt = expiredAt; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    // vnp_Amount là Long lưu VND nguyên
    public Double getAmountVND() {
        return amount != null ? amount.doubleValue() : 0.0;
    }

    public void setAmountVND(Double amountVND) {
        this.amount = amountVND != null ? Math.round(amountVND) : 0L;
    }
}