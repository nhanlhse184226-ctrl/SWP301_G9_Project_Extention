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
    private static final String LIST_PIN = "SELECT pinID, pinPercent, pinStatus, status, stationID FROM pinSlot";

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
                // Vì có thể không có record nào có pinStatus = 0 (unvaliable)
                check = true; // ← Luôn return true nếu procedure execute thành công
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
                    int pinStatus = rs.getInt("pinStatus");
                    int status = rs.getInt("status");

                    // Đọc stationID từ database
                    Integer stationID = rs.getObject("stationID", Integer.class);

                    // Sử dụng constructor với 5 fields
                    listPinSlot
                            .add(new PinSlotDTO(pinID, pinPercent, pinStatus, status, stationID));
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // ← Thêm error logging
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

    // Method để lấy danh sách PinSlot theo stationID
    public List<PinSlotDTO> getListPinSlotByStation(int stationID) throws SQLException {
        List<PinSlotDTO> listPinSlot = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        String sql = "SELECT pinID, pinPercent, pinStatus, status, stationID FROM dbo.pinSlot WHERE stationID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, stationID);
                rs = ptm.executeQuery();

                while (rs.next()) {
                    int pinID = rs.getInt("pinID");
                    int pinPercent = rs.getInt("pinPercent");
                    int pinStatus = rs.getInt("pinStatus");
                    int status = rs.getInt("status");

                    // Đọc stationID từ database
                    Integer stationIDFromDB = rs.getObject("stationID", Integer.class);

                    // Sử dụng constructor với 5 fields
                    listPinSlot.add(
                            new PinSlotDTO(pinID, pinPercent, pinStatus, status, stationIDFromDB));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error getting pin slot list by station: " + e.getMessage());
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

    // Method để update PinSlot theo pinID - cho Update Pin Slot API
    public boolean updatePinSlot(int pinID, int pinPercent) throws SQLException {
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;

        // Logic: Update pin và set status theo giá trị pin
        // Pin < 100% → status = 0 (unvaliable)
        // Pin = 100% → status = 1 (valiable)
        String sql = "UPDATE dbo.pinSlot SET pinPercent = ?, status = ? WHERE pinID = ?";
        int newStatus = (pinPercent < 100) ? 0 : 1;

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, pinPercent);
                ptm.setInt(2, newStatus);
                ptm.setInt(3, pinID);

                int rowsAffected = ptm.executeUpdate();
                success = (rowsAffected > 0);

                System.out.println("Update PinSlot - PinID: " + pinID + ", NewPercent: " + pinPercent + "%, Status: "
                        + newStatus + ", Rows affected: " + rowsAffected);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error in updatePinSlot: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error in updatePinSlot: " + e.getMessage());
            throw new SQLException("Failed to update pin slot: " + e.getMessage());
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
}