package com.example.demo.controller;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.PaymentDAO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PaymentDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/payment")
@Tag(name = "Payment Management", description = "APIs for payment processing and management")
public class PaymentController {
    
    private PaymentDAO paymentDAO = new PaymentDAO();
    
    @PostMapping("/create")
    @Operation(summary = "Create new payment", description = "Create a new payment transaction")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payment data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<PaymentDTO>> createPayment(@RequestBody PaymentDTO paymentRequest) {
        try {
            // Validation
            if (paymentRequest.getUserID() <= 0) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("error", "UserID is required", null));
            }
            
            if (paymentRequest.getAmount() == null || paymentRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("error", "Amount must be greater than 0", null));
            }
            
            if (paymentRequest.getPaymentMethod() == null || paymentRequest.getPaymentMethod().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("error", "Payment method is required", null));
            }
            
            // Set default values
            paymentRequest.setPaymentStatus("pending");
            
            // Create payment
            int paymentID = paymentDAO.createPayment(paymentRequest);
            
            if (paymentID > 0) {
                // Retrieve created payment
                PaymentDTO createdPayment = paymentDAO.getPaymentById(paymentID);
                return ResponseEntity.ok(new ApiResponse<>("success", "Payment created successfully", createdPayment));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("error", "Failed to create payment", null));
            }
            
        } catch (SQLException e) {
            System.out.println("Database error in createPayment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("error", "Database error: " + e.getMessage(), null));
        } catch (Exception e) {
            System.out.println("Unexpected error in createPayment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("error", "Unexpected error: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/{paymentID}")
    @Operation(summary = "Get payment by ID", description = "Retrieve payment details by payment ID")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentById(
            @Parameter(description = "Payment ID", required = true) @PathVariable int paymentID) {
        try {
            if (paymentID <= 0) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("error", "Invalid payment ID", null));
            }
            
            PaymentDTO payment = paymentDAO.getPaymentById(paymentID);
            
            if (payment != null) {
                return ResponseEntity.ok(new ApiResponse<>("success", "Payment found", payment));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("error", "Payment not found", null));
            }
            
        } catch (SQLException e) {
            System.out.println("Database error in getPaymentById: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("error", "Database error: " + e.getMessage(), null));
        } catch (Exception e) {
            System.out.println("Unexpected error in getPaymentById: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("error", "Unexpected error: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/user/{userID}")
    @Operation(summary = "Get payments by user ID", description = "Retrieve all payments for a specific user")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getPaymentsByUserID(
            @Parameter(description = "User ID", required = true) @PathVariable int userID) {
        try {
            if (userID <= 0) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("error", "Invalid user ID", null));
            }
            
            List<PaymentDTO> payments = paymentDAO.getPaymentsByUserID(userID);
            
            return ResponseEntity.ok(new ApiResponse<>("success", "Payments retrieved successfully", payments));
            
        } catch (SQLException e) {
            System.out.println("Database error in getPaymentsByUserID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("error", "Database error: " + e.getMessage(), null));
        } catch (Exception e) {
            System.out.println("Unexpected error in getPaymentsByUserID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("error", "Unexpected error: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/list")
    @Operation(summary = "Get all payments", description = "Retrieve all payments (Admin only)")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getAllPayments() {
        try {
            List<PaymentDTO> payments = paymentDAO.getAllPayments();
            
            return ResponseEntity.ok(new ApiResponse<>("success", "All payments retrieved successfully", payments));
            
        } catch (SQLException e) {
            System.out.println("Database error in getAllPayments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("error", "Database error: " + e.getMessage(), null));
        } catch (Exception e) {
            System.out.println("Unexpected error in getAllPayments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("error", "Unexpected error: " + e.getMessage(), null));
        }
    }
    
    @PutMapping("/update-status")
    @Operation(summary = "Update payment status", description = "Update payment status and transaction ID")
    public ResponseEntity<ApiResponse<String>> updatePaymentStatus(
            @Parameter(description = "Payment ID", required = true) @RequestParam int paymentID,
            @Parameter(description = "New payment status", required = true) @RequestParam String status,
            @Parameter(description = "Transaction ID from payment gateway") @RequestParam(required = false) String transactionID) {
        try {
            if (paymentID <= 0) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("error", "Invalid payment ID", null));
            }
            
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("error", "Status is required", null));
            }
            
            // Validate status values
            if (!status.equals("pending") && !status.equals("completed") && !status.equals("failed") && !status.equals("cancelled")) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("error", "Invalid status. Must be: pending, completed, failed, or cancelled", null));
            }
            
            boolean success = paymentDAO.updatePaymentStatus(paymentID, status, transactionID);
            
            if (success) {
                return ResponseEntity.ok(new ApiResponse<>("success", "Payment status updated successfully", status));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("error", "Payment not found or update failed", null));
            }
            
        } catch (SQLException e) {
            System.out.println("Database error in updatePaymentStatus: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("error", "Database error: " + e.getMessage(), null));
        } catch (Exception e) {
            System.out.println("Unexpected error in updatePaymentStatus: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("error", "Unexpected error: " + e.getMessage(), null));
        }
    }
}