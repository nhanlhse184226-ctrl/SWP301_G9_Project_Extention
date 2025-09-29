package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ModifyAccount {
     // Ánh xạ GET cho đường dẫn gốc "/test"
    @GetMapping("/modifyAccount")
    public String test() {
        return "Modify Account Controller is working!";
    }

}
