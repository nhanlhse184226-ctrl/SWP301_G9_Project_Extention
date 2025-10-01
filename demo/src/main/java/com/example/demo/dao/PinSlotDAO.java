package com.example.demo.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.PinSlotDTO;

public class PinSlotDAO {
    private static final String UPDATE = "EXEC dbo.UpdatePinPercent";
    private static final String LIST_PIN = "SELECT * FROM dbo.pin"; 
    
    // Thêm method declaration
    public boolean updatePinPercent() throws SQLException {
        boolean check = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(UPDATE);
                int rowsAffected = ptm.executeUpdate();
                
                // Log để debug
                System.out.println("UpdatePinPercent executed - Rows affected: " + rowsAffected);
                
                // Procedure chạy thành công dù có 0 rows affected
                // Vì có thể không có record nào có pinStatus = 'unvaliable'
                check = true;  // ← Luôn return true nếu procedure execute thành công
            }
        } catch (Exception e) {
            System.out.println("Error executing UpdatePinPercent: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Failed to execute UpdatePinPercent: " + e.getMessage());
        } finally {
            if (ptm != null) {
                ptm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        
        return check;
    }

    public List<PinSlotDTO> getListPinSlot() throws SQLException {
        List<PinSlotDTO> listPinSlot = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(LIST_PIN);
                rs = ptm.executeQuery();
                while (rs.next()) {
                    int pinID = rs.getInt("pinID");
                    int pinPercent = rs.getInt("pinPercent");
                    String pinStatus = rs.getString("pinStatus");  // ← Sửa column name
                    listPinSlot.add(new PinSlotDTO(pinID, pinPercent, pinStatus));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();  // ← Thêm error logging
            throw new SQLException("Error getting pin slot list: " + e.getMessage());
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
        return listPinSlot;
    }
    
    // Method để reserve slot
    public boolean reserveSlot(int pinID) throws SQLException {
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        
        String sql = "UPDATE dbo.pin SET reserveStatus = 'not ready', reserveTime = GETDATE() WHERE pinID = ? AND reserveStatus = 'ready'";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, pinID);
                
                int rowsAffected = ptm.executeUpdate();
                success = (rowsAffected > 0);
                
                System.out.println("Reserve slot " + pinID + " - Rows affected: " + rowsAffected);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error in reserveSlot: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error in reserveSlot: " + e.getMessage());
            throw new SQLException("Failed to reserve slot: " + e.getMessage());
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
    
    // Method để reset tất cả reservations đã quá 60 phút
    public boolean resetExpiredReservations() throws SQLException {
        boolean success = false;
        Connection conn = null;
        CallableStatement cs = null;
        
        String sql = "{call ResetExpiredReservations_Test}";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                cs = conn.prepareCall(sql);
                
                cs.execute();
                success = true;
                
                System.out.println("Reset expired reservations (Test 1min) procedure executed successfully");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error in resetExpiredReservations: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error in resetExpiredReservations: " + e.getMessage());
            throw new SQLException("Failed to reset expired reservations: " + e.getMessage());
        } finally {
            if (cs != null) {
                cs.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        
        return success;
    }
}