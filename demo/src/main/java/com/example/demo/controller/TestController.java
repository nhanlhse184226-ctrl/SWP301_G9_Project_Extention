package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    // Ánh xạ GET cho đường dẫn gốc "/test"
    @GetMapping("/test")
    public String test() {
        return "Test Controller is working!";
    }
}
