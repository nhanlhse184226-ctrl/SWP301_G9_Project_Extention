package com.example.demo.controller;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

@RestController
@RequestMapping("/api")
@Tag(name = "Rating Management", description = "APIs for managing station ratings and user feedback")
public class RatingController {
    
    private final RatingDAO ratingDAO = new RatingDAO();
    
    // API để tạo rating mới (bấm sao trên map)
    @PostMapping("/rating/create")
    @Operation(summary = "Create station rating", description = "Create a new star rating (1-5 stars) for a charging station. Can be anonymous or by registered user.")
    public ResponseEntity<ApiResponse<Object>> createRating(
            @Parameter(description = "Station ID to rate", required = true) @RequestParam int stationID,
            @Parameter(description = "Rating value (1-5 stars)", required = true, example = "5") @RequestParam int rating,
            @Parameter(description = "User ID (optional for anonymous rating)") @RequestParam(required = false) Integer userID) {
        
        try {
            // Validate input
            if (stationID <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Station ID must be greater than 0"));
            }
            
            if (rating < 1 || rating > 5) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Rating must be between 1 and 5 stars"));
            }
            
            // Gọi DAO để tạo rating
            boolean success = ratingDAO.createRating(stationID, userID, rating);
            
            if (success) {
                String userInfo = (userID != null) ? " by user " + userID : " (anonymous)";
                return ResponseEntity.ok(
                    ApiResponse.success("Rating created successfully", 
                        rating + " stars rating for station " + stationID + userInfo));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create rating"));
            }
            
        } catch (SQLException e) {
            System.out.println("Database error in createRating: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Database error occurred: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error creating rating: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error creating rating: " + e.getMessage()));
        }
    }
    
    // API để lấy danh sách ratings theo station
    @GetMapping("/rating/station/{stationID}")
    @Operation(summary = "Get station ratings", description = "Retrieve all individual ratings and reviews for a specific charging station.")
    public ResponseEntity<ApiResponse<Object>> getRatingsByStation(
            @Parameter(description = "Station ID to get ratings for", required = true) @PathVariable int stationID) {
        try {
            // Validate input
            if (stationID <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Station ID must be greater than 0"));
            }
            
            List<RatingDTO> ratings = ratingDAO.getRatingsByStation(stationID);
            
            return ResponseEntity.ok(
                ApiResponse.success("Retrieved ratings successfully", ratings));
            
        } catch (SQLException e) {
            System.out.println("Database error in getRatingsByStation: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Database error occurred: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error getting ratings: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting ratings: " + e.getMessage()));
        }
    }
    
    // API để lấy rating trung bình của station
    @GetMapping("/rating/average/{stationID}")
    @Operation(summary = "Get average station rating", description = "Calculate and retrieve the average star rating for a charging station with display formatting.")
    public ResponseEntity<ApiResponse<Object>> getAverageRating(
            @Parameter(description = "Station ID to calculate average for", required = true) @PathVariable int stationID) {
        try {
            // Validate input
            if (stationID <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Station ID must be greater than 0"));
            }
            
            double averageRating = ratingDAO.getAverageRating(stationID);
            
            Map<String, Object> result = new HashMap<>();
            result.put("stationID", stationID);
            result.put("averageRating", Math.round(averageRating * 100.0) / 100.0); // Round to 2 decimal places
            result.put("displayRating", String.format("%.1f", averageRating) + " ⭐");
            
            return ResponseEntity.ok(
                ApiResponse.success("Retrieved average rating successfully", result));
            
        } catch (SQLException e) {
            System.out.println("Database error in getAverageRating: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Database error occurred: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error getting average rating: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting average rating: " + e.getMessage()));
        }
    }
    
    // API để lấy thống kê chi tiết rating (số lượng mỗi loại sao)
    @GetMapping("/rating/statistics/{stationID}")
    @Operation(summary = "Get rating statistics", description = "Get detailed rating breakdown showing count of 1-star, 2-star, 3-star, 4-star, and 5-star ratings for a station.")
    public ResponseEntity<ApiResponse<Object>> getRatingStatistics(
            @Parameter(description = "Station ID to get statistics for", required = true) @PathVariable int stationID) {
        try {
            // Validate input
            if (stationID <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Station ID must be greater than 0"));
            }
            
            int[] statistics = ratingDAO.getRatingStatistics(stationID);
            double averageRating = ratingDAO.getAverageRating(stationID);
            
            Map<String, Object> result = new HashMap<>();
            result.put("stationID", stationID);
            result.put("averageRating", Math.round(averageRating * 100.0) / 100.0);
            result.put("oneStar", statistics[1]);
            result.put("twoStar", statistics[2]);
            result.put("threeStar", statistics[3]);
            result.put("fourStar", statistics[4]);
            result.put("fiveStar", statistics[5]);
            result.put("totalRatings", statistics[1] + statistics[2] + statistics[3] + statistics[4] + statistics[5]);
            
            return ResponseEntity.ok(
                ApiResponse.success("Retrieved rating statistics successfully", result));
            
        } catch (SQLException e) {
            System.out.println("Database error in getRatingStatistics: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Database error occurred: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error getting rating statistics: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting rating statistics: " + e.getMessage()));
        }
    }
    
    // API để kiểm tra status service
    @GetMapping("/rating/status")
    @Operation(summary = "Check rating service status", description = "Check if the rating service is running and available for processing station ratings.")
    public ResponseEntity<ApiResponse<Object>> getRatingStatus() {
        return ResponseEntity.ok(
            ApiResponse.success("Rating service is running", 
                "Service ready to handle 5-star ratings for pin stations"));
    }
}