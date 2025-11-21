package com.example.demo.controller;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.RatingDAO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.RatingDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller quản lý đánh giá và rating trạm sạc
 * Cho phép người dùng đánh giá trạm sạc từ 1-5 sao và xem thống kê rating
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Rating Management", description = "APIs for managing station ratings and user feedback")
public class RatingController {
    
    // Khởi tạo DAO để truy cập database
    private final RatingDAO ratingDAO = new RatingDAO();
    
    /**
     * API tạo rating mới cho trạm sạc
     * Cho phép user đánh giá trạm sạc từ 1-5 sao (có thể ẩn danh)
     * @param stationID - ID trạm sạc cần đánh giá (bắt buộc > 0)
     * @param rating - Số sao đánh giá từ 1-5 (bắt buộc)
     * @param userID - ID người dùng (tùy chọn, có thể null cho rating ẩn danh)
     * @return ResponseEntity chứa kết quả tạo rating
     */
    // API để tạo rating mới (bấm sao trên map)
    @PostMapping("/rating/create")
    @Operation(summary = "Create station rating", description = "Create a new star rating (1-5 stars) for a charging station. Can be anonymous or by registered user.")
    public ResponseEntity<ApiResponse<Object>> createRating(
            @Parameter(description = "Station ID to rate", required = true) @RequestParam int stationID,
            @Parameter(description = "Rating value (1-5 stars)", required = true, example = "5") @RequestParam int rating,
            @Parameter(description = "User ID (optional for anonymous rating)") @RequestParam(required = false) Integer userID) {
        
        try {
            // Kiểm tra stationID phải lớn hơn 0
            if (stationID <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Station ID must be greater than 0"));
            }
            
            // Kiểm tra rating phải từ 1-5 sao
            if (rating < 1 || rating > 5) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Rating must be between 1 and 5 stars"));
            }
            
            // Gọi DAO để lưu rating vào database
            boolean success = ratingDAO.createRating(stationID, userID, rating);
            
            // Kiểm tra kết quả tạo rating
            if (success) {
                // Tạo rating thành công - hiển thị thông tin user nếu có
                String userInfo = (userID != null) ? " by user " + userID : " (anonymous)";
                return ResponseEntity.ok(
                    ApiResponse.success("Rating created successfully", 
                        rating + " stars rating for station " + stationID + userInfo));
            } else {
                // Tạo rating thất bại
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create rating"));
            }
            
        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Database error in createRating: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Database error occurred: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi chung
            System.out.println("Error creating rating: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error creating rating: " + e.getMessage()));
        }
    }
    
    /**
     * API lấy danh sách ratings của trạm sạc
     * Trả về tất cả đánh giá và review của trạm cụ thể
     * @param stationID - ID trạm sạc cần xem rating (bắt buộc > 0)
     * @return ResponseEntity chứa danh sách ratings
     */
    // API để lấy danh sách ratings theo station
    @GetMapping("/rating/station/{stationID}")
    @Operation(summary = "Get station ratings", description = "Retrieve all individual ratings and reviews for a specific charging station.")
    public ResponseEntity<ApiResponse<Object>> getRatingsByStation(
            @Parameter(description = "Station ID to get ratings for", required = true) @PathVariable int stationID) {
        try {
            // Kiểm tra stationID phải lớn hơn 0
            if (stationID <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Station ID must be greater than 0"));
            }
            
            // Gọi DAO để lấy danh sách ratings từ database
            List<RatingDTO> ratings = ratingDAO.getRatingsByStation(stationID);
            
            // Trả về danh sách ratings (có thể rỗng)
            return ResponseEntity.ok(
                ApiResponse.success("Retrieved ratings successfully", ratings));
            
        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Database error in getRatingsByStation: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Database error occurred: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi chung
            System.out.println("Error getting ratings: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting ratings: " + e.getMessage()));
        }
    }
    
    /**
     * API lấy rating trung bình của trạm sạc
     * Tính toán và trả về điểm rating trung bình với format hiển thị
     * @param stationID - ID trạm sạc cần tính trung bình (bắt buộc > 0)
     * @return ResponseEntity chứa rating trung bình và format hiển thị
     */
    // API để lấy rating trung bình của station
    @GetMapping("/rating/average/{stationID}")
    @Operation(summary = "Get average station rating", description = "Calculate and retrieve the average star rating for a charging station with display formatting.")
    public ResponseEntity<ApiResponse<Object>> getAverageRating(
            @Parameter(description = "Station ID to calculate average for", required = true) @PathVariable int stationID) {
        try {
            // Kiểm tra stationID phải lớn hơn 0
            if (stationID <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Station ID must be greater than 0"));
            }
            
            // Gọi DAO để tính rating trung bình từ database
            double averageRating = ratingDAO.getAverageRating(stationID);
            
            // Tạo map chứa thông tin rating đã format
            Map<String, Object> result = new HashMap<>();
            result.put("stationID", stationID);
            result.put("averageRating", Math.round(averageRating * 100.0) / 100.0); // Làm tròn 2 chữ số thập phân
            result.put("displayRating", String.format("%.1f", averageRating) + " ⭐"); // Format hiển thị với icon sao
            
            // Trả về kết quả với thông tin đã format
            return ResponseEntity.ok(
                ApiResponse.success("Retrieved average rating successfully", result));
            
        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Database error in getAverageRating: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Database error occurred: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi chung
            System.out.println("Error getting average rating: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting average rating: " + e.getMessage()));
        }
    }
    
    /**
     * API lấy thống kê chi tiết rating theo số sao
     * Hiển thị số lượng đánh giá 1, 2, 3, 4, 5 sao của trạm
     * @param stationID - ID trạm sạc cần thống kê (bắt buộc > 0)
     * @return ResponseEntity chứa thống kê chi tiết từng loại rating
     */
    // API để lấy thống kê chi tiết rating (số lượng mỗi loại sao)
    @GetMapping("/rating/statistics/{stationID}")
    @Operation(summary = "Get rating statistics", description = "Get detailed rating breakdown showing count of 1-star, 2-star, 3-star, 4-star, and 5-star ratings for a station.")
    public ResponseEntity<ApiResponse<Object>> getRatingStatistics(
            @Parameter(description = "Station ID to get statistics for", required = true) @PathVariable int stationID) {
        try {
            // Kiểm tra stationID phải lớn hơn 0
            if (stationID <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Station ID must be greater than 0"));
            }
            
            // Gọi DAO để lấy thống kê từng loại sao từ database
            int[] statistics = ratingDAO.getRatingStatistics(stationID);
            // Lấy luôn rating trung bình
            double averageRating = ratingDAO.getAverageRating(stationID);
            
            // Tạo map chứa thống kê chi tiết
            Map<String, Object> result = new HashMap<>();
            result.put("stationID", stationID);
            result.put("averageRating", Math.round(averageRating * 100.0) / 100.0); // Rating trung bình
            result.put("oneStar", statistics[1]);     // Số lượng đánh giá 1 sao
            result.put("twoStar", statistics[2]);     // Số lượng đánh giá 2 sao
            result.put("threeStar", statistics[3]);   // Số lượng đánh giá 3 sao
            result.put("fourStar", statistics[4]);    // Số lượng đánh giá 4 sao
            result.put("fiveStar", statistics[5]);    // Số lượng đánh giá 5 sao
            // Tổng số đánh giá
            result.put("totalRatings", statistics[1] + statistics[2] + statistics[3] + statistics[4] + statistics[5]);
            
            // Trả về thống kê đầy đủ
            return ResponseEntity.ok(
                ApiResponse.success("Retrieved rating statistics successfully", result));
            
        } catch (SQLException e) {
            // Xử lý lỗi database
            System.out.println("Database error in getRatingStatistics: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Database error occurred: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi chung
            System.out.println("Error getting rating statistics: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting rating statistics: " + e.getMessage()));
        }
    }
    
    /**
     * API kiểm tra trạng thái dịch vụ rating
     * Trả về thông tin service đang chạy và sẵn sàng xử lý ratings
     * @return ResponseEntity chứa trạng thái dịch vụ
     */
    // API để kiểm tra status service
    @GetMapping("/rating/status")
    @Operation(summary = "Check rating service status", description = "Check if the rating service is running and available for processing station ratings.")
    public ResponseEntity<ApiResponse<Object>> getRatingStatus() {
        // Trả về thông tin service đang hoạt động
        return ResponseEntity.ok(
            ApiResponse.success("Rating service is running", 
                "Service ready to handle 5-star ratings for pin stations"));
    }
}