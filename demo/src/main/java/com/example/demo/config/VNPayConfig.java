package com.example.demo.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {
    
    // Base URL - Using localhost (NO NGROK!)
    public static final String BASE_URL = "http://localhost:8080";
    
    public static final String VNP_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String VNP_RETURN_URL = BASE_URL + "/vnpay/return";
    public static final String VNP_IPN_URL = BASE_URL + "/vnpay/ipn";
    public static final String VNP_TMN_CODE = "8I1PNFUT";
    public static final String VNP_HASH_SECRET = "7UJW8OWB93IDXHQOH4MR4DJ9YMVBECXI";
    
    public static final String VNP_VERSION = "2.1.0";
    public static final String VNP_COMMAND = "pay";
    public static final String VNP_ORDER_TYPE = "other";
    public static final String VNP_CURRENCY_CODE = "VND";
    public static final String VNP_LOCALE = "vn";
    
    public static final String SUCCESS_CODE = "00";
    public static final String TRANSACTION_SUCCESS = "00";
    
    public static final String VNPAY_QR = "VNPAYQR";
    public static final String VN_BANK = "VNBANK";
    public static final String INT_CARD = "INTCARD";
}