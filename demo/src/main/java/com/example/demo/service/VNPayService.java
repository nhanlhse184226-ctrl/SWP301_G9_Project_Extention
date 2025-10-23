package com.example.demo.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.config.VNPayConfig;
import com.example.demo.dto.VNPayPaymentDTO;
import com.example.demo.dto.VNPayPaymentResponseDTO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * VNPay Service - Đơn giản hơn PayOS rất nhiều!
 * Chỉ cần 3 method chính: create, buildUrl, verify
 */
@Service
public class VNPayService {
    
    @Autowired
    private VNPayQRCodeService vnpayQRCodeService;

    // DAO for persistence
    private final com.example.demo.dao.VNPayPaymentDAO vnpayPaymentDAO = new com.example.demo.dao.VNPayPaymentDAO();
    
    /**
     * Tạo payment với QR code - Method mới cho frontend 
     * NOTE: stationID và pinID không được lưu vào database nữa (legacy parameters)
     */
    @Deprecated
    public VNPayPaymentResponseDTO createPaymentWithQR(Integer userID, Integer packID, Integer stationID, Integer pinID,
                                                      Double amountVND, String orderInfo, String ipAddress, Integer total) 
            throws UnsupportedEncodingException {
        
        // Simply call the new method without stationID/pinID
        return createPaymentWithQR(userID, packID, amountVND, orderInfo, ipAddress, total);
    }

    /**
     * Tạo payment với QR code - Recommended method (without deprecated stationID/pinID)
     */
    public VNPayPaymentResponseDTO createPaymentWithQR(Integer userID, Integer packID, 
                                                      Double amountVND, String orderInfo, String ipAddress, Integer total) 
            throws UnsupportedEncodingException {
        
        // Tạo payment record using new method without deprecated stationID/pinID
        String txnRef = createPayment(userID, packID, amountVND, orderInfo, total);
        
        // Build payment URL
        String paymentUrl = buildPaymentUrl(txnRef, amountVND, orderInfo, ipAddress);
        
        // Tạo QR code chứa URL thanh toán - User quét QR → Mở browser → Thanh toán
        // Cách này ĐƠN GIẢN và HIỆU QUẢ nhất cho sandbox VNPay
        String qrCodeBase64 = vnpayQRCodeService.generatePaymentUrlQRCode(paymentUrl);
        System.out.println("Generated URL QR code for easy payment access");
        
        // Create response DTO
        VNPayPaymentResponseDTO response = new VNPayPaymentResponseDTO();
        response.setTxnRef(txnRef);
        response.setPaymentUrl(paymentUrl);
        response.setQrCodeData(paymentUrl); // Data dùng để tạo QR
        response.setQrCodeBase64(qrCodeBase64); // QR code image base64
        response.setAmountVND(amountVND);
        response.setOrderInfo(orderInfo);
        response.setStatus("PENDING");
        
        // Set expiration time (15 minutes from now)
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date expiredDate = new Date(System.currentTimeMillis() + 15 * 60 * 1000); // 15 minutes
        response.setExpiredAt(formatter.format(expiredDate));
        
        return response;
    }

    /**
     * Tạo payment với QR code - Method cũ cho backward compatibility 
     */
    public VNPayPaymentResponseDTO createPaymentWithQR(Integer userID, Integer servicePackID, 
                                                      Double amountVND, String orderInfo, String ipAddress) 
            throws UnsupportedEncodingException {
        
        // Gọi method mới với stationID và pinID = null
        return createPaymentWithQR(userID, servicePackID, null, null, amountVND, orderInfo, ipAddress, null);
    }
    
    /**
     * Tạo payment record và trả về transaction reference (recommended - without deprecated stationID/pinID)
     */
    public String createPayment(Integer userID, Integer packID, Double amountVND, String orderInfo, Integer total) {
        // Generate unique transaction reference
        String vnp_TxnRef = generateTxnRef();
        
        // Amount để lưu DB (giữ nguyên VND, không nhân 100)
        Long vnp_Amount = Math.round(amountVND);
        
        // Save to database using DAO (without deprecated stationID/pinID)
        try {
            boolean created = vnpayPaymentDAO.createPayment(userID, packID, vnp_TxnRef, orderInfo, vnp_Amount, 0, total);
            if (!created) {
                System.out.println("Warning: Failed to persist VNPay payment record for txnRef=" + vnp_TxnRef);
            }
        } catch (Exception e) {
            System.out.println("Error saving VNPay payment: " + e.getMessage());
        }
        
        System.out.println("Created VNPay payment: " + vnp_TxnRef + " for amount: " + amountVND + " VND");
        
        return vnp_TxnRef;
    }

