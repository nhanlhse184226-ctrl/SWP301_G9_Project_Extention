package com.example.demo.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.PinSlotDTO;

public class PinSlotDAO {
    private static final String UPDATE = "EXEC dbo.UpdatePinPercent";
    private static final String LIST_PIN = "SELECT * FROM pinSlot";

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
                    String pinStatus = rs.getString("pinStatus");

                    // Đọc thêm reservation fields
                    String reserveStatus = rs.getString("reserveStatus");
                    Timestamp reserveTimeStamp = rs.getTimestamp("reserveTime");
                    LocalDateTime reserveTime = (reserveTimeStamp != null) ? reserveTimeStamp.toLocalDateTime() : null;

                    // Đọc stationID từ database
                    Integer stationID = rs.getObject("stationID", Integer.class);

                    // Sử dụng constructor với đầy đủ 6 fields
                    listPinSlot
                            .add(new PinSlotDTO(pinID, pinPercent, pinStatus, reserveStatus, reserveTime, stationID));
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

        String sql = "SELECT * FROM dbo.pinSlot WHERE stationID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, stationID);
                rs = ptm.executeQuery();

                while (rs.next()) {
                    int pinID = rs.getInt("pinID");
                    int pinPercent = rs.getInt("pinPercent");
                    String pinStatus = rs.getString("pinStatus");

                    // Đọc thêm reservation fields
                    String reserveStatus = rs.getString("reserveStatus");
                    Timestamp reserveTimeStamp = rs.getTimestamp("reserveTime");
                    LocalDateTime reserveTime = (reserveTimeStamp != null) ? reserveTimeStamp.toLocalDateTime() : null;

                    // Đọc stationID từ database
                    Integer stationIDFromDB = rs.getObject("stationID", Integer.class);

                    // Sử dụng constructor với đầy đủ 6 fields
                    listPinSlot.add(
                            new PinSlotDTO(pinID, pinPercent, pinStatus, reserveStatus, reserveTime, stationIDFromDB));
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

    // Method để reserve slot
    public boolean reserveSlot(int pinID) throws SQLException {
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;

        String sql = "UPDATE dbo.pinSlot SET reserveStatus = 'not ready', reserveTime = GETDATE() WHERE pinID = ? AND reserveStatus = 'ready'";

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

    // Method để update PinSlot theo pinID - cho Update Pin Slot API
    public boolean updatePinSlot(int pinID, int pinPercent) throws SQLException {
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;

        // Logic: Update pin và set status theo giá trị pin
        // Pin < 100% → status = "unvaliable"
        // Pin = 100% → status = "valiable"
        String sql = "UPDATE dbo.pinSlot SET pinPercent = ?, pinStatus = ? WHERE pinID = ?";
        String newStatus = (pinPercent < 100) ? "unvaliable" : "valiable";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, pinPercent);
                ptm.setString(2, newStatus);
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