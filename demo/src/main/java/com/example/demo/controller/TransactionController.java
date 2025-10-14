package com.example.demo.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.dao.TransactionDAO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.TransactionDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Transaction Management", description = "APIs for managing payment transactions and charging sessions")
public class TransactionController {

    private TransactionDAO transactionDAO = new TransactionDAO();

    // API để lấy tất cả transactions
    @GetMapping("/transaction/list")
    @Operation(summary = "Get all transactions", description = "Retrieve all payment transactions from the database with full details.")
    public ResponseEntity<ApiResponse<Object>> getAllTransactions() {
        try {
            List<TransactionDTO> listTransaction = transactionDAO.listTransaction();

            if (listTransaction != null && !listTransaction.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Get all transactions successfully", listTransaction));
            } else {
                return ResponseEntity.ok(ApiResponse.success("No transactions found", listTransaction));
            }

        } catch (SQLException e) {
            System.out.println("Error at TransactionController getAllTransactions: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    // API để lấy danh sách transaction theo userID
    @GetMapping("/transaction/getByUser")
    @Operation(summary = "Get transactions by user ID", description = "Retrieve all transactions for a specific user, ordered by creation date (newest first).")
    public ResponseEntity<ApiResponse<Object>> getTransactionsByUser(
            @Parameter(description = "User ID to get transactions for", required = true) @RequestParam int userID) {
        try {
            // Validation
            if (userID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("User ID must be greater than 0"));
            }

            List<TransactionDTO> listTransaction = transactionDAO.getTransactionsByUser(userID);

            if (listTransaction != null && !listTransaction.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Get transactions for user successfully", listTransaction));
            } else {
                return ResponseEntity.ok(ApiResponse.success("No transactions found for user ID: " + userID, listTransaction));
            }

        } catch (SQLException e) {
            System.out.println("Error at TransactionController getTransactionsByUser: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    // API để lấy transaction theo ID
    @GetMapping("/transaction/getById")
    @Operation(summary = "Get transaction by ID", description = "Retrieve a specific transaction by its transaction ID.")
    public ResponseEntity<ApiResponse<Object>> getTransactionById(
            @Parameter(description = "Transaction ID to retrieve", required = true) @RequestParam int transactionID) {
        try {
            // Validation
            if (transactionID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Transaction ID must be greater than 0"));
            }

            TransactionDTO transaction = transactionDAO.getTransactionById(transactionID);

            if (transaction != null) {
                return ResponseEntity.ok(ApiResponse.success("Get transaction successfully", transaction));
            } else {
                return ResponseEntity.ok(ApiResponse.success("Transaction not found with ID: " + transactionID, null));
            }

        } catch (SQLException e) {
            System.out.println("Error at TransactionController getTransactionById: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    // API để tạo transaction mới
    @PostMapping("/transaction/create")
    @Operation(summary = "Create new transaction", description = "Create a new payment transaction with validation: User must be driver (role=1), PinSlot must belong to the specified Station. Auto-generates timestamp and 1-hour expiration.")
    public ResponseEntity<ApiResponse<Object>> createTransaction(
            @Parameter(description = "Driver ID (must have role=1 and be active)", required = true) @RequestParam int userID,
            @Parameter(description = "Transaction amount", required = true) @RequestParam int amount,
            @Parameter(description = "Package ID", required = true) @RequestParam int pack,
            @Parameter(description = "Station ID (must match the station of the pinSlot)", required = true) @RequestParam int stationID,
            @Parameter(description = "Pin slot ID (must belong to the specified station)", required = true) @RequestParam int pinID,
            @Parameter(description = "Initial transaction status (0=pending, 1=completed, 2=failed)", required = false) @RequestParam(defaultValue = "0") int status) {

        try {
            // Validation
            if (userID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("User ID must be greater than 0"));
            }
            if (amount <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Amount must be greater than 0"));
            }
            if (stationID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Station ID must be greater than 0"));
            }
            if (pinID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Pin ID must be greater than 0"));
            }
            if (status < 0 || status > 2) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Status must be 0 (pending), 1 (completed), or 2 (failed)"));
            }

            boolean success = transactionDAO.createTransaction(userID, amount, pack, stationID, pinID, status);

            if (success) {
                return ResponseEntity.ok(ApiResponse.success("Transaction created successfully", 
                    "UserID: " + userID + ", Amount: " + amount + ", Pack: " + pack + 
                    ", StationID: " + stationID + ", PinID: " + pinID + ", Status: " + status));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create transaction"));
            }

        } catch (SQLException e) {
            System.out.println("SQLException at TransactionController createTransaction: " + e.toString());
            e.printStackTrace(); // In full stack trace để debug
            return ResponseEntity.internalServerError().body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("General Exception at TransactionController createTransaction: " + e.toString());
            e.printStackTrace(); // In full stack trace để debug
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error: " + e.getMessage()));
        }
    }

    // API để update status của transaction
    @PutMapping("/transaction/updateStatus")
    @Operation(summary = "Update transaction status", description = "Update the status of a specific transaction (0=pending, 1=completed, 2=failed).")
    public ResponseEntity<ApiResponse<Object>> updateTransactionStatus(
            @Parameter(description = "Transaction ID to update", required = true) @RequestParam int transactionID,
            @Parameter(description = "New status (0=pending, 1=completed, 2=expired, 3=canceled)", required = true) @RequestParam int status) {

        try {
            // Validation
            if (transactionID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Transaction ID must be greater than 0"));
            }
            if (status < 0 || status > 2) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Status must be 0 (pending), 1 (completed), or 2 (failed)"));
            }

            boolean success = transactionDAO.updateTransactionStatus(transactionID, status);

            if (success) {
                String statusText = (status == 0) ? "pending" : (status == 1) ? "completed" : "failed";
                return ResponseEntity.ok(ApiResponse.success("Transaction status updated successfully", 
                    "TransactionID: " + transactionID + " updated to status: " + status + " (" + statusText + ")"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update transaction status. Transaction ID may not exist."));
            }

        } catch (SQLException e) {
            System.out.println("Error at TransactionController updateTransactionStatus: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    // API để manual trigger update expired transactions
    @GetMapping("/transaction/updateExpired")
    @Operation(summary = "Update expired transactions", description = "Manually trigger the stored procedure to update all expired transactions.")
    public ResponseEntity<ApiResponse<Object>> updateExpiredTransactionsManual() {
        try {
            boolean success = transactionDAO.updateExpiredTransactions();

            if (success) {
                return ResponseEntity.ok(ApiResponse.success("Expired transactions updated successfully", "UpdateExpiredTransactions procedure executed"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update expired transactions"));
            }

        } catch (SQLException e) {
            System.out.println("Error at TransactionController updateExpiredTransactionsManual: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    // Scheduled task - chạy mỗi phút (60000ms = 1 phút) để update expired transactions
    @Scheduled(fixedRate = 60000)
    public void updateExpiredTransactionsScheduled() {
        try {
            boolean success = transactionDAO.updateExpiredTransactions();

            if (success) {
                System.out.println("Scheduled update: UpdateExpiredTransactions procedure executed successfully at " + new java.util.Date());
            } else {
                System.out.println("Scheduled update: UpdateExpiredTransactions procedure execution failed at " + new java.util.Date());
            }

        } catch (SQLException e) {
            System.out.println("Scheduled update error: " + e.toString());
        }
    }

    // API để kiểm tra service status
    @GetMapping("/transaction/status")
    @Operation(summary = "Check transaction service status", description = "Check if the transaction management service is running with scheduled updates.")
    public ResponseEntity<ApiResponse<Object>> getTransactionServiceStatus() {
        return ResponseEntity.ok(ApiResponse.success("Transaction service is running", 
            "Scheduled expired transaction cleanup every 1 minute"));
    }

}