    /**
     * Tạo payment record và trả về transaction reference
     * NOTE: stationID và pinID không được lưu vào database nữa (legacy parameters for backward compatibility)
     */
    @Deprecated
    public String createPayment(Integer userID, Integer packID, Integer stationID, Integer pinID, Double amountVND, String orderInfo, Integer total) {
        // Simply call the new method without stationID/pinID
        return createPayment(userID, packID, amountVND, orderInfo, total);
    }

    // Backward compatibility method - stationID và pinID không được lưu vào database
    @Deprecated
    public String createPayment(Integer userID, Integer packID, Integer stationID, Integer pinID, Double amountVND, String orderInfo) {
        return createPayment(userID, packID, amountVND, orderInfo, null);
    }

    /**
     * Tạo payment record và trả về transaction reference (backward compatibility)
     */
    public String createPayment(Integer userID, Integer servicePackID, Double amountVND, String orderInfo) {
        // Gọi method mới without deprecated stationID/pinID
        return createPayment(userID, servicePackID, amountVND, orderInfo, null);
    }
    
    /**
     * Build VNPay payment URL - Theo hướng dẫn chính thức VNPay
     */
    public String buildPaymentUrl(String vnp_TxnRef, Double amountVND, String orderInfo, String ipAddress) 
            throws UnsupportedEncodingException {
        
        // Prepare parameters theo code mẫu VNPay chính thức
        Map<String, String> vnp_Params = new HashMap<>();
        
        // Required parameters - Theo code mẫu VNPay
        vnp_Params.put("vnp_Version", VNPayConfig.VNP_VERSION);
        vnp_Params.put("vnp_Command", VNPayConfig.VNP_COMMAND);
        vnp_Params.put("vnp_TmnCode", VNPayConfig.VNP_TMN_CODE);
        
        // Amount: VNPay yêu cầu nhân 100 theo spec chính thức (bắt buộc!)
        int amount = (int) Math.round(amountVND * 100);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        
        vnp_Params.put("vnp_CurrCode", VNPayConfig.VNP_CURRENCY_CODE);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", VNPayConfig.VNP_ORDER_TYPE);
        vnp_Params.put("vnp_Locale", VNPayConfig.VNP_LOCALE);
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.VNP_RETURN_URL);
        
        // IP Address - Sửa để tránh IPv6 localhost issue
        String clientIP = (ipAddress != null && !ipAddress.contains(":")) ? ipAddress : "127.0.0.1";
        vnp_Params.put("vnp_IpAddr", clientIP);
        
        // Create date và Expire date - GMT+7 timezone (CHÍNH XÁC theo code mẫu VNPay)
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        // Expire date - 15 phút sau (VNPay requirement)
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        
        // Build hash data và query string theo VNPay format
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data - Theo code mẫu VNPay chính thức: ENCODE cả fieldName và fieldValue
                hashData.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                // Build query - Theo VNPay: ENCODE cả fieldName và fieldValue
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        
        // Generate secure hash theo VNPay
        String vnp_SecureHash = hmacSHA512(VNPayConfig.VNP_HASH_SECRET, hashData.toString());
        
        // Build final payment URL
        String paymentUrl = VNPayConfig.VNP_PAY_URL + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;
        
        // Debug logging
        System.out.println("=== VNPay URL Generation (Official Standard) ===");
        System.out.println("Secret Key: " + VNPayConfig.VNP_HASH_SECRET);
        System.out.println("Hash data: " + hashData.toString());
        System.out.println("Secure hash: " + vnp_SecureHash);
        System.out.println("Payment URL: " + paymentUrl);
        System.out.println("==============================================");
        
