package com.example.demo.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.PinSlotDAO;
import com.example.demo.dao.UserDAO;
import com.example.demo.dao.VehicleDAO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.VehicleDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller quản lý phương tiện và pin swap
 * Cung cấp API để quản lý vehicles và thực hiện swap pin (core function của mainflow)
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Vehicle Management", description = "APIs for managing vehicles and pin swapping")
public class VehicleController {

    // Khởi tạo các DAO để truy cập database
    private VehicleDAO vehicleDAO = new VehicleDAO();
    private PinSlotDAO pinSlotDAO = new PinSlotDAO();
    private UserDAO userDAO = new UserDAO();

    /**
     * API lấy tất cả vehicles trong hệ thống
     * Trả về danh sách tất cả phương tiện (dành cho admin)
     * @return ResponseEntity chứa danh sách tất cả vehicles
     */
    @GetMapping("/vehicle/list")
    @Operation(summary = "Get all vehicles", description = "Retrieve list of all vehicles in the system")
    public ResponseEntity<ApiResponse<List<VehicleDTO>>> getAllVehicles() {
        try {
            // Gọi DAO để lấy tất cả vehicles
            List<VehicleDTO> vehicles = vehicleDAO.getAllVehicles();
            
            // Trả về kết quả thành công với danh sách vehicles
            return ResponseEntity.ok(ApiResponse.success("Vehicles retrieved successfully", vehicles));
        } catch (SQLException e) {
            // Xử lý lỗi database và in ra console
            System.out.println("Database error in getAllVehicles: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi chung
            System.out.println("Unexpected error in getAllVehicles: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Unexpected error occurred"));
        }
    }

