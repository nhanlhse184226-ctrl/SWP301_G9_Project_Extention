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
import com.example.demo.dao.VehicleDAO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.VehicleDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Vehicle Management", description = "APIs for managing vehicles and pin swapping")
public class VehicleController {

    private VehicleDAO vehicleDAO = new VehicleDAO();
    private PinSlotDAO pinSlotDAO = new PinSlotDAO();

    @GetMapping("/vehicle/list")
    @Operation(summary = "Get all vehicles", description = "Retrieve list of all vehicles in the system")
    public ResponseEntity<ApiResponse<List<VehicleDTO>>> getAllVehicles() {
        try {
            List<VehicleDTO> vehicles = vehicleDAO.getAllVehicles();
            return ResponseEntity.ok(ApiResponse.success("Vehicles retrieved successfully", vehicles));
        } catch (SQLException e) {
            System.out.println("Database error in getAllVehicles: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Unexpected error in getAllVehicles: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Unexpected error occurred"));
        }
    }

    @GetMapping("/vehicle/user")
    @Operation(summary = "Get vehicles by user ID", description = "Retrieve vehicles belonging to a specific user")
    public ResponseEntity<ApiResponse<List<VehicleDTO>>> getVehiclesByUserID(
            @Parameter(description = "User ID to get vehicles for", required = true) @RequestParam int userID) {
        try {
            List<VehicleDTO> vehicles = vehicleDAO.getVehiclesByUserID(userID);
            return ResponseEntity.ok(ApiResponse.success("User vehicles retrieved successfully", vehicles));
        } catch (SQLException e) {
            System.out.println("Database error in getVehiclesByUserID: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Unexpected error in getVehiclesByUserID: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Unexpected error occurred"));
        }
    }

    @PostMapping("/vehicle/create")
    @Operation(summary = "Create new vehicle", description = "Create a new vehicle record")
    public ResponseEntity<ApiResponse<Object>> createVehicle(
            @Parameter(description = "User ID", required = true) @RequestParam int userID,
            @Parameter(description = "License plate number", required = true) @RequestParam String licensePlate,
            @Parameter(description = "Vehicle type", required = true) @RequestParam String vehicleType,
            @Parameter(description = "Initial pin percentage (0-100)", required = true) @RequestParam int pinPercent,
            @Parameter(description = "Initial pin health (0-100)", required = true) @RequestParam int pinHealth) {
        try {
            // Validation cho pinPercent
            if (pinPercent < 0 || pinPercent > 100) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("Pin percentage must be between 0 and 100. Provided value: " + pinPercent));
            }

            // Validation cho pinHealth
            if (pinHealth < 0 || pinHealth > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Pin health must be between 0 and 100. Provided value: " + pinHealth));
            }

            // Validation cho licensePlate (không được empty)
            if (licensePlate == null || licensePlate.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("License plate cannot be empty"));
            }

            // Validation cho vehicleType (không được empty)
            if (vehicleType == null || vehicleType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Vehicle type cannot be empty"));
            }

            boolean success = vehicleDAO.createVehicle(userID, licensePlate, vehicleType, pinPercent, pinHealth);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("Vehicle created successfully",
                        "Vehicle with license plate " + licensePlate + " has been created"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create vehicle"));
            }
        } catch (SQLException e) {
            System.out.println("Database error in createVehicle: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Unexpected error in createVehicle: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Unexpected error occurred"));
        }
    }

    @PostMapping("/vehicle/PinSwap")
    @Operation(summary = "Swap pin data between Vehicle and PinSlot", description = "Exchange SOH and SOC values between a vehicle and a pin slot according to conversation requirements")
    public ResponseEntity<ApiResponse<Object>> swapVehiclePinSlotData(
            @Parameter(description = "Vehicle ID to swap data with", required = true) @RequestParam int vehicleID,
            @Parameter(description = "Pin Slot ID to swap data with", required = true) @RequestParam int pinSlotID) {
        try {
            boolean success = pinSlotDAO.swapVehiclePinSlotData(vehicleID, pinSlotID);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("Pin data swapped successfully",
                        "SOH and SOC values have been exchanged between Vehicle ID " + vehicleID + " and PinSlot ID "
                                + pinSlotID));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to swap pin data. Check if both Vehicle and PinSlot exist"));
            }
        } catch (Exception e) {
            System.out.println("Unexpected error in swapVehiclePinSlotData: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Unexpected error occurred"));
        }
    }

}