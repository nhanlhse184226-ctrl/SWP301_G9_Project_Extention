package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
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
            @RequestParam String roleID,
            @RequestParam String status) {
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

            // Validate và convert status
            int statusInt;
            try {
                statusInt = Integer.parseInt(status);
                if (statusInt < 0 || statusInt > 2) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Status must be 0, 1, or 2"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid status format"));
            }

            // Kiểm tra email trùng
            if (dao.checkDuplicateEmail(Email)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Email already exists"));
            }

            // Kiểm tra phone trùng
            if (dao.checkDuplicatePhone(phoneInt)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Phone number already exists"));
            }

            UserDTO newUser = new UserDTO(0, Name, Email, Password, phoneInt, roleIDInt, statusInt);

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

    // API cập nhật thông tin user (không bao gồm phone và password)
    @PutMapping("/user/update")

    public ResponseEntity<ApiResponse<Object>> updateUser(
            @RequestParam String userID,
            @RequestParam String Name,
            @RequestParam String Email,
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

            // Tạo UserDTO với thông tin mới (chỉ Name, Email, roleID)
            UserDTO updateUser = new UserDTO();
            updateUser.setUserID(userIDInt);
            updateUser.setName(Name);
            updateUser.setEmail(Email);
            updateUser.setRoleID(roleIDInt);

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

    // API cập nhật phone và password cho driver
    @PutMapping("/user/updateDriver")
    public ResponseEntity<ApiResponse<Object>> updateDriver(
            @RequestParam String userID,
            @RequestParam String phone,
            @RequestParam String password) {
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
            long phoneLong;
            try {
                phoneLong = Long.parseLong(phone);
                if (phoneLong <= 0) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Phone number must be positive"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid phone number format"));
            }

            // Validate password
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Password cannot be empty"));
            }

            // Cập nhật driver info trong database
            boolean result = dao.updateDriver(userIDInt, phoneLong, password);

            if (result) {
                return ResponseEntity.ok(ApiResponse.success("Driver information updated successfully", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to update driver information or user not found"));
            }

        } catch (Exception e) {
            System.out.println("Error at UserController - updateDriver: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    // API cập nhật trạng thái user
    @PutMapping("/user/updateStatus")
    public ResponseEntity<ApiResponse<Object>> updateUserStatus(@RequestParam String userID) {
        try {
            UserDAO dao = new UserDAO();
            int userIDInt;
            try {
                userIDInt = Integer.parseInt(userID);
                if (userIDInt <= 0) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("User ID must be positive"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID format"));
            }

            boolean result = dao.updateStatus(userIDInt);

            if (result) {
                return ResponseEntity.ok(ApiResponse.success("User status updated successfully", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to update user status or user not found"));
            }

        } catch (Exception e) {
            System.out.println("Error at UserController - deleteUser: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }
}