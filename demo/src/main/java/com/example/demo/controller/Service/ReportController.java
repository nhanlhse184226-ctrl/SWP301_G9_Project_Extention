package com.example.demo.controller.Service;

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
