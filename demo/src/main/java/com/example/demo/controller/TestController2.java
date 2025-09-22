package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController2 {
    // Ánh xạ GET cho đường dẫn gốc "/test"
    @GetMapping("/test2")
    public String test2() {
        return "Test Controller 2 is working! and git push is working 3";
    }
}
