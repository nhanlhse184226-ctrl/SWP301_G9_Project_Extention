package com.example.demo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.RatingDTO;

public class RatingDAO {
    
    // Method để tạo rating mới
    public boolean createRating(int stationID, Integer userID, int rating) throws SQLException {
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