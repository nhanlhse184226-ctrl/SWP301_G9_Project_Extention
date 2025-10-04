package com.example.demo.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.PinSlotDAO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PinSlotDTO;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PinSlotController {
    
    private PinSlotDAO pinSlotDAO = new PinSlotDAO();
    
    // API để trigger manual update
    @GetMapping("/pinSlot/updatePinPercent")
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
                System.out.println("Scheduled update: PinSlotDB procedure executed successfully at " + new java.util.Date());
            } else {
                System.out.println("Scheduled update: PinSlotDB procedure execution failed at " + new java.util.Date());
            }
            
        } catch (SQLException e) {
            System.out.println("Scheduled update error: " + e.toString());
        }
    }
    
    // Scheduled task để reset expired reservations - chạy mỗi 5 giây để kiểm tra và reset những slot đã reserve quá 1 phút
    @Scheduled(fixedDelay = 5000) 
    public void resetExpiredReservationsScheduled() {
        try {
            boolean check = pinSlotDAO.resetExpiredReservations();
            
            if (check) {
                System.out.println("Scheduled reset: Expired reservations reset successfully at " + new java.util.Date());
            } else {
                System.out.println("Scheduled reset: Reset expired reservations failed at " + new java.util.Date());
            }
            
        } catch (SQLException e) {
            System.out.println("Scheduled reset error: " + e.toString());
        }
    }
    
    // API để lấy danh sách PinSlot theo stationID
    @GetMapping("/pinSlot/list")
    public ResponseEntity<ApiResponse<Object>> getListPinSlot(@RequestParam int stationID) {
        try {
            // Validation
            if (stationID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Station ID must be greater than 0"));
            }
            
            List<PinSlotDTO> listPinSlot = pinSlotDAO.getListPinSlotByStation(stationID);
            
            if (listPinSlot != null && !listPinSlot.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Get PinSlot list for station " + stationID + " successfully", listPinSlot));
            } else {
                return ResponseEntity.ok(ApiResponse.success("No PinSlots found for station " + stationID, listPinSlot));
            }
            
        } catch (SQLException e) {
            System.out.println("Error at PinSlotController getListPinSlot: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }
    
    // API để lấy tất cả PinSlot (không filter theo station)
    @GetMapping("/pinSlot/listAll")
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
    public ResponseEntity<ApiResponse<Object>> getPinSlotStatus() {
        return ResponseEntity.ok(ApiResponse.success("PinSlot service is running", "Scheduled updates every 1 minute"));
    }
    
    // API để reserve slot
    @PostMapping("/pinSlot/reserve")
    public ResponseEntity<ApiResponse<Object>> reserveSlot(@RequestParam int pinID) {
        try {
            boolean success = pinSlotDAO.reserveSlot(pinID);
            
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("Slot reserved successfully", "Pin slot " + pinID + " has been reserved"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to reserve slot. Slot may already be reserved or not available"));
            }
            
        } catch (SQLException e) {
            System.out.println("Database error in reserveSlot: " + e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error("Database error occurred"));
        } catch (Exception e) {
            System.out.println("Unexpected error in reserveSlot: " + e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }
    
    // API để update PinSlot theo pinID (Method 1: Request Parameters)
    @PutMapping("/pinSlot/updateSlot")
    public ResponseEntity<ApiResponse<Object>> updatePinSlot(
            @RequestParam int pinID, 
            @RequestParam int pinPercent) {
        
        try {
            // Validate pinPercent (0-100)
            if (pinPercent < 0 || pinPercent > 100) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Pin percent must be between 0 and 100"));
            }
            
            // Gọi DAO để update
            boolean success = pinSlotDAO.updatePinSlot(pinID, pinPercent);
            
            if (success) {
                String statusMessage = (pinPercent < 100) ? 
                    " (Status: unvaliable)" : 
                    " (Status: valiable)";
                    
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
}
