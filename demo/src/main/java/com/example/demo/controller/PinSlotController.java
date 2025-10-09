package com.example.demo.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.dao.PinSlotDAO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PinSlotDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Pin Slot Management", description = "APIs for managing individual charging slots and charging status")
public class PinSlotController {

    private PinSlotDAO pinSlotDAO = new PinSlotDAO();

    // API để trigger manual update
    @GetMapping("/pinSlot/updatePinPercent")
    @Operation(summary = "Manual charging update", description = "Manually trigger charging percentage update for all pin slots (+1% per call).")
    public ResponseEntity<ApiResponse<Object>> updatePinSlotManual() {
        try {
            boolean check = pinSlotDAO.updatePinPercent();

            if (check) {
                return ResponseEntity.ok(ApiResponse.success("PinSlotDB successfully", "Update completed"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("PinSlotDB update failed"));
            }

        } catch (SQLException e) {
            System.out.println("Error at PinSlotController: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    // Scheduled task - chạy mỗi phút (60000ms = 1 phút)
    @Scheduled(fixedRate = 60000)
    public void updatePinSlotScheduled() {
        try {
            boolean check = pinSlotDAO.updatePinPercent();

            if (check) {
                System.out.println(
                        "Scheduled update: PinSlotDB procedure executed successfully at " + new java.util.Date());
            } else {
                System.out.println("Scheduled update: PinSlotDB procedure execution failed at " + new java.util.Date());
            }

        } catch (SQLException e) {
            System.out.println("Scheduled update error: " + e.toString());
        }
    }

    // API để lấy danh sách PinSlot theo stationID
    @GetMapping("/pinSlot/list")
    @Operation(summary = "Get charging slots by station", description = "Retrieve all charging slots for a specific station with their current status and availability.")
    public ResponseEntity<ApiResponse<Object>> getListPinSlot(
            @Parameter(description = "Station ID to get slots for", required = true) @RequestParam int stationID) {
        try {
            // Validation
            if (stationID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Station ID must be greater than 0"));
            }

            List<PinSlotDTO> listPinSlot = pinSlotDAO.getListPinSlotByStation(stationID);

            if (listPinSlot != null && !listPinSlot.isEmpty()) {
                return ResponseEntity.ok(ApiResponse
                        .success("Get PinSlot list for station " + stationID + " successfully", listPinSlot));
            } else {
                return ResponseEntity
                        .ok(ApiResponse.success("No PinSlots found for station " + stationID, listPinSlot));
            }

        } catch (SQLException e) {
            System.out.println("Error at PinSlotController getListPinSlot: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    // API để lấy tất cả PinSlot (không filter theo station)
    @GetMapping("/pinSlot/listAll")
    @Operation(summary = "Get all charging slots", description = "Retrieve all charging slots from all stations (Admin view).")
    public ResponseEntity<ApiResponse<Object>> getAllPinSlots() {
        try {
            List<PinSlotDTO> listPinSlot = pinSlotDAO.getListPinSlot();

            if (listPinSlot != null && !listPinSlot.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Get all PinSlots successfully", listPinSlot));
            } else {
                return ResponseEntity.ok(ApiResponse.success("PinSlot list is empty", listPinSlot));
            }

        } catch (SQLException e) {
            System.out.println("Error at PinSlotController getAllPinSlots: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    // API để kiểm tra status
    @GetMapping("/pinSlot/status")
    @Operation(summary = "Check slot service status", description = "Check if the pin slot management service is running with scheduled updates.")
    public ResponseEntity<ApiResponse<Object>> getPinSlotStatus() {
        return ResponseEntity.ok(ApiResponse.success("PinSlot service is running", "Scheduled updates every 1 minute"));
    }

    // API để update PinSlot theo pinID (Method 1: Request Parameters)
    @PutMapping("/pinSlot/updateSlot")
    @Operation(summary = "Update slot charging level", description = "Manually update the charging percentage (0-100%) of a specific pin slot.")
    public ResponseEntity<ApiResponse<Object>> updatePinSlot(
            @Parameter(description = "Pin slot ID to update", required = true) @RequestParam int pinID,
            @Parameter(description = "New charging percentage (0-100)", required = true, example = "85") @RequestParam int pinPercent) {

        try {
            // Validate pinPercent (0-100)
            if (pinPercent < 0 || pinPercent > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Pin percent must be between 0 and 100"));
            }

            // Gọi DAO để update
            boolean success = pinSlotDAO.updatePinSlot(pinID, pinPercent);

            if (success) {
                String statusMessage = (pinPercent < 100) ? " (Status: 0 - unvaliable)" : " (Status: 1 - valiable)";

                return ResponseEntity.ok(
                        ApiResponse.success("Pin slot updated successfully",
                                "PinID: " + pinID + " updated to " + pinPercent + "%" + statusMessage));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to update pin slot. PinID may not exist."));
            }

        } catch (Exception e) {
            System.out.println("Error updating pin slot: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error updating pin slot: " + e.getMessage()));
        }
    }

    @PutMapping("/pinSlot/reserve")
    @Operation(summary = "Reserve a pin slot", description = "Reserve a specific pin slot if it's status is 1 (valiable) and pinSlotStatus is 1 (available).")
    public ResponseEntity<ApiResponse<Object>> reservePinSlot(
            @Parameter(description = "Pin slot ID to reserve", required = true) @RequestParam int pinID,
            @Parameter(description = "User ID reserving the slot", required = false) @RequestParam(required = false) Integer userID) {
        try {
            boolean success = pinSlotDAO.reservePinSlot(pinID, userID);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("Pin slot reserved successfully", pinID));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse
                                .error("Failed to reserve pin slot. PinID may not exist or is not available."));
            }

        } catch (Exception e) {
            System.out.println("Error reserving pin slot: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error reserving pin slot: " + e.getMessage()));
        }
    }

    @PutMapping("/pinSlot/unreserve")
    @Operation(summary = "Unreserve a pin slot", description = "Unreserve a specific pin slot by setting status to 0 and userID to null.")
    public ResponseEntity<ApiResponse<Object>> unreservePinSlot(
            @Parameter(description = "Pin slot ID to unreserve", required = true) @RequestParam int pinID) {
        try {
            boolean success = pinSlotDAO.unreservePinSlot(pinID);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("Pin slot unreserved successfully", pinID));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse
                                .error("Failed to unreserve pin slot. PinID may not exist or is not currently reserved."));
            }

        } catch (Exception e) {
            System.out.println("Error unreserving pin slot: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error unreserving pin slot: " + e.getMessage()));
        }
    }

    @PutMapping("/pinSlot/updateStatus")
    @Operation(summary = "Update pin slot status", description = "Update the status of a specific pin slot.")
    public ResponseEntity<ApiResponse<Object>> updatePinSlotStatus(
            @Parameter(description = "Pin slot ID to update", required = true) @RequestParam int pinID,
            @Parameter(description = "New status for the pin slot", required = true) @RequestParam int status) {

        try {
            boolean success = pinSlotDAO.updatePinSlotStatus(pinID, status);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("Pin slot status updated successfully", pinID));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to update pin slot status. PinID may not exist."));
            }

        } catch (Exception e) {
            System.out.println("Error updating pin slot status: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error updating pin slot status: " + e.getMessage()));
        }
    }

}
