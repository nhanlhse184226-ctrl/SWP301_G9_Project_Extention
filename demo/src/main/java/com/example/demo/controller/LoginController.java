/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.example.demo.dao.UserDAO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.ApiResponse;

/**
 *
 * @author hd
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {

    protected ResponseEntity<ApiResponse<Object>> processRequest(String userID, String password) {
        UserDTO loginUser = null;

        try {
            UserDAO dao = new UserDAO();
            loginUser = dao.checkLogin(userID, password);

            // Kiểm tra login thành công chưa
            if (loginUser != null) {
                // Login thành công - trả về user info
                return ResponseEntity.ok(ApiResponse.success("Login successful", loginUser));
            } else {
                // Login thất bại - sai user/password
                return ResponseEntity.badRequest().body(ApiResponse.error("Incorrect UserId or password"));
            }

        } catch (Exception e) {
            System.out.println("Error at LoginController: " + e.toString());
            // Lỗi hệ thống
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    @PostMapping("/login")
    protected ResponseEntity<ApiResponse<Object>> doPost(
            @RequestParam String userID,
            @RequestParam String password) {
        return processRequest(userID, password);
    }

    public String getServletInfo() {
        return "Short description";
    }
}
