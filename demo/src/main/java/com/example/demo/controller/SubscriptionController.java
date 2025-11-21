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

/**
 * Controller quản lý subscription và số dư tài khoản user
 * Xử lý việc kiểm tra và trừ số dư khi user sử dụng dịch vụ
 * 
 * Phân quyền:
 * - roleID=1 (User): Xem subscription của chính mình
 * - roleID=2 (Staff): Không có quyền truy cập subscription
 * - roleID=3 (Admin): Có thể xem subscription của bất kỳ user nào
 * 
 * Workflow:
 * 1. User mua service pack -> tăng total trong subscription
 * 2. User đặt slot/pin -> trừ total (decrementTotal)
 * 3. User kiểm tra số dư còn lại qua getUserSubscription
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Subscription Management", description = "APIs for managing user subscriptions and account balance")
public class SubscriptionController {

    // Khởi tạo DAO để truy cập database
    private final SubscriptionDAO subscriptionDAO = new SubscriptionDAO();

    /**
     * API lấy thông tin subscription của user
     * User xem số dư tài khoản của mình
     * @param userID - ID của user cần kiểm tra subscription
     * @return ResponseEntity chứa thông tin subscription và số dư
     */
    // API để lấy subscription theo userID
    @GetMapping("/subscription/getUserSubscription")
    @Operation(summary = "Get user subscription", description = "Retrieve subscription information including total balance for a specific user.")
    public ResponseEntity<ApiResponse<Object>> getSubscriptionByUserId(
            @Parameter(description = "User ID to get subscription for", required = true, example = "123") @RequestParam int userID) {
        try {
            // Kiểm tra userID hợp lệ
            if (userID <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("User ID must be greater than 0"));
            }

            // Gọi DAO để lấy subscription info từ database
            // Bao gồm total balance, ngày tạo, ngày cập nhật...
            SubscriptionDTO subscription = subscriptionDAO.getSubscriptionByUserId(userID);
            
            // Kiểm tra kết quả và trả về response phù hợp
            if (subscription != null) {
                String message = "Subscription found for user " + userID;
                return ResponseEntity.ok(ApiResponse.success(message, subscription));
            } else {
                // User chưa có subscription (chưa mua gói nào)
                String message = "No subscription found for user " + userID;
                // Trả về success với data = null (không phải lỗi, chỉ là chưa có data)
                return ResponseEntity.ok(ApiResponse.success(message, null));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database và in ra console
            System.out.println("Database error in getSubscriptionByUserId: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi hệ thống khác
            System.out.println("Error getting subscription: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error getting subscription: " + e.getMessage()));
        }
    }

    /**
     * API trừ 1 từ total balance của user
     * Được gọi khi user đặt slot hoặc sử dụng dịch vụ
     * @param userID - ID của user cần trừ balance
     * @return ResponseEntity chứa kết quả trừ balance
     */
    // API để trừ 1 từ total của user
    @PostMapping("/subscription/decrementTotal")
    @Operation(summary = "Decrement user total", description = "Decrement the user's total balance by 1. Total will not go below 0.")
    public ResponseEntity<ApiResponse<Object>> decrementTotal(
            @Parameter(description = "User ID to decrement total for", required = true, example = "123") @RequestParam int userID) {
        try {
            // Kiểm tra userID hợp lệ
            if (userID <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("User ID must be greater than 0"));
            }

            // Gọi DAO để trừ 1 từ total balance
            // DAO sẽ kiểm tra total > 0 trước khi trừ
            // Nếu total = 0, sẽ không trừ nữa
            boolean success = subscriptionDAO.decrementTotal(userID);
            
            // Kiểm tra kết quả trừ balance
            if (success) {
                String message = "Total decremented successfully for user " + userID;
                return ResponseEntity.ok(ApiResponse.success(message, null));
            } else {
                // Có thể do user không tồn tại hoặc total đã = 0
                String message = "Total was not decremented. User " + userID + " may not exist or total is already 0";
                return ResponseEntity.ok(ApiResponse.success(message, null));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database và in ra console
            System.out.println("Database error in decrementTotal: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi hệ thống khác
            System.out.println("Error decrementing total: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error decrementing total: " + e.getMessage()));
        }
    }

    
}