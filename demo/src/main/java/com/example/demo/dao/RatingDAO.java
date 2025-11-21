package com.example.demo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.RatingDTO;

/**
 * DAO quản lý đánh giá và rating cho các trạm sạc
 * Xử lý việc tạo, lấy và thống kê các rating của user
 * 
 * Database table: rating
 * - ratingID: ID tự động tăng (PK)
 * - stationID: ID trạm sạc được đánh giá (FK)
 * - userID: ID user đánh giá (FK, có thể null cho anonymous)
 * - rating: Số sao (1-5)
 * - createAt: Thời gian tạo
 * 
 * Business Rules:
 * - Mỗi user chỉ được rating 1 lần cho 1 station (user đăng nhập)
 * - Anonymous user có thể rating nhiều lần
 * - Rating phải từ 1-5 sao
 * - Station phải tồn tại trước khi rating
 */
public class RatingDAO {
    
    /**
     * Kiểm tra user đã rating station này chưa
     * Đảm bảo mỗi user chỉ được rating 1 lần cho 1 station
     * @param stationID ID trạm sạc cần kiểm tra
     * @param userID ID user (null nếu là anonymous)
     * @return true nếu user đã rating station này, false nếu chưa
     * @throws SQLException nếu có lỗi database
     */
    // Kiểm tra user đã rating station này chưa
    public boolean hasUserRatedStation(int stationID, Integer userID) throws SQLException {
        // Nếu userID = null -> anonymous user -> cho phép rating nhiều lần
        if (userID == null) return false; // Anonymous user có thể rating nhiều lần
        
        // Khởi tạo các đối tượng database connection
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        // SQL đếm số lượng rating của user cho station này
        String sql = "SELECT COUNT(*) FROM dbo.rating WHERE stationID = ? AND userID = ?";
        
        try {
            // Mở kết nối database
            conn = DBUtils.getConnection();
            
            // Kiểm tra kết nối thành công
            if (conn != null) {
                // Chuẩn bị prepared statement
                ptm = conn.prepareStatement(sql);
                
                // Gán stationID vào parameter đầu tiên
                ptm.setInt(1, stationID);
                
                // Gán userID vào parameter thứ hai
                ptm.setInt(2, userID);
                
                // Thực thi query đếm
                rs = ptm.executeQuery();
                
                // Kiểm tra có kết quả
                if (rs.next()) {
                    // Lấy giá trị COUNT(*) từ cột đầu tiên
                    // > 0 nghĩa là user đã rating station này rồi
                    return rs.getInt(1) > 0;
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return false;
    }
    
    /**
     * Kiểm tra station có tồn tại trong hệ thống không
     * Validation trước khi cho phép rating
     * @param stationID ID trạm sạc cần kiểm tra
     * @return true nếu station tồn tại, false nếu không
     * @throws SQLException nếu có lỗi database
     */
    // Kiểm tra station có tồn tại không
    public boolean isStationExists(int stationID) throws SQLException {
        // Khởi tạo các đối tượng database connection
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        // SQL đếm số lượng station với ID này trong bảng pinStation
        String sql = "SELECT COUNT(*) FROM dbo.pinStation WHERE stationID = ?";
        
        try {
            // Mở kết nối database
            conn = DBUtils.getConnection();
            
            // Kiểm tra kết nối thành công
            if (conn != null) {
                // Chuẩn bị prepared statement
                ptm = conn.prepareStatement(sql);
                
                // Gán stationID vào parameter
                ptm.setInt(1, stationID);
                
                // Thực thi query đếm
                rs = ptm.executeQuery();
                
                // Kiểm tra có kết quả
                if (rs.next()) {
                    // Lấy giá trị COUNT(*)
                    // > 0 nghĩa là station tồn tại
                    return rs.getInt(1) > 0;
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return false;
    }
    
    /**
     * Tạo rating mới cho station
     * Bao gồm validation đầy đủ và kiểm tra duplicate
     * @param stationID ID trạm sạc được đánh giá (phải > 0)
     * @param userID ID user đánh giá (null nếu anonymous)
     * @param rating Số sao (1-5)
     * @return true nếu tạo thành công, false nếu thất bại
     * @throws SQLException nếu validation không pass hoặc lỗi database
     */
    // Method để tạo rating mới
    public boolean createRating(int stationID, Integer userID, int rating) throws SQLException {
        // Kiểm tra stationID hợp lệ (phải là số dương)
        // Enhanced validation
        if (stationID <= 0) {
            throw new SQLException("Invalid station ID");
        }
        
        // Kiểm tra rating trong khoảng 1-5 sao
        if (rating < 1 || rating > 5) {
            throw new SQLException("Rating must be between 1 and 5 stars");
        }
        
        // Kiểm tra station có tồn tại trong hệ thống không
        // Gọi method isStationExists() để validate
        if (!isStationExists(stationID)) {
            throw new SQLException("Station with ID " + stationID + " does not exist");
        }
        
        // Kiểm tra user đã rating station này chưa (chỉ với user đã đăng nhập)
        // Anonymous user (userID = null) có thể rating nhiều lần
        if (userID != null && hasUserRatedStation(stationID, userID)) {
            throw new SQLException("User has already rated this station");
        }
        
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        
        String sql = "INSERT INTO dbo.rating (stationID, userID, rating, createAt) VALUES (?, ?, ?, GETDATE())";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, stationID);
                if (userID != null) {
                    ptm.setInt(2, userID);
                } else {
                    ptm.setNull(2, java.sql.Types.INTEGER);
                }
                ptm.setInt(3, rating);
                
                int rowsAffected = ptm.executeUpdate();
                success = (rowsAffected > 0);
                
                System.out.println("Create Rating - StationID: " + stationID + ", UserID: " + userID + ", Rating: " + rating + " stars, Rows affected: " + rowsAffected);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error in createRating: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error in createRating: " + e.getMessage());
            throw new SQLException("Failed to create rating: " + e.getMessage());
        } finally {
            if (ptm != null) {
                ptm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        
        return success;
    }
    
    // Method để lấy danh sách ratings theo stationID
    public List<RatingDTO> getRatingsByStation(int stationID) throws SQLException {
        List<RatingDTO> ratings = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT ratingID, stationID, userID, rating, createAt FROM dbo.rating WHERE stationID = ? ORDER BY createAt DESC";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, stationID);
                rs = ptm.executeQuery();
                
                while (rs.next()) {
                    RatingDTO rating = new RatingDTO(
                        rs.getInt("ratingID"),
                        rs.getInt("stationID"),
                        rs.getObject("userID", Integer.class), // Handle null userID
                        rs.getInt("rating"),
                        rs.getTimestamp("createAt")
                    );
                    ratings.add(rating);
                }
                
                System.out.println("Retrieved " + ratings.size() + " ratings for station ID: " + stationID);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error in getRatingsByStation: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error in getRatingsByStation: " + e.getMessage());
            throw new SQLException("Failed to get ratings: " + e.getMessage());
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ptm != null) {
                ptm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        
        return ratings;
    }
    
    // Method để tính rating trung bình theo stationID
    public double getAverageRating(int stationID) throws SQLException {
        double averageRating = 0.0;
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT AVG(CAST(rating AS FLOAT)) as avgRating, COUNT(*) as totalRatings FROM dbo.rating WHERE stationID = ?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, stationID);
                rs = ptm.executeQuery();
                
                if (rs.next()) {
                    averageRating = rs.getDouble("avgRating");
                    int totalRatings = rs.getInt("totalRatings");
                    
                    System.out.println("Station ID: " + stationID + " - Average Rating: " + String.format("%.2f", averageRating) + " (" + totalRatings + " ratings)");
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error in getAverageRating: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error in getAverageRating: " + e.getMessage());
            throw new SQLException("Failed to get average rating: " + e.getMessage());
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ptm != null) {
                ptm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        
        return averageRating;
    }
    
    // Method để lấy thống kê rating (số lượng rating theo từng sao)
    public int[] getRatingStatistics(int stationID) throws SQLException {
        int[] statistics = new int[6]; // Index 0 không dùng, 1-5 cho 1-5 sao
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT rating, COUNT(*) as count FROM dbo.rating WHERE stationID = ? GROUP BY rating ORDER BY rating";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, stationID);
                rs = ptm.executeQuery();
                
                while (rs.next()) {
                    int ratingValue = rs.getInt("rating");
                    int count = rs.getInt("count");
                    if (ratingValue >= 1 && ratingValue <= 5) {
                        statistics[ratingValue] = count;
                    }
                }
                
                System.out.println("Retrieved rating statistics for station ID: " + stationID);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error in getRatingStatistics: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error in getRatingStatistics: " + e.getMessage());
            throw new SQLException("Failed to get rating statistics: " + e.getMessage());
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ptm != null) {
                ptm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        
        return statistics;
    }
}