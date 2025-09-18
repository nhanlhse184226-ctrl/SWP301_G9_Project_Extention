package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    // Ánh xạ GET cho đường dẫn gốc "/"
    @GetMapping("/")
    public String hello() {
        return "Hello Spring Boot!";
    }
}
