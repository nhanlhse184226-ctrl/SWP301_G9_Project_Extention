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

import com.example.demo.dao.TransactionDAO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.TransactionDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller quản lý các giao dịch thanh toán
 * Cung cấp API để tạo, cập nhật, theo dõi giao dịch và phiên sạc pin
 * Hỗ trợ mainflow: tạo transaction -> staff approve -> pin swap
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Transaction Management", description = "APIs for managing payment transactions and charging sessions")
public class TransactionController {

    // Khởi tạo DAO để truy cập database
    private TransactionDAO transactionDAO = new TransactionDAO();

    /**
     * API lấy tất cả giao dịch trong hệ thống
     * Trả về toàn bộ transactions từ database (dành cho admin)
     * @return ResponseEntity chứa danh sách tất cả transactions
     */
    // API để lấy tất cả transactions
    @GetMapping("/transaction/list")
    @Operation(summary = "Get all transactions", description = "Retrieve all payment transactions from the database with full details.")
    public ResponseEntity<ApiResponse<Object>> getAllTransactions() {
        try {
            // Gọi DAO để lấy tất cả transactions từ database
            List<TransactionDTO> listTransaction = transactionDAO.listTransaction();

            // Kiểm tra kết quả và trả về response tương ứng
            if (listTransaction != null && !listTransaction.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Get all transactions successfully", listTransaction));
            } else {
                return ResponseEntity.ok(ApiResponse.success("No transactions found", listTransaction));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Error at TransactionController getAllTransactions: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * API lấy transactions theo user ID (MAINFLOW - Xem lịch sử)
     * Trả về tất cả giao dịch của user qua vehicles của họ
     * @param userID - ID của user cần xem lịch sử (bắt buộc > 0)
     * @return ResponseEntity chứa danh sách transactions của user
     */
    // API để lấy danh sách transaction theo userID
    @GetMapping("/transaction/getByUser")
    @Operation(summary = "Get transactions by user ID", description = "Retrieve all transactions for a specific user through their vehicles, ordered by creation date (newest first).")
    public ResponseEntity<ApiResponse<Object>> getTransactionsByUser(
            @Parameter(description = "User ID to get transactions for", required = true) @RequestParam int userID) {
        try {
            // Kiểm tra tính hợp lệ của userID
            if (userID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("User ID must be greater than 0"));
            }

            // Gọi DAO để lấy transactions của user qua vehicles
            List<TransactionDTO> listTransaction = transactionDAO.getTransactionsByUser(userID);

            // Kiểm tra kết quả và trả về response tương ứng
            if (listTransaction != null && !listTransaction.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Get transactions for user successfully", listTransaction));
            } else {
                return ResponseEntity.ok(ApiResponse.success("No transactions found for user ID: " + userID, listTransaction));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Error at TransactionController getTransactionsByUser: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * API lấy transactions theo vehicle ID
     * Trả về tất cả giao dịch của một xe cụ thể
     * @param vehicleID - ID của vehicle cần xem lịch sử (bắt buộc > 0)
     * @return ResponseEntity chứa danh sách transactions của vehicle
     */
    // API để lấy danh sách transaction theo vehicleID
    @GetMapping("/transaction/getByVehicle")
    @Operation(summary = "Get transactions by vehicle ID", description = "Retrieve all transactions for a specific vehicle, ordered by creation date (newest first).")
    public ResponseEntity<ApiResponse<Object>> getTransactionsByVehicle(
            @Parameter(description = "Vehicle ID to get transactions for", required = true) @RequestParam int vehicleID) {
        try {
            // Kiểm tra tính hợp lệ của vehicleID
            if (vehicleID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Vehicle ID must be greater than 0"));
            }

            // Gọi DAO để lấy tất cả transactions của vehicle này
            // Sắp xếp theo thời gian tạo (mới nhất trước)
            List<TransactionDTO> listTransaction = transactionDAO.getTransactionsByVehicle(vehicleID);

            // Kiểm tra kết quả và trả về response tương ứng
            if (listTransaction != null && !listTransaction.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Get transactions for vehicle successfully", listTransaction));
            } else {
                // Vehicle chưa có transaction nào (xe chưa từng sạc)
                return ResponseEntity.ok(ApiResponse.success("No transactions found for vehicle ID: " + vehicleID, listTransaction));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database và in ra console để debug
            System.out.println("Error at TransactionController getTransactionsByVehicle: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * API lấy transactions theo station ID
     * Trả về tất cả giao dịch tại một trạm sạc cụ thể (dành cho staff/admin)
     * @param stationID - ID của trạm sạc cần xem lịch sử (bắt buộc > 0)
     * @return ResponseEntity chứa danh sách transactions tại station
     */
    // API để lấy danh sách transaction theo stationID
    @GetMapping("/transaction/getByStation")
    @Operation(summary = "Get transactions by station ID", description = "Retrieve all transactions for a specific station, ordered by creation date (newest first).")
    public ResponseEntity<ApiResponse<Object>> getTransactionsByStation(
            @Parameter(description = "Station ID to get transactions for", required = true) @RequestParam int stationID) {
        try {
            // Kiểm tra tính hợp lệ của stationID
            if (stationID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Station ID must be greater than 0"));
            }

            // Gọi DAO để lấy tất cả transactions tại station này
            // Bao gồm tất cả pin slots thuộc station
            List<TransactionDTO> listTransaction = transactionDAO.getTransactionsByStation(stationID);

            // Kiểm tra kết quả và trả về response tương ứng
            if (listTransaction != null && !listTransaction.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Get transactions for station successfully", listTransaction));
            } else {
                // Station chưa có transaction nào (trạm mới hoặc chưa hoạt động)
                return ResponseEntity.ok(ApiResponse.success("No transactions found for station ID: " + stationID, listTransaction));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database và in ra console để debug
            System.out.println("Error at TransactionController getTransactionsByStation: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * API lấy transaction theo ID cụ thể
     * Trả về chi tiết một giao dịch dựa trên transaction ID
     * @param transactionID - ID của transaction cần xem chi tiết (bắt buộc > 0)
     * @return ResponseEntity chứa thông tin chi tiết transaction
     */
    // API để lấy transaction theo ID
    @GetMapping("/transaction/getById")
    @Operation(summary = "Get transaction by ID", description = "Retrieve a specific transaction by its transaction ID.")
    public ResponseEntity<ApiResponse<Object>> getTransactionById(
            @Parameter(description = "Transaction ID to retrieve", required = true) @RequestParam int transactionID) {
        try {
            // Kiểm tra tính hợp lệ của transactionID
            if (transactionID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Transaction ID must be greater than 0"));
            }

            // Gọi DAO để lấy transaction cụ thể theo ID
            // Bao gồm tất cả thông tin chi tiết: vehicle, station, pin, amount...
            TransactionDTO transaction = transactionDAO.getTransactionById(transactionID);

            // Kiểm tra kết quả và trả về response tương ứng
            if (transaction != null) {
                return ResponseEntity.ok(ApiResponse.success("Get transaction successfully", transaction));
            } else {
                // Transaction không tồn tại với ID này
                return ResponseEntity.ok(ApiResponse.success("Transaction not found with ID: " + transactionID, null));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database và in ra console để debug
            System.out.println("Error at TransactionController getTransactionById: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * API tạo giao dịch mới (MAINFLOW - Tạo transaction)
     * Tạo transaction sau khi user đặt slot, trước khi thanh toán
     * Validates: Vehicle phải thuộc active driver, PinSlot phải thuộc Station
     * @param userID - ID chủ sở hữu vehicle (bắt buộc > 0)
     * @param vehicleID - ID vehicle (phải thuộc active driver với role=1) (bắt buộc > 0)
     * @param amount - Số tiền giao dịch (bắt buộc)
     * @param pack - ID gói dịch vụ (bắt buộc)
     * @param stationID - ID trạm sạc (phải khớp với station của pinSlot) (bắt buộc > 0)
     * @param pinID - ID pin slot (phải thuộc station specified) (bắt buộc > 0)
     * @param status - Trạng thái ban đầu (0=pending, 1=completed, 2=failed)
     * @return ResponseEntity chứa kết quả tạo giao dịch
     */
    // API để tạo transaction mới
    @PostMapping("/transaction/create")
    @Operation(summary = "Create new transaction", description = "Create a new payment transaction with validation: Vehicle must belong to active driver, PinSlot must belong to the specified Station. Auto-generates timestamp and 1-hour expiration.")
    public ResponseEntity<ApiResponse<Object>> createTransaction(
            @Parameter(description = "User ID (owner of the vehicle)", required = true) @RequestParam int userID,
            @Parameter(description = "Vehicle ID (must belong to active driver with role=1)", required = true) @RequestParam int vehicleID,
            @Parameter(description = "Transaction amount", required = true) @RequestParam int amount,
            @Parameter(description = "Package ID", required = true) @RequestParam int pack,
            @Parameter(description = "Station ID (must match the station of the pinSlot)", required = true) @RequestParam int stationID,
            @Parameter(description = "Pin slot ID (must belong to the specified station)", required = true) @RequestParam int pinID,
            @Parameter(description = "Initial transaction status (0=pending, 1=completed, 2=failed)", required = false) @RequestParam(defaultValue = "0") int status) {

        try {
            // Kiểm tra tính hợp lệ của userID
            if (userID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("User ID must be greater than 0"));
            }
            // Kiểm tra tính hợp lệ của vehicleID
            if (vehicleID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Vehicle ID must be greater than 0"));
            }
            // Kiểm tra tính hợp lệ của stationID
            if (stationID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Station ID must be greater than 0"));
            }
            // Kiểm tra tính hợp lệ của pinID
            if (pinID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Pin ID must be greater than 0"));
            }
            // Kiểm tra tính hợp lệ của status
            if (status < 0 || status > 2) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Status must be 0 (pending), 1 (completed), or 2 (failed)"));
            }

            // Gọi DAO để tạo transaction với validation đầy đủ
            boolean success = transactionDAO.createTransactionWithUserAndVehicle(userID, vehicleID, amount, pack, stationID, pinID, status);

            // Kiểm tra kết quả tạo transaction
            if (success) {
                // Tạo thành công
                return ResponseEntity.ok(ApiResponse.success("Transaction created successfully with userID and vehicleID", 
                    "UserID: " + userID + ", VehicleID: " + vehicleID + ", Amount: " + amount + ", Pack: " + pack + 
                    ", StationID: " + stationID + ", PinID: " + pinID + ", Status: " + status));
            } else {
                // Tạo thất bại (validation không pass hoặc lỗi database)
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create transaction"));
            }

        } catch (SQLException e) {
            // Xử lý lỗi SQL và in stack trace để debug
            System.out.println("SQLException at TransactionController createTransaction: " + e.toString());
            e.printStackTrace(); // In full stack trace để debug
            return ResponseEntity.internalServerError().body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi chung và in stack trace để debug
            System.out.println("General Exception at TransactionController createTransaction: " + e.toString());
            e.printStackTrace(); // In full stack trace để debug
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error: " + e.getMessage()));
        }
    }

    /**
     * API cập nhật trạng thái giao dịch (MAINFLOW - Staff approve)
     * Staff chuyển transaction từ pending (0) sang completed (1)
     * @param transactionID - ID giao dịch cần cập nhật (bắt buộc > 0)
     * @param status - Trạng thái mới (0=pending, 1=completed, 2=expired, 3=canceled)
     * @return ResponseEntity chứa kết quả cập nhật trạng thái
     */
    // API để update status của transaction
    @PutMapping("/transaction/updateStatus")
    @Operation(summary = "Update transaction status", description = "Update the status of a specific transaction (0=pending, 1=completed, 2=failed).")
    public ResponseEntity<ApiResponse<Object>> updateTransactionStatus(
            @Parameter(description = "Transaction ID to update", required = true) @RequestParam int transactionID,
            @Parameter(description = "New status (0=pending, 1=completed, 2=expired, 3=canceled)", required = true) @RequestParam int status) {

        try {
            // Kiểm tra tính hợp lệ của transactionID
            if (transactionID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Transaction ID must be greater than 0"));
            }
            // Kiểm tra tính hợp lệ của status
            if (status < 0 || status > 3) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Status must be 0 (pending), 1 (completed), 2 (expired), or 3 (canceled)"));
            }

            // Gọi DAO để cập nhật trạng thái transaction
            boolean success = transactionDAO.updateTransactionStatus(transactionID, status);

            // Kiểm tra kết quả cập nhật
            if (success) {
                // Cập nhật thành công - tạo status text để hiển thị
                String statusText = (status == 0) ? "pending" : (status == 1) ? "completed" : "failed";
                return ResponseEntity.ok(ApiResponse.success("Transaction status updated successfully", 
                    "TransactionID: " + transactionID + " updated to status: " + status + " (" + statusText + ")"));
            } else {
                // Cập nhật thất bại (transaction không tồn tại)
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update transaction status. Transaction ID may not exist."));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Error at TransactionController updateTransactionStatus: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * API manual trigger cập nhật transactions hết hạn
     * Admin có thể chạy thủ công để cập nhật các giao dịch đã quá hạn
     * @return ResponseEntity chứa kết quả cập nhật transactions hết hạn
     */
    // API để manual trigger update expired transactions
    @GetMapping("/transaction/updateExpired")
    @Operation(summary = "Update expired transactions", description = "Manually trigger the stored procedure to update all expired transactions.")
    public ResponseEntity<ApiResponse<Object>> updateExpiredTransactionsManual() {
        try {
            // Gọi DAO để thực thi stored procedure UpdateExpiredTransactions
            // Procedure sẽ tự động tìm và cập nhật status các transactions quá 1 giờ
            boolean success = transactionDAO.updateExpiredTransactions();

            // Kiểm tra kết quả thực thi procedure
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("Expired transactions updated successfully", "UpdateExpiredTransactions procedure executed"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update expired transactions"));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database và in ra console để debug
            System.out.println("Error at TransactionController updateExpiredTransactionsManual: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * Scheduled task tự động cập nhật transactions hết hạn
     * Chạy mỗi 1 phút để kiểm tra và cập nhật transactions quá hạn
     * Đảm bảo hệ thống tự động dọn dẹp các giao dịch không hoàn thành
     */
    // Scheduled task - chạy mỗi phút (60000ms = 1 phút) để update expired transactions
    @Scheduled(fixedRate = 60000)
    public void updateExpiredTransactionsScheduled() {
        try {
            // Tự động gọi DAO để cập nhật transactions hết hạn
            // Thực thi stored procedure mỗi phút để dọn dẹp transactions cũ
            boolean success = transactionDAO.updateExpiredTransactions();

            // In log để tracking hoạt động của scheduled task
            if (success) {
                System.out.println("Scheduled update: UpdateExpiredTransactions procedure executed successfully at " + new java.util.Date());
            } else {
                System.out.println("Scheduled update: UpdateExpiredTransactions procedure execution failed at " + new java.util.Date());
            }

        } catch (SQLException e) {
            // Xử lý lỗi và in ra console (không throw exception để không crash scheduler)
            System.out.println("Scheduled update error: " + e.toString());
        }
    }

    /**
     * API kiểm tra trạng thái dịch vụ transaction
     * Xác nhận service đang hoạt động và scheduled task đang chạy
     * @return ResponseEntity chứa thông tin trạng thái service
     */
    // API để kiểm tra service status
    @GetMapping("/transaction/status")
    @Operation(summary = "Check transaction service status", description = "Check if the transaction management service is running with scheduled updates.")
    public ResponseEntity<ApiResponse<Object>> getTransactionServiceStatus() {
        // Trả về thông tin service đang hoạt động
        // Bao gồm thông tin về scheduled task cleanup transactions
        return ResponseEntity.ok(ApiResponse.success("Transaction service is running", 
            "Scheduled expired transaction cleanup every 1 minute"));
    }

}