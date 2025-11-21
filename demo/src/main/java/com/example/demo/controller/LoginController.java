
package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.example.demo.dao.UserDAO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller xử lý đăng nhập người dùng
 * Cung cấp API để xác thực thông tin đăng nhập
 * @author hd
 */
@RestController
@RequestMapping("/api")
public class LoginController {
    /**
     * Xử lý logic đăng nhập chính
     * @param Email - Email người dùng nhập vào
     * @param Password - Mật khẩu người dùng nhập vào
     * @param request - HTTP request object
     * @return ResponseEntity chứa kết quả đăng nhập
     */
    protected ResponseEntity<ApiResponse<Object>> processRequest(String Email, String Password,
            HttpServletRequest request) {
        // Khởi tạo biến để lưu thông tin người dùng sau khi đăng nhập
        UserDTO loginUser = null;

        try {
            // Tạo đối tượng DAO để truy cập database
            UserDAO dao = new UserDAO();
            
            // Gọi method kiểm tra thông tin đăng nhập trong database
            loginUser = dao.checkLogin(Email, Password);

            // Kiểm tra xem đăng nhập có thành công hay không
            if (loginUser != null) {
                // Đăng nhập thành công - trả về thông tin user
                return ResponseEntity.ok(ApiResponse.success("Login successful", loginUser));
            } else {
                // Đăng nhập thất bại - email hoặc password không đúng
                return ResponseEntity.badRequest().body(ApiResponse.error("Incorrect Email or Password"));
            }

        } catch (Exception e) {
            // In thông tin lỗi ra console để debug
            System.out.println("Error at LoginController: " + e.toString());
            // Trả về lỗi hệ thống cho client
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * API endpoint cho đăng nhập
     * Nhận POST request từ client với email và password
     * @param Email - Email người dùng (bắt buộc)
     * @param Password - Mật khẩu người dùng (bắt buộc) 
     * @param request - HTTP request object
     * @return ResponseEntity chứa kết quả đăng nhập
     */
    @PostMapping("/login")
    protected ResponseEntity<ApiResponse<Object>> doPost(
            @RequestParam String Email,
            @RequestParam String Password,
            HttpServletRequest request) {
        // Gọi method xử lý logic đăng nhập chính
        return processRequest(Email, Password, request);
    }

}