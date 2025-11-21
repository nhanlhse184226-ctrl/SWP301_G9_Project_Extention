package com.example.demo.controller;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.PinStationDAO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PinStationDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller quản lý các trạm sạc pin
 * Cung cấp API để tạo, cập nhật, quản lý trạm sạc và phân công nhân viên
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Pin Station Management", description = "APIs for managing charging stations and their properties")
public class PinStationController {
    
    // Khởi tạo DAO để truy cập database
    private final PinStationDAO pinStationDAO = new PinStationDAO();
    
    /**
     * API tạo trạm sạc mới
     * Tự động tạo 15 pin slots cho mỗi trạm sạc mới
     * @param stationName - Tên trạm sạc (bắt buộc)
     * @param location - Địa điểm/địa chỉ trạm sạc (bắt buộc)
     * @param status - Trạng thái trạm (0=inactive, 1=active, 2=maintenance)
     * @param x - Tọa độ X của trạm sạc
     * @param y - Tọa độ Y của trạm sạc
     * @param userID - ID nhân viên quản lý trạm (tùy chọn)
     * @return ResponseEntity chứa kết quả tạo trạm
     */
    // API để tạo PinStation mới (trigger sẽ tự động tạo 15 pin slots)
    @PostMapping("/pinStation/create")
    @Operation(summary = "Create new charging station", description = "Create a new charging station with specified name and location. Automatically creates 15 pin slots for the station.")
    public ResponseEntity<ApiResponse<Object>> createPinStation(
            @Parameter(description = "Name of the charging station", required = true) @RequestParam String stationName,
            @Parameter(description = "Location/address of the charging station", required = true) @RequestParam String location,
            @Parameter(description = "Station status (0=inactive, 1=active, 2=maintenance)", example = "1") @RequestParam(defaultValue = "1") int status,
            @Parameter(description = "X coordinate of the station", required = true) @RequestParam float x,
            @Parameter(description = "Y coordinate of the station", required = true) @RequestParam float y,
            @Parameter(description = "User ID of station staff", required = false) @RequestParam(required = false) Integer userID) {
        
        try {
            // Kiểm tra tên trạm không được rỗng
            if (stationName == null || stationName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Station name is required"));
            }
            
            // Kiểm tra địa điểm không được rỗng
            if (location == null || location.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Location is required"));
            }
            
            // Kiểm tra status hợp lệ (0, 1, 2)
            if (status < 0 || status > 2) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Status must be 0 (inactive), 1 (active), or 2 (maintenance)"));
            }
            
            // Gọi DAO để tạo trạm sạc mới
            boolean success = pinStationDAO.createPinStation(
                stationName.trim(), 
                location.trim(), 
                status,
                x,
                y,
                userID
            );
            
            // Kiểm tra kết quả tạo trạm
            if (success) {
                return ResponseEntity.ok(
                    ApiResponse.success("Pin station created successfully", 
                        "Station '" + stationName + "' has been created at '" + location + "' with 15 pin slots"));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create pin station"));
            }
            
        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Database error in createPinStation: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Database error occurred: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi chung
            System.out.println("Error creating pin station: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error creating pin station: " + e.getMessage()));
        }
    }
    
    // API để lấy danh sách tất cả PinStations
    @GetMapping("/pinStation/list")
    @Operation(summary = "Get all charging stations", description = "Retrieve a list of all charging stations with their details including name, location, and status.")
    public ResponseEntity<ApiResponse<Object>> getListPinStation() {
        try {
            List<PinStationDTO> listPinStation = pinStationDAO.getListPinStation();
            
            if (listPinStation != null && !listPinStation.isEmpty()) {
                return ResponseEntity.ok(
                    ApiResponse.success("Get pin station list successfully", listPinStation));
            } else {
                return ResponseEntity.ok(
                    ApiResponse.success("Pin station list is empty", listPinStation));
            }
            
        } catch (SQLException e) {
            System.out.println("Database error in getListPinStation: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Database error occurred: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error getting pin station list: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting pin station list: " + e.getMessage()));
        }
    }
    
    // API để lấy PinStation theo ID
    @GetMapping("/pinStation/{stationID}")
    @Operation(summary = "Get charging station by ID", description = "Retrieve details of a specific charging station using its ID.")
    public ResponseEntity<ApiResponse<Object>> getPinStationById(
            @Parameter(description = "Station ID to retrieve", required = true) @PathVariable int stationID) {
        try {
            // Validate input
            if (stationID <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Station ID must be greater than 0"));
            }
            
            PinStationDTO station = pinStationDAO.getPinStationById(stationID);
            
            if (station != null) {
                return ResponseEntity.ok(
                    ApiResponse.success("Pin station found", station));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Pin station not found with ID: " + stationID));
            }
            
        } catch (SQLException e) {
            System.out.println("Database error in getPinStationById: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Database error occurred: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error getting pin station: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting pin station: " + e.getMessage()));
        }
    }
    
    // API để lấy danh sách PinStation theo userID
    @GetMapping("/pinStation/getByUser")
    @Operation(summary = "Get charging stations by user ID", description = "Retrieve all charging stations assigned to a specific user, ordered by creation date (newest first).")
    public ResponseEntity<ApiResponse<Object>> getStationsByUserID(
            @Parameter(description = "User ID to get stations for", required = true) @RequestParam int userID) {
        try {
            // Validate input
            if (userID <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User ID must be greater than 0"));
            }
            
            List<PinStationDTO> listPinStation = pinStationDAO.getStationsByUserID(userID);
            
            if (listPinStation != null && !listPinStation.isEmpty()) {
                return ResponseEntity.ok(
                    ApiResponse.success("Get stations for user successfully", listPinStation));
            } else {
                return ResponseEntity.ok(
                    ApiResponse.success("No stations found for user ID: " + userID, listPinStation));
            }
            
        } catch (SQLException e) {
            System.out.println("Database error in getStationsByUserID: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Database error occurred: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error getting stations for user: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting stations for user: " + e.getMessage()));
        }
    }
    
    // API để update PinStation
    @PutMapping("/pinStation/update")
    @Operation(summary = "Update charging station", description = "Update charging station information including name, location, status, and coordinates.")
    public ResponseEntity<ApiResponse<Object>> updatePinStation(
            @Parameter(description = "Station ID to update", required = true) @RequestParam int stationID,
            @Parameter(description = "New station name", required = true) @RequestParam String stationName,
            @Parameter(description = "New station location", required = true) @RequestParam String location,
            @Parameter(description = "New X coordinate", required = true) @RequestParam Float x,
            @Parameter(description = "New Y coordinate", required = true) @RequestParam Float y) {
        
        try {
            // Validate input
            if (stationID <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Station ID must be greater than 0"));
            }
            if (stationName == null || stationName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Station name is required"));
            }
            if (location == null || location.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Location is required"));
            }
            
            // Gọi DAO để update station
            boolean success = pinStationDAO.updatePinStation(
                stationID,
                stationName.trim(), 
                location.trim(), 
                x,
                y
            );
            
            if (success) {
                return ResponseEntity.ok(
                    ApiResponse.success("Pin station updated successfully", 
                        "Station ID " + stationID + " has been updated with new information"));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update pin station"));
            }
            
        } catch (SQLException e) {
            System.out.println("Database error in updatePinStation: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Update failed: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error updating pin station: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error updating pin station: " + e.getMessage()));
        }
    }
    
    // API để đảo ngược status của pinStation và pinSlot
    @GetMapping("/pinStation/updateStatus")
    @Operation(summary = "Toggle station status", description = "Toggle the status of a charging station and all its associated pin slots between active and inactive.")
    public ResponseEntity<ApiResponse<Object>> togglePinStationStatus(
            @Parameter(description = "Station ID to toggle status with 1 is active and 0 is inactive", required = true) @RequestParam int stationID) {
        try {
            // Đảo ngược status của pinStation và tất cả pinSlot
            boolean result = pinStationDAO.updateStatus(stationID);

            if (result) {
                return ResponseEntity.ok(ApiResponse.success("Pin station status updated successfully", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update pin station status or station not found"));
            }
            
        } catch (Exception e) {
            System.out.println("Error at togglePinStationStatus: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }
    
    // API để kiểm tra status service
    @GetMapping("/pinStation/status")
    @Operation(summary = "Check service status", description = "Check if the Pin Station service is running and available.")
    public ResponseEntity<ApiResponse<Object>> getPinStationStatus() {
        return ResponseEntity.ok(
            ApiResponse.success("Pin Station service is running", 
                "Service ready to create and manage pin stations"));
    }

    /**
     * API phân công nhân viên vào trạm sạc
     * Gán một nhân viên (roleID=2) quản lý một trạm sạc cụ thể
     * @param userID - ID nhân viên từ session (bắt buộc, roleID=2)
     * @param stationID - ID trạm sạc cần gán nhân viên (bắt buộc)
     * @return ResponseEntity chứa kết quả phân công
     */
    // API để assign staff vào station
    @PutMapping("/pinStation/assignStaff")
    @Operation(summary = "Assign staff to station", description = "Assign a staff member (roleID=2) to manage a specific charging station. UserID comes from session, stationID is manual input.")
    public ResponseEntity<ApiResponse<Object>> assignStaffToStation(
            @Parameter(description = "User ID from session (must be staff with roleID=2)", required = true) @RequestParam Integer userID,
            @Parameter(description = "Station ID to assign staff to", required = true) @RequestParam int stationID) {
        try {
            // Kiểm tra stationID hợp lệ
            if (stationID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Station ID must be greater than 0"));
            }
            
            // Kiểm tra userID hợp lệ
            if (userID == null || userID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("User ID is required and must be greater than 0"));
            }

            // Gọi DAO để phân công nhân viên vào trạm
            boolean success = pinStationDAO.assignStaffToStation(stationID, userID);
            
            // Kiểm tra kết quả phân công
            if (success) {
                String message = "Staff (userID: " + userID + ") successfully assigned to station " + stationID;
                return ResponseEntity.ok(ApiResponse.success(message, 
                    "StationID: " + stationID + ", AssignedUserID: " + userID));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to assign staff to station. Station may not exist."));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Database error in assignStaffToStation: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Assignment failed: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi chung
            System.out.println("Error assigning staff to station: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error assigning staff to station: " + e.getMessage()));
        }
    }

    // API để check staff đã được assign vào trạm nào
    @GetMapping("/pinStation/checkStaffAssignment")
    @Operation(summary = "Check staff assignment", description = "Check which station a staff member is currently assigned to.")
    public ResponseEntity<ApiResponse<Object>> checkStaffAssignment(
            @Parameter(description = "User ID to check assignment for", required = true) @RequestParam Integer userID) {
        try {
            // Validation
            if (userID == null || userID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("User ID is required and must be greater than 0"));
            }

            Integer assignedStationID = pinStationDAO.getStaffAssignedStation(userID);
            
            if (assignedStationID != null) {
                String message = "Staff (userID: " + userID + ") is assigned to station " + assignedStationID;
                Map<String, Object> data = new HashMap<>();
                data.put("userID", userID);
                data.put("assignedStationID", assignedStationID);
                return ResponseEntity.ok(ApiResponse.success(message, data));
            } else {
                String message = "Staff (userID: " + userID + ") is not assigned to any station";
                Map<String, Object> data = new HashMap<>();
                data.put("userID", userID);
                data.put("assignedStationID", null);
                return ResponseEntity.ok(ApiResponse.success(message, data));
            }

        } catch (SQLException e) {
            System.out.println("Database error in checkStaffAssignment: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Check assignment failed: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error checking staff assignment: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error checking staff assignment: " + e.getMessage()));
        }
    }
}
