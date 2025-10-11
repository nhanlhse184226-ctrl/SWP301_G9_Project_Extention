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
    
    /**
     * Tạo payment với QR code - Method mới cho frontend
     */
    public VNPayPaymentResponseDTO createPaymentWithQR(Integer userID, Integer servicePackID, 
                                                      Double amountVND, String orderInfo, String ipAddress) 
            throws UnsupportedEncodingException {
        
        // Tạo payment record
        String txnRef = createPayment(userID, servicePackID, amountVND, orderInfo);
        
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
     * Tạo payment record và trả về transaction reference
     */
    public String createPayment(Integer userID, Integer servicePackID, Double amountVND, String orderInfo) {
        // Generate unique transaction reference
        String vnp_TxnRef = generateTxnRef();
        
        // Convert amount to VNPay format (VND * 100)
        Long vnp_Amount = Math.round(amountVND * 100);
        
        // TODO: Save to database using DAO
        // vnpayPaymentDAO.createPayment(userID, servicePackID, vnp_TxnRef, orderInfo, vnp_Amount);
        
        System.out.println("Created VNPay payment: " + vnp_TxnRef + " for amount: " + amountVND + " VND");
        
        return vnp_TxnRef;
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
        
        // Amount: Nhân với 100 để triệt tiêu phần thập phân (VNPay requirement)
        int amount = (int) (amountVND * 100);
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
        
        return hmacSHA512(VNPayConfig.VNP_HASH_SECRET, hashData.toString());
    }
    

    public boolean verifyPayment(HttpServletRequest request) {
        Map<String, String> params = new TreeMap<>();
        
        // Get all parameters except vnp_SecureHash
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if (!paramName.equals("vnp_SecureHash")) {
                params.put(paramName, request.getParameter(paramName));
            }
        }
        
        // Build query string
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (query.length() > 0) {
                query.append("&");
            }
            try {
                query.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            } catch (UnsupportedEncodingException e) {
                return false;
            }
        }
        
        // Generate expected hash
        String expectedHash = hmacSHA512(VNPayConfig.VNP_HASH_SECRET, query.toString());
        String receivedHash = request.getParameter("vnp_SecureHash");
        
        return expectedHash.equals(receivedHash);
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
            // TODO: Update database using DAO
            // vnpayPaymentDAO.updatePaymentSuccess(vnp_TxnRef, vnp_TransactionNo, vnp_ResponseCode, 
            //                                     vnp_TransactionStatus, vnp_PayDate, vnp_BankCode);
            
            System.out.println("VNPay payment successful: " + vnp_TxnRef);
            return true;
        } else {
            // TODO: Update database as failed
            // vnpayPaymentDAO.updatePaymentFailed(vnp_TxnRef, vnp_ResponseCode, vnp_TransactionStatus);
            
            System.out.println("VNPay payment failed: " + vnp_TxnRef + ", Code: " + vnp_ResponseCode);
            return false;
        }
    }
    
    /**
     * Get payment status by transaction reference
     */
    public VNPayPaymentDTO getPaymentByTxnRef(String vnp_TxnRef) {
        // TODO: Implement DAO call
        // return vnpayPaymentDAO.getPaymentByTxnRef(vnp_TxnRef);
        
        // Mock response for now
        VNPayPaymentDTO payment = new VNPayPaymentDTO();
        payment.setVnp_TxnRef(vnp_TxnRef);
        payment.setStatus("SUCCESS");
        return payment;
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
}