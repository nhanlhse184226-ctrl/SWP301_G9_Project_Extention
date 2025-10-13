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
    private static final String LIST_PIN = "SELECT pinID, pinPercent, pinHealth, pinStatus, status, userID, stationID FROM pinSlot";

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
                System.out.println("UpdatePinPercent executed - Rows affected: " + rowsAffected);
                check = true;
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
                    int pinHealth = rs.getInt("pinHealth");
                    int pinStatus = rs.getInt("pinStatus");
                    int status = rs.getInt("status");

                    // Đọc userID và stationID từ database
                    Integer userID = rs.getObject("userID", Integer.class);
                    int stationID = rs.getInt("stationID");

                    // Sử dụng constructor với đầy đủ fields bao gồm pinHealth
                    listPinSlot.add(new PinSlotDTO(pinID, pinPercent, pinHealth, pinStatus, status, userID, stationID));
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

        String sql = "SELECT pinID, pinPercent, pinHealth, pinStatus, status, userID, stationID FROM dbo.pinSlot WHERE stationID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, stationID);
                rs = ptm.executeQuery();

                while (rs.next()) {
                    int pinID = rs.getInt("pinID");
                    int pinPercent = rs.getInt("pinPercent");
                    int pinHealth = rs.getInt("pinHealth");
                    int pinStatus = rs.getInt("pinStatus");
                    int status = rs.getInt("status");

                    // Đọc userID và stationID từ database
                    Integer userID = rs.getObject("userID", Integer.class);
                    int stationIDFromDB = rs.getInt("stationID");

                    // Sử dụng constructor với đầy đủ fields bao gồm pinHealth
                    listPinSlot.add(new PinSlotDTO(pinID, pinPercent, pinHealth, pinStatus, status, userID, stationIDFromDB));
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
    public boolean updatePinSlot(int pinID, int pinPercent, int pinHealth) throws SQLException {
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;

        String sql = "UPDATE dbo.pinSlot SET pinPercent = ?, pinHealth = ? WHERE pinID = ?";
        int newStatus = (pinPercent < 100) ? 0 : 1;

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, pinPercent);
                ptm.setInt(2, pinHealth);
                ptm.setInt(3, pinID);

                int rowsAffected = ptm.executeUpdate();
                success = (rowsAffected > 0);

                System.out.println("Update PinSlot - PinID: " + pinID + ", NewPercent: " + pinPercent + "%, PinHealth: " + pinHealth + "%, Status: "
                        + newStatus + ", Rows affected: " + rowsAffected);
            }
        } catch (Exception e) {
            System.out.println("Error updating pin slot: " + e.getMessage());
            throw new SQLException("Error updating pin slot: " + e.getMessage());
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

    public boolean updatePinSlotStatus(int pinID, int status) throws SQLException {
        boolean check = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        String sql = "UPDATE dbo.pinSlot SET status = ? WHERE pinID = ?";
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, status);
                ptm.setInt(2, pinID);

                check = ptm.executeUpdate() > 0 ? true : false;
            }
        } catch (Exception e) {
            System.out.println("Error updating pin slot status: " + e.getMessage());
            throw new SQLException("Error updating pin slot status: " + e.getMessage());
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

    public boolean reservePinSlot(int pinID, Integer userID) throws SQLException {
        boolean check = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        String reserve = "UPDATE dbo.pinSlot SET status = 2, userID = ? WHERE pinID = ?";
        String checkStatus = "SELECT status, pinStatus FROM dbo.pinSlot WHERE pinID = ?";
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                // Kiểm tra trạng thái hiện tại
                ptm = conn.prepareStatement(checkStatus);
                ptm.setInt(1, pinID);
                ResultSet rs = ptm.executeQuery();
                if (rs.next()) {
                    int status = rs.getInt("status");
                    int pinStatus = rs.getInt("pinStatus");
                    // Chỉ cho phép đặt chỗ nếu trạng thái hiện tại là 0 (available) và pinStatus là 1 (fully charged)
                    if (status == 0 && pinStatus == 1) {
                        ptm.close(); // Đóng PreparedStatement cũ trước khi tái sử dụng
                        ptm = conn.prepareStatement(reserve);
                        ptm.setInt(1, userID);
                        ptm.setInt(2, pinID);
                        check = ptm.executeUpdate() > 0 ? true : false;
                    } else {
                        System.out.println("Pin slot cannot be reserved. Current status: " + status + ", Pin status: " + pinStatus);
                    }
                } else {
                    System.out.println("Pin slot with ID " + pinID + " does not exist.");
                }
                rs.close();
            }
        } catch (Exception e) {
            System.out.println("Error reserving pin slot: " + e.getMessage());
            throw new SQLException("Error reserving pin slot: " + e.getMessage());
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

    public boolean unreservePinSlot(int pinID) throws SQLException {
        boolean check = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        String unreserve = "UPDATE dbo.pinSlot SET status = 0, userID = NULL WHERE pinID = ?";
        String checkStatus = "SELECT status, pinStatus FROM dbo.pinSlot WHERE pinID = ?";
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                // Kiểm tra trạng thái hiện tại
                ptm = conn.prepareStatement(checkStatus);
                ptm.setInt(1, pinID);
                ResultSet rs = ptm.executeQuery();
                if (rs.next()) {
                    int status = rs.getInt("status");
                    // Chỉ cho phép unreserve nếu trạng thái hiện tại là 2 (reserved)
                    if (status == 2) {
                        ptm.close(); // Đóng PreparedStatement cũ trước khi tái sử dụng
                        ptm = conn.prepareStatement(unreserve);
                        ptm.setInt(1, pinID);
                        check = ptm.executeUpdate() > 0;
                    } else {
                        System.out.println("Pin slot cannot be unreserved. Current status: " + status + " (must be 2 to unreserve)");
                    }
                } else {
                    System.out.println("Pin slot with ID " + pinID + " does not exist.");
                }
                rs.close();
            }
        } catch (Exception e) {
            System.out.println("Error unreserving pin slot: " + e.getMessage());
            throw new SQLException("Error unreserving pin slot: " + e.getMessage());
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

}