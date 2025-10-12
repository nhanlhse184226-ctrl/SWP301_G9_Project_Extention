package com.example.demo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.SubscriptionDTO;

public class SubscriptionDAO {

    public SubscriptionDTO getSubscriptionByUserId(int userID) throws SQLException {
        SubscriptionDTO sub = null;
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                String sql = "SELECT userID, total FROM Subscription WHERE userID = ?";
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, userID);
                rs = ptm.executeQuery();
                if (rs.next()) {
                    int total = rs.getInt("total");
                    sub = new SubscriptionDTO(userID, total);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error getting subscription by userID: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        return sub;
    }

    public boolean updateOrInsertTotal(int userID, int amount) throws SQLException {
        boolean check = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                // Check if subscription exists
                SubscriptionDTO existing = getSubscriptionByUserId(userID);
                
                if (existing != null) {
                    // Update existing subscription - add amount to current total
                    String sql = "UPDATE Subscription SET total = total + ? WHERE userID = ?";
                    ptm = conn.prepareStatement(sql);
                    ptm.setInt(1, amount);
                    ptm.setInt(2, userID);
                } else {
                    // Insert new subscription with amount as initial total
                    String sql = "INSERT INTO Subscription (userID, total) VALUES (?, ?)";
                    ptm = conn.prepareStatement(sql);
                    ptm.setInt(1, userID);
                    ptm.setInt(2, amount);
                }
                
                check = ptm.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error updating subscription total: " + e.getMessage());
        } finally {
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        return check;
    }
}