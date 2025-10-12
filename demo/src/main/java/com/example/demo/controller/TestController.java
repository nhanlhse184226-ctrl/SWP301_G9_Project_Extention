package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.VNPayService;

@RestController
public class TestController {

    @Autowired
    private VNPayService vnpayService;

    // Ánh xạ GET cho đường dẫn gốc "/test"
    @GetMapping("/test")
    public String test() {
        return "Test Controller is working!";
    }
    
    @GetMapping("/test/callback")
    public ResponseEntity<String> testCallback(@RequestParam String txnRef) {
        // Simulate successful VNPay callback
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionStatus", "00");
        params.put("vnp_TransactionNo", "123456789");
        params.put("vnp_PayDate", "20251012120000");
        params.put("vnp_BankCode", "NCB");
        
        String result = vnpayService.handleVnPayCallback(params);
        return ResponseEntity.ok("Callback result: " + result);
    }
}
