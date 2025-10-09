package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.UserDAO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.UserDTO;

@RestController
@RequestMapping("/api")
public class UserController {
    
    // API lấy danh sách driver
    @GetMapping("/user/listDriver")
    public ResponseEntity<ApiResponse<Object>> getListDriver() {
        try {
            UserDAO dao = new UserDAO();
            List<UserDTO> listDriver = dao.getListDriver();
            
            if (listDriver != null && !listDriver.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Get driver list successful", listDriver));
            } else {
                return ResponseEntity.ok(ApiResponse.success("No drivers found", listDriver));
            }
            
        } catch (Exception e) {
            System.out.println("Error at UserController - getListDriver: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }
    
    // API lấy danh sách staff
    @GetMapping("/user/listStaff")
    public ResponseEntity<ApiResponse<Object>> getListStaff() {
        try {
            UserDAO dao = new UserDAO();
            List<UserDTO> listStaff = dao.getListStaff();

            if (listStaff != null && !listStaff.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Get staff list successful", listStaff));
            } else {
                return ResponseEntity.ok(ApiResponse.success("No staff found", listStaff));
            }
            
        } catch (Exception e) {
            System.out.println("Error at UserController - getListStaff: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }
    
    // API thêm user mới
    @PostMapping("/user/add")
    public ResponseEntity<ApiResponse<Object>> addUser(
            @RequestParam String Name,
            @RequestParam String Email,
            @RequestParam String Password,
            @RequestParam String phone,
            @RequestParam String roleID) {
        try {
            UserDAO dao = new UserDAO();
            
            // Validate và convert phone
            long phoneInt;
            try {
                phoneInt = Long.parseLong(phone);
                if (phoneInt <= 0) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Phone number must be positive"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid phone number format"));
            }
            
            // Validate và convert roleID
            int roleIDInt;
            try {
                roleIDInt = Integer.parseInt(roleID);
                if (roleIDInt <= 0) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Role ID must be positive"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid role ID format"));
            }
            
            // Validate basic fields
            if (Name == null || Name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Name cannot be empty"));
            }
            if (Email == null || Email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Email cannot be empty"));
            }
            if (Password == null || Password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Password cannot be empty"));
            }
            
            // Kiểm tra email trùng
            if (dao.checkDuplicateEmail(Email)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Email already exists"));
            }
            
            // Kiểm tra phone trùng
            if (dao.checkDuplicatePhone(phoneInt)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Phone number already exists"));
            }

            UserDTO newUser = new UserDTO(0, Name, Email, Password, phoneInt, roleIDInt);

            // Thêm user vào database
            boolean result = dao.create(newUser);

            if (result) {
                return ResponseEntity.ok(ApiResponse.success("User added successfully", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to add user"));
            }
            
        } catch (Exception e) {
            System.out.println("Error at UserController - addUser: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }
    
    // API cập nhật thông tin user
    @PutMapping("/user/update")
    public ResponseEntity<ApiResponse<Object>> updateUser(
            @RequestParam String userID,
            @RequestParam String Name,
            @RequestParam String Email,
            @RequestParam String Password,
            @RequestParam String phone,
            @RequestParam String roleID) {
        try {
            UserDAO dao = new UserDAO();
            
            // Validate và convert userID
            int userIDInt;
            try {
                userIDInt = Integer.parseInt(userID);
                if (userIDInt <= 0) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("User ID must be positive"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID format"));
            }
            
            // Validate và convert phone
            int phoneInt;
            try {
                phoneInt = Integer.parseInt(phone);
                if (phoneInt <= 0) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Phone number must be positive"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid phone number format"));
            }
            
            // Validate và convert roleID
            int roleIDInt;
            try {
                roleIDInt = Integer.parseInt(roleID);
                if (roleIDInt <= 0) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Role ID must be positive"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid role ID format"));
            }
            
            // Validate basic fields
            if (Name == null || Name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Name cannot be empty"));
            }
            if (Email == null || Email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Email cannot be empty"));
            }
            if (Password == null || Password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Password cannot be empty"));
            }
            
            // Tạo UserDTO với thông tin mới
            UserDTO updateUser = new UserDTO(userIDInt, Name, Email, Password, phoneInt, roleIDInt);
            
            // Cập nhật user trong database
            boolean result = dao.update(updateUser);
            
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("User updated successfully", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update user or user not found"));
            }
            
        } catch (Exception e) {
            System.out.println("Error at UserController - updateUser: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }
    
    // API xóa user
    @DeleteMapping("/user/delete")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@RequestParam String userID) {
        try {
            UserDAO dao = new UserDAO();
            
            // Validate và convert userID
            int userIDInt;
            try {
                userIDInt = Integer.parseInt(userID);
                if (userIDInt <= 0) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("User ID must be positive"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID format"));
            }
            
            // Tạo UserDTO với userID để delete
            UserDTO deleteUser = new UserDTO();
            deleteUser.setUserID(userIDInt);
            
            // Xóa user khỏi database
            boolean result = dao.delete(deleteUser);
            
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete user or user not found"));
            }
            
        } catch (Exception e) {
            System.out.println("Error at UserController - deleteUser: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }
}