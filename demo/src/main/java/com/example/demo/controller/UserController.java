package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.example.demo.dao.UserDAO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.ApiResponse;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
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
            @RequestParam int phone,
            @RequestParam int roleID) {
        try {
            UserDAO dao = new UserDAO();
            
            // Kiểm tra email trùng
            if (dao.checkDuplicateEmail(Email)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Email already exists"));
            }
            
            // Kiểm tra phone trùng
            if (dao.checkDuplicatePhone(phone)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Phone number already exists"));
            }

            UserDTO newUser = new UserDTO(0, Name, Email, Password, phone, roleID);

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
            @RequestParam int userID,
            @RequestParam String Name,
            @RequestParam String Email,
            @RequestParam String Password,
            @RequestParam int phone,
            @RequestParam int roleID) {
        try {
            UserDAO dao = new UserDAO();
            
            // Tạo UserDTO với thông tin mới
            UserDTO updateUser = new UserDTO(userID, Name, Email, Password, phone, roleID);
            
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
    public ResponseEntity<ApiResponse<Object>> deleteUser(@RequestParam int userID) {
        try {
            UserDAO dao = new UserDAO();
            
            // Tạo UserDTO với userID để delete
            UserDTO deleteUser = new UserDTO();
            deleteUser.setUserID(userID);
            
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