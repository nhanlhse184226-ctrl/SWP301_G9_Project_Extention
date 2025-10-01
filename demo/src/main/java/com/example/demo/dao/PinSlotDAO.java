package com.example.demo.dao;

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
}