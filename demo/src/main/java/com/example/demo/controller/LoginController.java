package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
     // Ánh xạ GET cho đường dẫn gốc "/test"
    @GetMapping("/login")
    public String test() {
        return "Login Controller is working!";
    }

}
