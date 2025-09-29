package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RatingController {
    // Ánh xạ GET cho đường dẫn gốc "/test"
    @GetMapping("/rating")
    public String test() {
        return "Rating Controller is working!";
    }
}
