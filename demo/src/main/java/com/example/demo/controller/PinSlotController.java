package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PinSlotController {
    // Ánh xạ GET cho đường dẫn gốc "/test"
    @GetMapping("/pinSlot")
    public String test() {
        return "Pin Slot Controller is working!";
    }
}
