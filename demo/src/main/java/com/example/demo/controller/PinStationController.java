package com.example.demo.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.PinStationDAO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PinStationDTO;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PinStationController {
    
    private final PinStationDAO pinStationDAO = new PinStationDAO();
    
    // API để tạo PinStation mới (trigger sẽ tự động tạo 15 pin slots)
    @PostMapping("/pinStation/create")
    public ResponseEntity<ApiResponse<Object>> createPinStation(
            @RequestParam String stationName,
            @RequestParam String location,
            @RequestParam(defaultValue = "active") String status) {
        
        try {
            // Validate input
            if (stationName == null || stationName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Station name is required"));
            }
            
            if (location == null || location.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Location is required"));
            }
            
            // Gọi DAO để tạo station mới
            boolean success = pinStationDAO.createPinStation(
                stationName.trim(), 
                location.trim(), 
                status.trim()
            );
            
            if (success) {
                return ResponseEntity.ok(
                    ApiResponse.success("Pin station created successfully", 
                        "Station '" + stationName + "' has been created at '" + location + "' with 15 pin slots"));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create pin station"));
            }
            
        } catch (SQLException e) {
            System.out.println("Database error in createPinStation: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Database error occurred: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error creating pin station: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error creating pin station: " + e.getMessage()));
        }
    }
    
    // API để lấy danh sách tất cả PinStations
    @GetMapping("/pinStation/list")
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
    public ResponseEntity<ApiResponse<Object>> getPinStationById(@PathVariable int stationID) {
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
    
    // API để xóa pinStation (copy từ UserController)
    @DeleteMapping("/pinStation/delete")
    public ResponseEntity<ApiResponse<Object>> deletePinStation(@RequestParam int stationID) {
        try {
            // Tạo PinStationDTO với stationID để delete
            PinStationDTO deletePinStation = new PinStationDTO();
            deletePinStation.setStationID(stationID);
            
            // Xóa pinStation khỏi database
            boolean result = pinStationDAO.delete(deletePinStation);
            
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("Pin station deleted successfully", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete pin station or station not found"));
            }
            
        } catch (Exception e) {
            System.out.println("Error at deletePinStation: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }
    
    // API để kiểm tra status service
    @GetMapping("/pinStation/status")
    public ResponseEntity<ApiResponse<Object>> getPinStationStatus() {
        return ResponseEntity.ok(
            ApiResponse.success("Pin Station service is running", 
                "Service ready to create and manage pin stations"));
    }
}