        return paymentUrl;
    }
    
    /**
     * Hash all fields cho Return URL và IPN validation - Theo code mẫu chính thức VNPay
     * Fields đã được encode khi put vào map, nên hashAllFields chỉ concatenate
     */
    public String hashAllFields(Map<String, String> fields) {
        System.out.println("=== VNPayService.hashAllFields START ===");
        System.out.println("Input fields: " + fields);
        
        // Build hash data từ sorted fields - theo code mẫu VNPay Return/IPN
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Fields đã encode, chỉ concatenate
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(fieldValue);
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }
        
        String result = hmacSHA512(VNPayConfig.VNP_HASH_SECRET, hashData.toString());
        System.out.println("Hash data: " + hashData.toString());
        System.out.println("Generated signature: " + result);
        System.out.println("=== VNPayService.hashAllFields END ===");
        
        return result;
    }
    

    public boolean verifyPayment(HttpServletRequest request) {
        System.out.println("=== VNPayService.verifyPayment START ===");
        
        Map<String, String> params = new TreeMap<>();
        
        // Get all parameters except vnp_SecureHash
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if (!paramName.equals("vnp_SecureHash")) {
                params.put(paramName, request.getParameter(paramName));
            }
        }
        
        System.out.println("Parameters for verification: " + params);
        
        // Build query string
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (query.length() > 0) {
                query.append("&");
            }
            try {
                query.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            } catch (UnsupportedEncodingException e) {
                System.out.println("ERROR: Encoding failed: " + e.getMessage());
                return false;
            }
        }
        
        // Generate expected hash
        String expectedHash = hmacSHA512(VNPayConfig.VNP_HASH_SECRET, query.toString());
        String receivedHash = request.getParameter("vnp_SecureHash");
        
        System.out.println("Query string: " + query.toString());
        System.out.println("Expected hash: " + expectedHash);
        System.out.println("Received hash: " + receivedHash);
        boolean isValid = expectedHash.equals(receivedHash);
        System.out.println("Signature valid: " + isValid);
        System.out.println("=== VNPayService.verifyPayment END ===");
        
        return isValid;
    }
    
    /**
     * Process successful payment return
     */
    public boolean processPaymentReturn(HttpServletRequest request) {
        // Verify signature first
        if (!verifyPayment(request)) {
            System.out.println("VNPay signature verification failed");
            return false;
        }
        
        // Get payment info
        String vnp_TxnRef = request.getParameter("vnp_TxnRef");
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String vnp_TransactionStatus = request.getParameter("vnp_TransactionStatus");
        String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
        String vnp_PayDate = request.getParameter("vnp_PayDate");
        String vnp_BankCode = request.getParameter("vnp_BankCode");
        
        // Check if payment is successful
        boolean isSuccess = VNPayConfig.SUCCESS_CODE.equals(vnp_ResponseCode) && 
                           VNPayConfig.TRANSACTION_SUCCESS.equals(vnp_TransactionStatus);
        
        if (isSuccess) {
            // Update payment status to SUCCESS using DAO
            try {
                vnpayPaymentDAO.updatePaymentStatus(vnp_TxnRef, 1, // 1 = SUCCESS
                                                   vnp_TransactionNo, vnp_ResponseCode, 
                                                   vnp_TransactionStatus, vnp_PayDate, vnp_BankCode);
                System.out.println("Payment updated successfully for txnRef: " + vnp_TxnRef);
            } catch (Exception e) {
                System.err.println("Failed to update payment status: " + e.getMessage());
            }
            
            System.out.println("VNPay payment successful: " + vnp_TxnRef);
            return true;
        } else {
            // Update payment status to FAILED using DAO
            try {
                vnpayPaymentDAO.updatePaymentStatus(vnp_TxnRef, 2, // 2 = FAILED
                                                   vnp_TransactionNo, vnp_ResponseCode, 
                                                   vnp_TransactionStatus, vnp_PayDate, vnp_BankCode);
                System.out.println("Payment marked as failed for txnRef: " + vnp_TxnRef);
            } catch (Exception e) {
                System.err.println("Failed to update payment status: " + e.getMessage());
            }
            // vnpayPaymentDAO.updatePaymentFailed(vnp_TxnRef, vnp_ResponseCode, vnp_TransactionStatus);
            
            System.out.println("VNPay payment failed: " + vnp_TxnRef + ", Code: " + vnp_ResponseCode);
            return false;
        }
    }
    
    /**
     * Get payment by transaction reference
     */
    public VNPayPaymentDTO getPaymentByTxnRef(String vnp_TxnRef) {
        try {
            return vnpayPaymentDAO.getPaymentByTxnRef(vnp_TxnRef);
        } catch (Exception e) {
            System.out.println("Error fetching payment by txnRef: " + e.getMessage());
            return null;
        }
    }

   
    // Helper methods
    
    /**
     * Generate unique transaction reference
     */
    private String generateTxnRef() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = formatter.format(new Date());
        int random = new Random().nextInt(1000);
        return "VNP" + timestamp + String.format("%03d", random);
    }
    
    /**
     * HMAC SHA512 hash function - Theo code mẫu chính thức VNPay
     */
    public static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA512", e);
        }
    }

    /**
     * Process VNPay payment response and update status
     */
    public boolean processPaymentResponse(Map<String, String> vnpParams) {
        try {
            System.out.println("=== VNPayService.processPaymentResponse START ===");
            System.out.println("Input params: " + vnpParams);
            
            String vnp_TxnRef = vnpParams.get("vnp_TxnRef");
            String vnp_ResponseCode = vnpParams.get("vnp_ResponseCode");
            String vnp_TransactionNo = vnpParams.get("vnp_TransactionNo");
            String vnp_TransactionStatus = vnpParams.get("vnp_TransactionStatus");
            String vnp_PayDate = vnpParams.get("vnp_PayDate");
            String vnp_BankCode = vnpParams.get("vnp_BankCode");
            
            System.out.println("Extracted params: txnRef=" + vnp_TxnRef + ", responseCode=" + vnp_ResponseCode + ", transactionStatus=" + vnp_TransactionStatus);
            
            // Determine status based on response code
            int status;
            if ("00".equals(vnp_ResponseCode)) {
                status = 1; // SUCCESS
                System.out.println("Payment SUCCESS - setting status to 1");
            } else {
                status = 2; // FAILED
                System.out.println("Payment FAILED - setting status to 2");
            }
            
            // Update payment status in database
            System.out.println("Calling DAO updatePaymentStatus...");
            boolean result = vnpayPaymentDAO.updatePaymentStatus(vnp_TxnRef, status, vnp_TransactionNo, 
                                                     vnp_ResponseCode, vnp_TransactionStatus, 
                                                     vnp_PayDate, vnp_BankCode);
            System.out.println("DAO updatePaymentStatus result: " + result);
            System.out.println("=== VNPayService.processPaymentResponse END ===");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verify VNPay signature
     */
    public boolean verifyPaymentSignature(Map<String, String> vnpParams) {
        String vnp_SecureHash = vnpParams.get("vnp_SecureHash");
        vnpParams.remove("vnp_SecureHash");
        vnpParams.remove("vnp_SecureHashType");
        
        // Build hash data
        StringBuilder hashData = new StringBuilder();
        vnpParams.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                if (hashData.length() > 0) {
                    hashData.append("&");
                }
                hashData.append(entry.getKey()).append("=").append(entry.getValue());
            });
        
        String signValue = hmacSHA512(VNPayConfig.VNP_HASH_SECRET, hashData.toString());
        return signValue.equals(vnp_SecureHash);
    }

    /**
     * Handle VNPay callback - update payment status (Subscription will be updated by DB trigger)
     */
    public String handleVnPayCallback(Map<String, String> vnpParams) {
        try {
            System.out.println("=== VNPay Callback Handler Started ===");
            System.out.println("Received params: " + vnpParams);
            
            // Verify signature
            if (!verifyPaymentSignature(new HashMap<>(vnpParams))) {
                System.out.println("❌ Invalid signature");
                return "Invalid signature";
            }
            System.out.println("✅ Signature verified");

            String txnRef = vnpParams.get("vnp_TxnRef");
            String responseCode = vnpParams.get("vnp_ResponseCode");
            String transactionStatus = vnpParams.get("vnp_TransactionStatus");
            
            System.out.println("TxnRef: " + txnRef + ", ResponseCode: " + responseCode + ", TransactionStatus: " + transactionStatus);

            // Find payment by txnRef
            VNPayPaymentDTO payment = vnpayPaymentDAO.getPaymentByTxnRef(txnRef);
            if (payment == null) {
                System.out.println("❌ Payment not found for txnRef: " + txnRef);
                return "Payment not found";
            }
            System.out.println("✅ Payment found: userID=" + payment.getUserID() + ", total=" + payment.getTotal() + ", status=" + payment.getStatus());

            // Idempotency: if already SUCCESS, don't apply again
            if (payment.getStatus() == 1) {
                System.out.println("⚠️ Payment already processed (status=1), skipping");
                return "00"; // Already processed
            }

            // Determine new status
            int status = "00".equals(responseCode) && "00".equals(transactionStatus) ? 1 : 2; // 1 success, 2 failed
            System.out.println("New status determined: " + status);

            // Update payment status (DB trigger will handle Subscription update automatically)
            boolean updated = vnpayPaymentDAO.updatePaymentStatus(txnRef, status, vnpParams.get("vnp_TransactionNo"),
                responseCode, transactionStatus, vnpParams.get("vnp_PayDate"), vnpParams.get("vnp_BankCode"));
            if (!updated) {
                System.out.println("❌ Failed to update payment status");
                return "Failed to update payment";
            }
            System.out.println("✅ Payment status updated");
            
            if (status == 1 && payment.getTotal() != null && payment.getTotal() > 0 && payment.getUserID() != null) {
                System.out.println("✅ DB Trigger will automatically update Subscription table for userID=" + payment.getUserID() + ", total=" + payment.getTotal());
            }

            System.out.println("=== VNPay Callback Handler Completed ===");
            return "00"; // Success

        } catch (Exception e) {
            System.out.println("❌ Exception in handleVnPayCallback: " + e.getMessage());
            e.printStackTrace();
            return "Error processing callback: " + e.getMessage();
        }
    }
}