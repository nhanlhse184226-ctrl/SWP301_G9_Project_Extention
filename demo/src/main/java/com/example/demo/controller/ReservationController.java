package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReservationController {
    // Ánh xạ GET cho đường dẫn gốc "/test"
    @GetMapping("/reservation")
    public String test() {
        return "Reservation Controller is working!";
    }
}
