package com.example.demo.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.PinSlotDAO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PinSlotDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller quản lý các slot sạc pin
 * Cung cấp API để quản lý trạng thái, đặt chỗ và swap pin giữa các slot
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Pin Slot Management", description = "APIs for managing individual charging slots and charging status")
public class PinSlotController {

    // Khởi tạo DAO để truy cập database
    private PinSlotDAO pinSlotDAO = new PinSlotDAO();

    /**
     * API cập nhật phần trăm pin thủ công
     * Tăng 1% cho tất cả pin slot mỗi lần gọi
     * @return ResponseEntity chứa kết quả cập nhật
     */
    // API để trigger manual update
    @GetMapping("/pinSlot/updatePinPercent")
    @Operation(summary = "Manual charging update", description = "Manually trigger charging percentage update for all pin slots (+1% per call).")
    public ResponseEntity<ApiResponse<Object>> updatePinSlotManual() {
        try {
            // Gọi DAO để cập nhật phần trăm pin cho tất cả slot
            boolean check = pinSlotDAO.updatePinPercent();

            // Kiểm tra kết quả cập nhật
            if (check) {
                // Cập nhật thành công
                return ResponseEntity.ok(ApiResponse.success("PinSlotDB successfully", "Update completed"));
            } else {
                // Cập nhật thất bại
                return ResponseEntity.badRequest().body(ApiResponse.error("PinSlotDB update failed"));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database và in ra console
            System.out.println("Error at PinSlotController: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * Scheduled task tự động cập nhật pin
     * Chạy mỗi phút (60000ms = 1 phút) để tăng phần trăm pin
     */
    // Scheduled task - chạy mỗi phút (60000ms = 1 phút)
    @Scheduled(fixedRate = 60000)
    public void updatePinSlotScheduled() {
        try {
            // Gọi DAO để cập nhật phần trăm pin tự động
            boolean check = pinSlotDAO.updatePinPercent();

            // Kiểm tra và log kết quả cập nhật
            if (check) {
                System.out.println(
                        "Scheduled update: PinSlotDB procedure executed successfully at " + new java.util.Date());
            } else {
                System.out.println("Scheduled update: PinSlotDB procedure execution failed at " + new java.util.Date());
            }

        } catch (SQLException e) {
            // Log lỗi nếu có
            System.out.println("Scheduled update error: " + e.toString());
        }
    }

    /**
     * API lấy danh sách pin slot theo station ID
     * @param stationID - ID của trạm sạc (bắt buộc > 0)
     * @return ResponseEntity chứa danh sách pin slots của trạm
     */
    // API để lấy danh sách PinSlot theo stationID
    @GetMapping("/pinSlot/list")
    @Operation(summary = "Get charging slots by station", description = "Retrieve all charging slots for a specific station with their current status and availability.")
    public ResponseEntity<ApiResponse<Object>> getListPinSlot(
            @Parameter(description = "Station ID to get slots for", required = true) @RequestParam int stationID) {
        try {
            // Kiểm tra tính hợp lệ của stationID
            if (stationID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Station ID must be greater than 0"));
            }

            // Gọi DAO để lấy danh sách pin slot theo station
            List<PinSlotDTO> listPinSlot = pinSlotDAO.getListPinSlotByStation(stationID);

            // Kiểm tra kết quả và trả về response tương ứng
            if (listPinSlot != null && !listPinSlot.isEmpty()) {
                return ResponseEntity.ok(ApiResponse
                        .success("Get PinSlot list for station " + stationID + " successfully", listPinSlot));
            } else {
                return ResponseEntity
                        .ok(ApiResponse.success("No PinSlots found for station " + stationID, listPinSlot));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Error at PinSlotController getListPinSlot: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * API lấy danh sách pin slot theo vehicle ID
     * Trả về tất cả slots đã được đặt bởi vehicle cụ thể
     * @param vehicleID - ID của vehicle cần tìm slots (bắt buộc > 0)
     * @return ResponseEntity chứa danh sách pin slots của vehicle
     */
    // API để lấy danh sách PinSlot theo vehicleID
    @GetMapping("/pinSlot/getByVehicle")
    @Operation(summary = "Get charging slots by vehicle", description = "Retrieve all charging slots reserved by a specific vehicle.")
    public ResponseEntity<ApiResponse<Object>> getListPinSlotByVehicle(
            @Parameter(description = "Vehicle ID to get slots for", required = true) @RequestParam int vehicleID) {
        try {
            // Kiểm tra tính hợp lệ của vehicleID
            if (vehicleID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Vehicle ID must be greater than 0"));
            }

            // Gọi DAO để lấy danh sách pin slot đã đặt bởi vehicle
            List<PinSlotDTO> listPinSlot = pinSlotDAO.getListPinSlotByVehicle(vehicleID);

            // Kiểm tra kết quả và trả về response tương ứng
            if (listPinSlot != null && !listPinSlot.isEmpty()) {
                return ResponseEntity.ok(ApiResponse
                        .success("Get PinSlot list for vehicle " + vehicleID + " successfully", listPinSlot));
            } else {
                return ResponseEntity
                        .ok(ApiResponse.success("No PinSlots found for vehicle " + vehicleID, listPinSlot));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Error at PinSlotController getListPinSlotByVehicle: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * API lấy tất cả pin slots trong hệ thống
     * Trả về toàn bộ slots từ tất cả stations (dành cho admin)
     * Không có filter theo station hoặc user
     * @return ResponseEntity chứa danh sách tất cả pin slots
     */
    // API để lấy tất cả PinSlot (không filter theo station)
    @GetMapping("/pinSlot/listAll")
    @Operation(summary = "Get all charging slots", description = "Retrieve all charging slots from all stations (Admin view).")
    public ResponseEntity<ApiResponse<Object>> getAllPinSlots() {
        try {
            // Gọi DAO để lấy tất cả pin slots trong hệ thống
            List<PinSlotDTO> listPinSlot = pinSlotDAO.getListPinSlot();

            // Kiểm tra kết quả và trả về response tương ứng
            if (listPinSlot != null && !listPinSlot.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Get all PinSlots successfully", listPinSlot));
            } else {
                return ResponseEntity.ok(ApiResponse.success("PinSlot list is empty", listPinSlot));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Error at PinSlotController getAllPinSlots: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * API kiểm tra trạng thái dịch vụ pin slot
     * Trả về thông tin service đang chạy và lịch trình cập nhật
     * @return ResponseEntity chứa thông tin trạng thái dịch vụ
     */
    // API để kiểm tra status
    @GetMapping("/pinSlot/status")
    @Operation(summary = "Check slot service status", description = "Check if the pin slot management service is running with scheduled updates.")
    public ResponseEntity<ApiResponse<Object>> getPinSlotStatus() {
        // Trả về thông tin service đang hoạt động
        return ResponseEntity.ok(ApiResponse.success("PinSlot service is running", "Scheduled updates every 1 minute"));
    }

    /**
     * API cập nhật thông tin pin slot cụ thể
     * Cho phép cập nhật phần trăm pin và sức khỏe pin thủ công
     * @param pinID - ID của pin slot cần cập nhật (bắt buộc)
     * @param pinPercent - Phần trăm pin mới (0-100) (bắt buộc)
     * @param pinHealth - Sức khỏe pin mới (0-100) (bắt buộc)
     * @return ResponseEntity chứa kết quả cập nhật
     */
    // API để update PinSlot theo pinID (Method 1: Request Parameters)
    @PutMapping("/pinSlot/updateSlot")
    @Operation(summary = "Update slot charging level and health", description = "Manually update the charging percentage (0-100%) and health status (0-100%) of a specific pin slot.")
    public ResponseEntity<ApiResponse<Object>> updatePinSlot(
            @Parameter(description = "Pin slot ID to update", required = true) @RequestParam int pinID,
            @Parameter(description = "New charging percentage (0-100)", required = true, example = "85") @RequestParam int pinPercent,
            @Parameter(description = "Pin health percentage (0-100)", required = true, example = "90") @RequestParam int pinHealth) {

        try {
            // Kiểm tra tính hợp lệ của pinPercent (phải từ 0-100)
            if (pinPercent < 0 || pinPercent > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Pin percent must be between 0 and 100"));
            }

            // Kiểm tra tính hợp lệ của pinHealth (phải từ 0-100)
            if (pinHealth < 0 || pinHealth > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Pin health must be between 0 and 100"));
            }

            // Gọi DAO để cập nhật pin slot với giá trị mới
            boolean success = pinSlotDAO.updatePinSlot(pinID, pinPercent, pinHealth);

            // Kiểm tra kết quả cập nhật
            if (success) {
                // Xác định trạng thái slot dựa trên pinPercent
                String statusMessage = (pinPercent < 100) ? " (Status: 0 - unavailable)" : " (Status: 1 - available)";

                return ResponseEntity.ok(
                        ApiResponse.success("Pin slot updated successfully",
                                "PinID: " + pinID + " updated to " + pinPercent + "%, Health: " + pinHealth + "%" + statusMessage));
            } else {
                // Cập nhật thất bại (pin slot không tồn tại)
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to update pin slot. PinID may not exist."));
            }

        } catch (Exception e) {
            // Xử lý lỗi và in ra console
            System.out.println("Error updating pin slot: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error updating pin slot: " + e.getMessage()));
        }
    }

    /**
     * API đặt chỗ pin slot 
     * Đặt slot cho user và vehicle cụ thể (chỉ khi slot available)
     * @param pinID - ID của pin slot cần đặt
     * @param userID - ID của user đặt slot (bắt buộc > 0)
     * @param vehicleID - ID của vehicle đặt slot (bắt buộc > 0)
     * @return ResponseEntity chứa kết quả đặt slot
     */
    @PutMapping("/pinSlot/reserve")
    @Operation(summary = "Reserve a pin slot", description = "Reserve a specific pin slot with userID and vehicleID if it's status is 1 (available) and pinSlotStatus is 1 (available).")
    public ResponseEntity<ApiResponse<Object>> reservePinSlot(
            @Parameter(description = "Pin slot ID to reserve", required = true) @RequestParam int pinID,
            @Parameter(description = "User ID reserving the slot", required = true) @RequestParam int userID,
            @Parameter(description = "Vehicle ID reserving the slot", required = true) @RequestParam int vehicleID) {
        try {
            // Kiểm tra tính hợp lệ của userID
            if (userID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("User ID must be greater than 0"));
            }
            // Kiểm tra tính hợp lệ của vehicleID
            if (vehicleID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Vehicle ID must be greater than 0"));
            }

            // Gọi DAO để đặt chỗ pin slot
            boolean success = pinSlotDAO.reservePinSlot(pinID, userID, vehicleID);
            if (success) {
                // Đặt chỗ thành công
                return ResponseEntity.ok(ApiResponse.success("Pin slot reserved successfully", 
                    "PinID: " + pinID + " reserved for UserID: " + userID + ", VehicleID: " + vehicleID));
            } else {
                // Đặt chỗ thất bại (slot không tồn tại hoặc không available)
                return ResponseEntity.badRequest()
                        .body(ApiResponse
                                .error("Failed to reserve pin slot. PinID may not exist or is not available."));
            }

        } catch (Exception e) {
            // Xử lý lỗi và in ra console
            System.out.println("Error reserving pin slot: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error reserving pin slot: " + e.getMessage()));
        }
    }

    /**
     * API hủy đặt chỗ pin slot
     * Giải phóng slot đã đặt bằng cách đặt status về 0 và userID về null
     * @param pinID - ID của pin slot cần hủy đặt chỗ (bắt buộc)
     * @return ResponseEntity chứa kết quả hủy đặt chỗ
     */
    @PutMapping("/pinSlot/unreserve")
    @Operation(summary = "Unreserve a pin slot", description = "Unreserve a specific pin slot by setting status to 0 and userID to null.")
    public ResponseEntity<ApiResponse<Object>> unreservePinSlot(
            @Parameter(description = "Pin slot ID to unreserve", required = true) @RequestParam int pinID) {
        try {
            // Gọi DAO để hủy đặt chỗ pin slot
            boolean success = pinSlotDAO.unreservePinSlot(pinID);
            
            // Kiểm tra kết quả hủy đặt chỗ
            if (success) {
                // Hủy đặt chỗ thành công
                return ResponseEntity.ok(ApiResponse.success("Pin slot unreserved successfully", pinID));
            } else {
                // Hủy đặt chỗ thất bại (slot không tồn tại hoặc chưa được đặt)
                return ResponseEntity.badRequest()
                        .body(ApiResponse
                                .error("Failed to unreserve pin slot. PinID may not exist or is not currently reserved."));
            }

        } catch (Exception e) {
            // Xử lý lỗi và in ra console
            System.out.println("Error unreserving pin slot: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error unreserving pin slot: " + e.getMessage()));
        }
    }

    /**
     * API cập nhật trạng thái pin slot
     * Thay đổi status của slot cụ thể (available/unavailable/maintenance)
     * @param pinID - ID của pin slot cần cập nhật trạng thái (bắt buộc)
     * @param status - Trạng thái mới của slot (bắt buộc)
     * @return ResponseEntity chứa kết quả cập nhật trạng thái
     */
    @PutMapping("/pinSlot/updateStatus")
    @Operation(summary = "Update pin slot status", description = "Update the status of a specific pin slot.")
    public ResponseEntity<ApiResponse<Object>> updatePinSlotStatus(
            @Parameter(description = "Pin slot ID to update", required = true) @RequestParam int pinID,
            @Parameter(description = "New status for the pin slot", required = true) @RequestParam int status) {

        try {
            // Gọi DAO để cập nhật trạng thái pin slot
            boolean success = pinSlotDAO.updatePinSlotStatus(pinID, status);
            
            // Kiểm tra kết quả cập nhật
            if (success) {
                // Cập nhật trạng thái thành công
                return ResponseEntity.ok(ApiResponse.success("Pin slot status updated successfully", pinID));
            } else {
                // Cập nhật trạng thái thất bại (slot không tồn tại)
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to update pin slot status. PinID may not exist."));
            }

        } catch (Exception e) {
            // Xử lý lỗi và in ra console
            System.out.println("Error updating pin slot status: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error updating pin slot status: " + e.getMessage()));
        }
    }

    /**
     * API hoán đổi dữ liệu pin giữa 2 slot
     * Trao đổi giá trị pinPercent và pinHealth giữa 2 pin slots
     * @param pinSlotID1 - ID của pin slot thứ nhất (bắt buộc > 0)
     * @param pinSlotID2 - ID của pin slot thứ hai (bắt buộc > 0, khác pinSlotID1)
     * @return ResponseEntity chứa kết quả hoán đổi dữ liệu
     */
    // API để swap pin data giữa 2 PinSlot
    @PostMapping("/pinSlot/swap")
    @Operation(summary = "Swap pin data between two PinSlots", description = "Exchange pinPercent and pinHealth values between two pin slots")
    public ResponseEntity<ApiResponse<Object>> swapPinSlotData(
            @Parameter(description = "First Pin Slot ID to swap data with", required = true) @RequestParam int pinSlotID1,
            @Parameter(description = "Second Pin Slot ID to swap data with", required = true) @RequestParam int pinSlotID2) {
        try {
            // Kiểm tra tính hợp lệ của pinSlotID1
            if (pinSlotID1 <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("First Pin Slot ID must be greater than 0"));
            }
            // Kiểm tra tính hợp lệ của pinSlotID2
            if (pinSlotID2 <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Second Pin Slot ID must be greater than 0"));
            }
            // Kiểm tra 2 slot ID phải khác nhau
            if (pinSlotID1 == pinSlotID2) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Cannot swap pin data with the same PinSlot. Pin Slot IDs must be different"));
            }

            // Gọi DAO để thực hiện hoán đổi dữ liệu pin
            boolean success = pinSlotDAO.swapPinSlotData(pinSlotID1, pinSlotID2);
            
            // Kiểm tra kết quả hoán đổi
            if (success) {
                // Hoán đổi thành công
                return ResponseEntity.ok(ApiResponse.success("Pin data swapped successfully",
                        "pinPercent and pinHealth values have been exchanged between PinSlot ID " + pinSlotID1 + " and PinSlot ID " + pinSlotID2));
            } else {
                // Hoán đổi thất bại (một hoặc cả 2 slot không tồn tại)
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to swap pin data. Check if both PinSlots exist"));
            }
        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Database error in swapPinSlotData: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi chung
            System.out.println("Unexpected error in swapPinSlotData: " + e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error("Unexpected error occurred: " + e.getMessage()));
        }
    }

    
}
