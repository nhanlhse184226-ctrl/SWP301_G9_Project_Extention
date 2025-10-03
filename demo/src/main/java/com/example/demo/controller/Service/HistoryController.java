package com.example.demo.controller.Service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HistoryController {
   
    // Ánh xạ GET cho đường dẫn gốc "/test"
    @GetMapping("/history")
    public String test() {
        return "History Controller is workingiiii!";
    }

}
