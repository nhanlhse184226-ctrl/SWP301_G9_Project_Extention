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

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RatingController {
    
    private final RatingDAO ratingDAO = new RatingDAO();
    
    // API để tạo rating mới (bấm sao trên map)
    @PostMapping("/rating/create")
    public ResponseEntity<ApiResponse<Object>> createRating(
            @RequestParam int stationID,
            @RequestParam int rating,
            @RequestParam(required = false) Integer userID) {
        
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
    public ResponseEntity<ApiResponse<Object>> getRatingsByStation(@PathVariable int stationID) {
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
    public ResponseEntity<ApiResponse<Object>> getAverageRating(@PathVariable int stationID) {
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
    public ResponseEntity<ApiResponse<Object>> getRatingStatistics(@PathVariable int stationID) {
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
    public ResponseEntity<ApiResponse<Object>> getRatingStatus() {
        return ResponseEntity.ok(
            ApiResponse.success("Rating service is running", 
                "Service ready to handle 5-star ratings for pin stations"));
    }
}