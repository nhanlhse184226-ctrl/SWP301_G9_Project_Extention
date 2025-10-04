
package com.example.demo.controller.Service;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.example.demo.dao.UserDAO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.ApiResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author hd
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {

    protected ResponseEntity<ApiResponse<Object>> processRequest(String Email, String Password, HttpServletRequest request) {
        UserDTO loginUser = null;

        try {
            UserDAO dao = new UserDAO();
            loginUser = dao.checkLogin(Email, Password);

            // Kiểm tra login thành công chưa
            if (loginUser != null) {
                // Login thành công - lưu vào session
                HttpSession session = request.getSession(true);
                session.setAttribute("LOGIN_USER", loginUser);
                session.setMaxInactiveInterval(30 * 60); // 30 phút
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
            @RequestParam String Password,
            HttpServletRequest request) {
        return processRequest(Email, Password, request);
    }

    @GetMapping("/loginUser")
    protected ResponseEntity<ApiResponse<Object>> getLoginUser(HttpServletRequest request) {
        try {
            // Lấy session hiện tại (không tạo mới nếu chưa có)
            HttpSession session = request.getSession(false);
            
            if (session != null) {
                // Lấy user từ session
                UserDTO loginUser = (UserDTO) session.getAttribute("LOGIN_USER");
                
                if (loginUser != null) {
                    // Có user trong session
                    return ResponseEntity.ok(ApiResponse.success("Get login user successful", loginUser));
                } else {
                    // Session tồn tại nhưng không có user
                    return ResponseEntity.badRequest().body(ApiResponse.error("No user logged in"));
                }
            } else {
                // Không có session
                return ResponseEntity.badRequest().body(ApiResponse.error("No active session"));
            }
            
        } catch (Exception e) {
            System.out.println("Error at getLoginUser: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    @PostMapping("/logout")
    protected ResponseEntity<ApiResponse<Object>> logout(HttpServletRequest request) {
        try {
            // Lấy session hiện tại (không tạo mới nếu chưa có)
            HttpSession session = request.getSession(false);
            
            if (session != null) {
                // Xoá user khỏi session
                session.removeAttribute("LOGIN_USER");
                // Tuỳ chọn: huỷ session hoàn toàn
                session.invalidate();
                return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
            } else {
                // Không có session
                return ResponseEntity.badRequest().body(ApiResponse.error("No active session to logout"));
            }
            
        } catch (Exception e) {
            System.out.println("Error at logout: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }
}