package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServicePackController {
    // Ánh xạ GET cho đường dẫn gốc "/test"
    @GetMapping("/servicePack")
    public String test() {
        return "Service Pack Controller is working!";
    }
}
