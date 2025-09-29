package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportController {
    // Ánh xạ GET cho đường dẫn gốc "/test"
    @GetMapping("/report")
    public String test() {
        return "Report Controller is working!";
    }
}
