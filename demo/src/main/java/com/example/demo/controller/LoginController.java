package com.example.demo.controller;

import com.example.demo.dao.UserDAO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dbUnits.DBUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.sql.Connection;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Cho phép CORS từ Frontend
public class LoginController {
    
    private UserDAO userDAO = new UserDAO();
    
    // Test controller hoạt động
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        ApiResponse<String> response = ApiResponse.success("Login Controller is working!", "healthy");
        return ResponseEntity.ok(response);
    }
    
    // Test kết nối database
    @GetMapping("/test-db")
    public ResponseEntity<ApiResponse<String>> testDatabaseConnection() {
        try {
            Connection conn = DBUtils.getConnection();
            if (conn != null) {
                conn.close();
                ApiResponse<String> response = ApiResponse.success("Kết nối database thành công!", "SQL Server Connected");
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<String> response = ApiResponse.error("Kết nối database thất bại - Connection null!");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.error("Kết nối database thất bại", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Login API - nhận JSON từ Frontend
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDTO>> login(@RequestBody LoginRequest loginRequest) {
        try {
            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();
            
            // Validate input
            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                ApiResponse<UserDTO> response = ApiResponse.error("Username và password không được để trống");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check login
            UserDTO user = userDAO.checkLogin(username, password);
            
            if (user != null) {
                ApiResponse<UserDTO> response = ApiResponse.success("Đăng nhập thành công", user);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<UserDTO> response = ApiResponse.error("Sai tên đăng nhập hoặc mật khẩu");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            ApiResponse<UserDTO> response = ApiResponse.error("Lỗi hệ thống", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // Login API - method GET cho test nhanh (tương thích với Swagger)
    @GetMapping("/login-test")
    public ResponseEntity<ApiResponse<UserDTO>> loginTest(
            @RequestParam String username, 
            @RequestParam String password) {
        
        try {
            UserDTO user = userDAO.checkLogin(username, password);
            
            if (user != null) {
                ApiResponse<UserDTO> response = ApiResponse.success("Đăng nhập thành công", user);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<UserDTO> response = ApiResponse.error("Sai tên đăng nhập hoặc mật khẩu");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            ApiResponse<UserDTO> response = ApiResponse.error("Lỗi hệ thống", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
