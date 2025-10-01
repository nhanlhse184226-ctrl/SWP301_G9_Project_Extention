package com.example.demo.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    
    // API để lấy danh sách PinSlot
    @GetMapping("/pinSlot/list")
    public ResponseEntity<ApiResponse<Object>> getListPinSlot() {
        try {
            List<PinSlotDTO> listPinSlot = pinSlotDAO.getListPinSlot();
            
            if (listPinSlot != null && !listPinSlot.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Get PinSlot list successfully", listPinSlot));
            } else {
                return ResponseEntity.ok(ApiResponse.success("PinSlot list is empty", listPinSlot));
            }
            
        } catch (SQLException e) {
            System.out.println("Error at PinSlotController getListPinSlot: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }
    
    // API để kiểm tra status
    @GetMapping("/pinSlot/status")
    public ResponseEntity<ApiResponse<Object>> getPinSlotStatus() {
        return ResponseEntity.ok(ApiResponse.success("PinSlot service is running", "Scheduled updates every 1 minute"));
    }
}
