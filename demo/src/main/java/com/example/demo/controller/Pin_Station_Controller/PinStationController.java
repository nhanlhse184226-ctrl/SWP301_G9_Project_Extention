package com.example.demo.controller.Pin_Station_Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PinStationController {
    // Ánh xạ GET cho đường dẫn gốc "/test"
    @GetMapping("/pinStation")
    public String test() {
        return "Pin Station Controller is working!";
    }
}