    /**
     * API lấy vehicles theo user ID (MAINFLOW - Chọn xe để đặt slot)
     * Trả về danh sách phương tiện thuộc về user cụ thể
     * @param userID - ID của user cần lấy vehicles (bắt buộc > 0)
     * @return ResponseEntity chứa danh sách vehicles của user
     */
    @GetMapping("/vehicle/user")
    @Operation(summary = "Get vehicles by user ID", description = "Retrieve vehicles belonging to a specific user")
    public ResponseEntity<ApiResponse<List<VehicleDTO>>> getVehiclesByUserID(
            @Parameter(description = "User ID to get vehicles for", required = true) @RequestParam int userID) {
        try {
            // Gọi DAO để lấy vehicles của user cụ thể
            List<VehicleDTO> vehicles = vehicleDAO.getVehiclesByUserID(userID);
            
            // Trả về kết quả thành công
            return ResponseEntity.ok(ApiResponse.success("User vehicles retrieved successfully", vehicles));
        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Database error in getVehiclesByUserID: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi chung
            System.out.println("Unexpected error in getVehiclesByUserID: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Unexpected error occurred"));
        }
    }

    /**
     * API tạo vehicle mới
     * Tạo phương tiện mới cho user với thông tin đầy đủ
     * @param userID - ID chủ sở hữu vehicle (bắt buộc > 0)
     * @param licensePlate - Biển số xe (bắt buộc, không được rỗng)
     * @param vehicleType - Loại xe (bắt buộc, không được rỗng)
     * @param pinPercent - Phần trăm pin ban đầu (0-100)
     * @param pinHealth - Sức khỏe pin ban đầu (0-100)
     * @return ResponseEntity chứa kết quả tạo vehicle
     */
    @PostMapping("/vehicle/create")
    @Operation(summary = "Create new vehicle", description = "Create a new vehicle record")
    public ResponseEntity<ApiResponse<Object>> createVehicle(
            @Parameter(description = "User ID", required = true) @RequestParam int userID,
            @Parameter(description = "License plate number", required = true) @RequestParam String licensePlate,
            @Parameter(description = "Vehicle type", required = true) @RequestParam String vehicleType,
            @Parameter(description = "Initial pin percentage (0-100)", required = true) @RequestParam int pinPercent,
            @Parameter(description = "Initial pin health (0-100)", required = true) @RequestParam int pinHealth) {
        try {
            // Kiểm tra tính hợp lệ của pinPercent (0-100)
            if (pinPercent < 0 || pinPercent > 100) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("Pin percentage must be between 0 and 100. Provided value: " + pinPercent));
            }

            // Kiểm tra tính hợp lệ của pinHealth (0-100)
            if (pinHealth < 0 || pinHealth > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Pin health must be between 0 and 100. Provided value: " + pinHealth));
            }

            // Kiểm tra licensePlate không được rỗng
            if (licensePlate == null || licensePlate.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("License plate cannot be empty"));
            }

            // Kiểm tra vehicleType không được rỗng
            if (vehicleType == null || vehicleType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Vehicle type cannot be empty"));
            }

            // Gọi DAO để tạo vehicle mới
            boolean success = vehicleDAO.createVehicle(userID, licensePlate, vehicleType, pinPercent, pinHealth);
            
            // Kiểm tra kết quả tạo vehicle
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("Vehicle created successfully",
                        "Vehicle with license plate " + licensePlate + " has been created"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create vehicle"));
            }
        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Database error in createVehicle: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi chung
            System.out.println("Unexpected error in createVehicle: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Unexpected error occurred"));
        }
    }

    /**
     * API hoán đổi dữ liệu pin giữa Vehicle và PinSlot (MAINFLOW - Core function)
     * Trao đổi SOH và SOC values giữa xe và slot theo yêu cầu conversation
     * Đây là FUNCTION QUAN TRỌNG NHẤT trong mainflow
     * @param vehicleID - ID vehicle cần swap pin (bắt buộc)
     * @param pinSlotID - ID pin slot cần swap pin (bắt buộc)
     * @return ResponseEntity chứa kết quả swap pin
     */
    @PostMapping("/vehicle/PinSwap")
    @Operation(summary = "Swap pin data between Vehicle and PinSlot", description = "Exchange SOH and SOC values between a vehicle and a pin slot according to conversation requirements")
    public ResponseEntity<ApiResponse<Object>> swapVehiclePinSlotData(
            @Parameter(description = "Vehicle ID to swap data with", required = true) @RequestParam int vehicleID,
            @Parameter(description = "Pin Slot ID to swap data with", required = true) @RequestParam int pinSlotID) {
        try {
            // Gọi DAO để thực hiện swap pin data giữa vehicle và slot
            boolean success = pinSlotDAO.swapVehiclePinSlotData(vehicleID, pinSlotID);
            
            // Kiểm tra kết quả swap
            if (success) {
                // Swap thành công
                return ResponseEntity.ok(ApiResponse.success("Pin data swapped successfully",
                        "SOH and SOC values have been exchanged between Vehicle ID " + vehicleID + " and PinSlot ID "
                                + pinSlotID));
            } else {
                // Swap thất bại (vehicle hoặc slot không tồn tại)
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to swap pin data. Check if both Vehicle and PinSlot exist"));
            }
        } catch (Exception e) {
            // Xử lý lỗi và in ra console
            System.out.println("Unexpected error in swapVehiclePinSlotData: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Unexpected error occurred"));
        }
    }

    /**
     * API lấy thông tin chủ sở hữu của vehicle
     * Trả về thông tin user (owner) của phương tiện cụ thể
     * @param vehicleID - ID vehicle cần lấy owner (bắt buộc > 0)
     * @return ResponseEntity chứa thông tin owner
     */
    @GetMapping("/vehicle/getOwner")
    @Operation(summary = "Get vehicle owner", description = "Retrieve the user (owner) information for a specific vehicle")
    public ResponseEntity<ApiResponse<UserDTO>> getVehicleOwner(
            @Parameter(description = "Vehicle ID to get owner for", required = true) @RequestParam int vehicleID) {
        try {
            // Kiểm tra tính hợp lệ của vehicleID
            if (vehicleID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Vehicle ID must be greater than 0"));
            }

            // Gọi DAO để lấy thông tin owner của vehicle
            UserDTO user = userDAO.getUserByVehicleID(vehicleID);

            // Kiểm tra kết quả
            if (user != null) {
                // Tìm thấy owner
                return ResponseEntity.ok(ApiResponse.success("Vehicle owner retrieved successfully", user));
            } else {
                // Không tìm thấy owner (vehicle không tồn tại)
                return ResponseEntity.notFound().build();
            }
        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Database error in getVehicleOwner: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi chung
            System.out.println("Unexpected error in getVehicleOwner: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Unexpected error occurred"));
        }
    }

    /**
     * API lấy thông tin vehicle theo ID
     * Trả về chi tiết phương tiện cụ thể
     * @param vehicleID - ID vehicle cần lấy thông tin (bắt buộc > 0)
     * @return ResponseEntity chứa thông tin vehicle
     */
    @GetMapping("/vehicle/getVehicleByID")
    @Operation(summary = "Get vehicle by ID", description = "Retrieve vehicle information for a specific vehicle ID")
    public ResponseEntity<ApiResponse<VehicleDTO>> getVehicleByID(
            @Parameter(description = "Vehicle ID to retrieve", required = true) @RequestParam int vehicleID) {
        try {
            // Kiểm tra tính hợp lệ của vehicleID
            if (vehicleID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Vehicle ID must be greater than 0"));
            }

            // Gọi DAO để lấy thông tin vehicle theo ID
            VehicleDTO vehicle = vehicleDAO.getVehiclesByVehicleID(vehicleID);

            // Kiểm tra kết quả
            if (vehicle != null) {
                // Tìm thấy vehicle
                return ResponseEntity.ok(ApiResponse.success("Vehicle retrieved successfully", vehicle));
            } else {
                // Không tìm thấy vehicle
                return ResponseEntity.notFound().build();
            }
        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Database error in getVehicleByID: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi chung
            System.out.println("Unexpected error in getVehicleByID: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Unexpected error occurred"));
        }
    }

}