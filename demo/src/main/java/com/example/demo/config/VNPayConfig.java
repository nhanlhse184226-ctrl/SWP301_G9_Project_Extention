package com.example.demo.config;

import org.springframework.context.annotation.Configuration;

/**
 * VNPay Configuration - Theo hướng dẫn chính thức VNPay
 * URL: https://sandbox.vnpayment.vn/apis/docs/huong-dan-tich-hop/
 */
@Configuration
public class VNPayConfig {
    
    // VNPay Sandbox Environment - Test với DEMO credentials từ documentation
    public static final String VNP_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String VNP_RETURN_URL = "http://localhost:8080/vnpay/return";
    public static final String VNP_IPN_URL = "http://localhost:8080/vnpay/ipn"; // IPN URL cần gửi cho VNPay
    public static final String VNP_TMN_CODE = "8I1PNFUT"; // DEMO Terminal ID từ VNPay documentation
    public static final String VNP_HASH_SECRET = "7UJW8OWB93IDXHQOH4MR4DJ9YMVBECXI"; // DEMO Secret Key từ VNPay documentation
    
    // VNPay API Constants - Theo documentation
    public static final String VNP_VERSION = "2.1.0";
    public static final String VNP_COMMAND = "pay";
    public static final String VNP_ORDER_TYPE = "other"; // Mã danh mục hàng hóa
    public static final String VNP_CURRENCY_CODE = "VND";
    public static final String VNP_LOCALE = "vn"; // Tiếng Việt
    
    // Response Codes theo bảng mã lỗi VNPay
    public static final String SUCCESS_CODE = "00";
    public static final String TRANSACTION_SUCCESS = "00";
    
    // VNPay Bank Codes - Theo documentation
    public static final String VNPAY_QR = "VNPAYQR"; // Thanh toán quét mã QR
    public static final String VN_BANK = "VNBANK"; // Thẻ ATM - Tài khoản ngân hàng nội địa
    public static final String INT_CARD = "INTCARD"; // Thẻ thanh toán quốc tế
}