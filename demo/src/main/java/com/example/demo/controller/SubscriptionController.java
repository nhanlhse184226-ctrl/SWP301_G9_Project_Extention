package com.example.demo.controller;

import java.sql.SQLException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.SubscriptionDAO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.SubscriptionDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Subscription Management", description = "APIs for managing user subscriptions and account balance")
public class SubscriptionController {

    private final SubscriptionDAO subscriptionDAO = new SubscriptionDAO();

    // API để lấy subscription theo userID
    @GetMapping("/subscription/getUserSubscription")
    @Operation(summary = "Get user subscription", description = "Retrieve subscription information including total balance for a specific user.")
    public ResponseEntity<ApiResponse<Object>> getSubscriptionByUserId(
            @Parameter(description = "User ID to get subscription for", required = true, example = "123") @RequestParam int userID) {
        try {
            // Validation
            if (userID <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("User ID must be greater than 0"));
            }

            SubscriptionDTO subscription = subscriptionDAO.getSubscriptionByUserId(userID);
            
            if (subscription != null) {
                String message = "Subscription found for user " + userID;
                return ResponseEntity.ok(ApiResponse.success(message, subscription));
            } else {
                String message = "No subscription found for user " + userID;
                // Trả về success với data = null thay vì error vì đây là trường hợp bình thường
                return ResponseEntity.ok(ApiResponse.success(message, null));
            }

        } catch (SQLException e) {
            System.out.println("Database error in getSubscriptionByUserId: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error getting subscription: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error getting subscription: " + e.getMessage()));
        }
    }

    // API để trừ 1 từ total của user
    @PostMapping("/subscription/decrementTotal")
    @Operation(summary = "Decrement user total", description = "Decrement the user's total balance by 1. Total will not go below 0.")
    public ResponseEntity<ApiResponse<Object>> decrementTotal(
            @Parameter(description = "User ID to decrement total for", required = true, example = "123") @RequestParam int userID) {
        try {
            // Validation
            if (userID <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("User ID must be greater than 0"));
            }

            boolean success = subscriptionDAO.decrementTotal(userID);
            
            if (success) {
                String message = "Total decremented successfully for user " + userID;
                return ResponseEntity.ok(ApiResponse.success(message, null));
            } else {
                String message = "Total was not decremented. User " + userID + " may not exist or total is already 0";
                return ResponseEntity.ok(ApiResponse.success(message, null));
            }

        } catch (SQLException e) {
            System.out.println("Database error in decrementTotal: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error decrementing total: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error decrementing total: " + e.getMessage()));
        }
    }

    
}