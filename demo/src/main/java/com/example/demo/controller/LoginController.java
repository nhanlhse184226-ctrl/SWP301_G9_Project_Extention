
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

    protected ResponseEntity<ApiResponse<Object>> processRequest(String Email, String Password) {
        UserDTO loginUser = null;

        try {
            UserDAO dao = new UserDAO();
            loginUser = dao.checkLogin(Email, Password);

            // Kiểm tra login thành công chưa
            if (loginUser != null) {
                // Login thành công - trả về user info
                return ResponseEntity.ok(ApiResponse.success("Login successful", loginUser));
            } else {
                // Login thất bại - sai user/password
                return ResponseEntity.badRequest().body(ApiResponse.error("Incorrect Email or Password"));
            }

        } catch (Exception e) {
            System.out.println("Error at LoginController: " + e.toString());
            // Lỗi hệ thống
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    @PostMapping("/login")
    protected ResponseEntity<ApiResponse<Object>> doPost(
            @RequestParam String Email,
            @RequestParam String Password) {
        return processRequest(Email, Password);
    }

    public String getServletInfo() {
        return "Short description";
    }
}
