package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {
     // Ánh xạ GET cho đường dẫn gốc "/test"
    @GetMapping("/payment")
    public String test() {
        return "Payment Controller is working!";
    }

}
