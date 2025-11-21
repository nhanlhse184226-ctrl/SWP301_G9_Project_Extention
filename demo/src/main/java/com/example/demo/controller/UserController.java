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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller quản lý người dùng
 * Cung cấp API để quản lý drivers, staff và thông tin users
 */
@RestController
@RequestMapping("/api")
@Tag(name = "User Management", description = "APIs for managing users, drivers, and staff")
public class UserController {

    // Khởi tạo DAO để truy cập database
    private UserDAO userDAO = new UserDAO();

    /**
     * API lấy danh sách drivers
     * Trả về tất cả users có roleID = 1 (driver)
     * @return ResponseEntity chứa danh sách drivers
     */
    // API lấy danh sách driver
    @GetMapping("/user/listDriver")
    public ResponseEntity<ApiResponse<Object>> getListDriver() {
        try {
            // Khởi tạo DAO để truy cập database
            UserDAO dao = new UserDAO();
            
            // Gọi DAO để lấy danh sách drivers (roleID = 1)
            List<UserDTO> listDriver = dao.getListDriver();

            // Kiểm tra kết quả và trả về response tương ứng
            if (listDriver != null && !listDriver.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Get driver list successful", listDriver));
            } else {
                return ResponseEntity.ok(ApiResponse.success("No drivers found", listDriver));
            }

        } catch (Exception e) {
            // Xử lý lỗi và in ra console
            System.out.println("Error at UserController - getListDriver: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * API lấy danh sách staff
     * Trả về tất cả users có roleID = 2 (staff)
     * @return ResponseEntity chứa danh sách staff
     */
    // API lấy danh sách staff
    @GetMapping("/user/listStaff")
    public ResponseEntity<ApiResponse<Object>> getListStaff() {
        try {
            // Khởi tạo DAO để truy cập database
            UserDAO dao = new UserDAO();
            
            // Gọi DAO để lấy danh sách staff (roleID = 2)
            List<UserDTO> listStaff = dao.getListStaff();

            // Kiểm tra kết quả và trả về response tương ứng
            if (listStaff != null && !listStaff.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Get staff list successful", listStaff));
            } else {
                return ResponseEntity.ok(ApiResponse.success("No staff found", listStaff));
            }

        } catch (Exception e) {
            // Xử lý lỗi và in ra console
            System.out.println("Error at UserController - getListStaff: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * API tạo user mới vào hệ thống
     * Admin có thể tạo users với các role khác nhau (driver/staff/admin)
     * @param Name - Tên user (bắt buộc, không được rỗng)
     * @param Email - Email user (bắt buộc, không được trùng)
     * @param Password - Mật khẩu (bắt buộc, không được rỗng)
     * @param phone - Số điện thoại (bắt buộc, không được trùng)
     * @param roleID - Vai trò: 1=Driver, 2=Staff, 3=Admin
     * @param status - Trạng thái: 0=Inactive, 1=Active, 2=Suspended
     * @return ResponseEntity chứa kết quả tạo user
     */
    // API thêm user mới
    @PostMapping("/user/add")
    @Operation(summary = "Add new user", description = "Create a new user with validation for unique email and phone number")
    public ResponseEntity<ApiResponse<Object>> addUser(
            @Parameter(description = "User full name", required = true) @RequestParam String Name,
            @Parameter(description = "User email (must be unique)", required = true) @RequestParam String Email,
            @Parameter(description = "User password", required = true) @RequestParam String Password,
            @Parameter(description = "User phone number (must be unique)", required = true) @RequestParam String phone,
            @Parameter(description = "Role ID: 1=Driver, 2=Staff, 3=Admin", required = true) @RequestParam String roleID,
            @Parameter(description = "User status: 0=Inactive, 1=Active, 2=Suspended", required = true) @RequestParam String status) {
        try {
            // Khởi tạo DAO để truy cập database
            UserDAO dao = new UserDAO();

            // Validate và convert phone number sang long
            long phoneInt;
            try {
                phoneInt = Long.parseLong(phone);
                if (phoneInt <= 0) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Phone number must be positive"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid phone number format"));
            }

            // Validate và convert roleID sang int
            int roleIDInt;
            try {
                roleIDInt = Integer.parseInt(roleID);
                if (roleIDInt <= 0 || roleIDInt > 3) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Role ID must be 1 (Driver), 2 (Staff), or 3 (Admin)"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid role ID format"));
            }

            // Validate các trường bắt buộc không được rỗng
            if (Name == null || Name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Name cannot be empty"));
            }
            if (Email == null || Email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Email cannot be empty"));
            }
            if (Password == null || Password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Password cannot be empty"));
            }

            // Validate và convert status sang int
            int statusInt;
            try {
                statusInt = Integer.parseInt(status);
                if (statusInt < 0 || statusInt > 2) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Status must be 0 (Inactive), 1 (Active), or 2 (Suspended)"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid status format"));
            }

            // Kiểm tra email đã tồn tại trong hệ thống chưa
            if (dao.checkDuplicateEmail(Email)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Email already exists"));
            }

            // Kiểm tra phone đã tồn tại trong hệ thống chưa
            if (dao.checkDuplicatePhone(phoneInt)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Phone number already exists"));
            }

            // Tạo đối tượng UserDTO với thông tin đã validate
            UserDTO newUser = new UserDTO(0, Name, Email, Password, phoneInt, roleIDInt, statusInt);

            // Gọi DAO để lưu user vào database
            boolean result = dao.create(newUser);

            // Kiểm tra kết quả tạo user
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("User added successfully", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to add user"));
            }

        } catch (Exception e) {
            // Xử lý lỗi và in ra console để debug
            System.out.println("Error at UserController - addUser: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * API cập nhật thông tin cơ bản của user
     * Chỉ cập nhật Name, Email, và Role (không sửa phone/password)
     * @param userID - ID của user cần cập nhật (bắt buộc > 0)
     * @param Name - Tên mới của user (bắt buộc, không được rỗng)
     * @param Email - Email mới của user (bắt buộc, không được rỗng)
     * @param roleID - Role mới: 1=Driver, 2=Staff, 3=Admin
     * @return ResponseEntity chứa kết quả cập nhật
     */
    // API cập nhật thông tin user (không bao gồm phone và password)
    @PutMapping("/user/update")
    @Operation(summary = "Update user basic info", description = "Update user's name, email, and role. Does not update phone or password.")
    public ResponseEntity<ApiResponse<Object>> updateUser(
            @Parameter(description = "User ID to update", required = true) @RequestParam String userID,
            @Parameter(description = "New user name", required = true) @RequestParam String Name,
            @Parameter(description = "New user email", required = true) @RequestParam String Email,
            @Parameter(description = "New role ID: 1=Driver, 2=Staff, 3=Admin", required = true) @RequestParam String roleID) {
        try {
            // Khởi tạo DAO để truy cập database
            UserDAO dao = new UserDAO();

            // Validate và convert userID sang int
            int userIDInt;
            try {
                userIDInt = Integer.parseInt(userID);
                if (userIDInt <= 0) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("User ID must be positive"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID format"));
            }

            // Validate và convert roleID sang int
            int roleIDInt;
            try {
                roleIDInt = Integer.parseInt(roleID);
                if (roleIDInt <= 0 || roleIDInt > 3) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Role ID must be 1 (Driver), 2 (Staff), or 3 (Admin)"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid role ID format"));
            }

            // Validate các trường bắt buộc
            if (Name == null || Name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Name cannot be empty"));
            }
            if (Email == null || Email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Email cannot be empty"));
            }

            // Tạo UserDTO với thông tin mới cần cập nhật
            // Chỉ cập nhật Name, Email, roleID (giữ nguyên phone và password)
            UserDTO updateUser = new UserDTO();
            updateUser.setUserID(userIDInt);
            updateUser.setName(Name);
            updateUser.setEmail(Email);
            updateUser.setRoleID(roleIDInt);

            // Gọi DAO để cập nhật thông tin user trong database
            boolean result = dao.update(updateUser);

            // Kiểm tra kết quả cập nhật
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("User updated successfully", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update user or user not found"));
            }

        } catch (Exception e) {
            // Xử lý lỗi và in ra console để debug
            System.out.println("Error at UserController - updateUser: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * API cập nhật thông tin nhạy cảm cho driver
     * Chỉ cập nhật phone và password (dành riêng cho drivers)
     * @param userID - ID của driver cần cập nhật (bắt buộc > 0)
     * @param phone - Số điện thoại mới (bắt buộc > 0)
     * @param password - Mật khẩu mới (bắt buộc, không được rỗng)
     * @return ResponseEntity chứa kết quả cập nhật driver
     */
    // API cập nhật phone và password cho driver
    @PutMapping("/user/updateDriver")
    @Operation(summary = "Update driver credentials", description = "Update driver's phone number and password (sensitive information)")
    public ResponseEntity<ApiResponse<Object>> updateDriver(
            @Parameter(description = "Driver user ID", required = true) @RequestParam String userID,
            @Parameter(description = "New phone number", required = true) @RequestParam String phone,
            @Parameter(description = "New password", required = true) @RequestParam String password) {
        try {
            // Khởi tạo DAO để truy cập database
            UserDAO dao = new UserDAO();

            // Validate và convert userID sang int
            int userIDInt;
            try {
                userIDInt = Integer.parseInt(userID);
                if (userIDInt <= 0) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("User ID must be positive"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID format"));
            }

            // Validate và convert phone sang long
            long phoneLong;
            try {
                phoneLong = Long.parseLong(phone);
                if (phoneLong <= 0) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Phone number must be positive"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid phone number format"));
            }

            // Validate password không được rỗng
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Password cannot be empty"));
            }

            // Gọi DAO để cập nhật phone và password cho driver
            // Chỉ cập nhật 2 trường này, giữ nguyên các thông tin khác
            boolean result = dao.updateDriver(userIDInt, phoneLong, password);

            // Kiểm tra kết quả cập nhật
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("Driver information updated successfully", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to update driver information or user not found"));
            }

        } catch (Exception e) {
            // Xử lý lỗi và in ra console để debug
            System.out.println("Error at UserController - updateDriver: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * API cập nhật trạng thái user (enable/disable)
     * Toggle trạng thái giữa active và inactive cho user
     * @param userID - ID của user cần thay đổi trạng thái (bắt buộc > 0)
     * @return ResponseEntity chứa kết quả cập nhật trạng thái
     */
    // API cập nhật trạng thái user
    @PutMapping("/user/updateStatus")
    @Operation(summary = "Update user status", description = "Toggle user status between active and inactive")
    public ResponseEntity<ApiResponse<Object>> updateUserStatus(
            @Parameter(description = "User ID to update status", required = true) @RequestParam String userID) {
        try {
            // Khởi tạo DAO để truy cập database
            UserDAO dao = new UserDAO();
            
            // Validate và convert userID sang int
            int userIDInt;
            try {
                userIDInt = Integer.parseInt(userID);
                if (userIDInt <= 0) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("User ID must be positive"));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID format"));
            }

            // Gọi DAO để toggle trạng thái user
            // Nếu user đang active (status=1) -> chuyển thành inactive (status=0)
            // Nếu user đang inactive (status=0) -> chuyển thành active (status=1)
            boolean result = dao.updateStatus(userIDInt);

            // Kiểm tra kết quả cập nhật
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("User status updated successfully", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to update user status or user not found"));
            }

        } catch (Exception e) {
            // Xử lý lỗi và in ra console để debug  
            System.out.println("Error at UserController - updateUserStatus: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }
}