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

    // Method để thực hiện stored procedure UpdatePinPercent
    public boolean updatePinPercent() throws SQLException {
        boolean check = false;
        Connection conn = null;
        PreparedStatement ptm = null;

        String sql = "EXEC dbo.UpdatePinPercent";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                int rowsAffected = ptm.executeUpdate();
                System.out.println("UpdatePinPercent executed - Rows affected: " + rowsAffected);
                check = true;
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException in updatePinPercent: " + e.getMessage());
            throw new SQLException("Database driver not found");
        } catch (SQLException e) {
            System.out.println("SQLException in updatePinPercent: " + e.getMessage());
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

        String sql = "SELECT pinID, pinPercent, pinHealth, pinStatus, status, userID, vehicleID, stationID FROM pinSlot";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                rs = ptm.executeQuery();
                while (rs.next()) {
                    int pinID = rs.getInt("pinID");
                    int pinPercent = rs.getInt("pinPercent");
                    int pinHealth = rs.getInt("pinHealth");
                    int pinStatus = rs.getInt("pinStatus");
                    int status = rs.getInt("status");

                    // Đọc userID, vehicleID và stationID từ database
                    Integer userID = rs.getObject("userID", Integer.class);
                    Integer vehicleID = rs.getObject("vehicleID", Integer.class);
                    int stationID = rs.getInt("stationID");

                    // Sử dụng constructor với đầy đủ fields bao gồm userID, vehicleID
                    listPinSlot.add(new PinSlotDTO(pinID, pinPercent, pinHealth, pinStatus, status, userID, vehicleID, stationID));
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException in getListPinSlot: " + e.getMessage());
            throw new SQLException("Database driver not found");
        } catch (SQLException e) {
            System.out.println("SQLException in getListPinSlot: " + e.getMessage());
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

        String sql = "SELECT pinID, pinPercent, pinHealth, pinStatus, status, userID, vehicleID, stationID FROM dbo.pinSlot WHERE stationID = ?";

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

                    // Đọc userID, vehicleID và stationID từ database
                    Integer userID = rs.getObject("userID", Integer.class);
                    Integer vehicleID = rs.getObject("vehicleID", Integer.class);
                    int stationIDFromDB = rs.getInt("stationID");

                    // Sử dụng constructor với đầy đủ fields bao gồm userID, vehicleID
                    listPinSlot.add(new PinSlotDTO(pinID, pinPercent, pinHealth, pinStatus, status, userID, vehicleID, stationIDFromDB));
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException in getListPinSlotByStation: " + e.getMessage());
            throw new SQLException("Database driver not found");
        } catch (SQLException e) {
            System.out.println("SQLException in getListPinSlotByStation: " + e.getMessage());
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

    // Method để lấy danh sách PinSlot theo vehicleID
    public List<PinSlotDTO> getListPinSlotByVehicle(int vehicleID) throws SQLException {
        List<PinSlotDTO> listPinSlot = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        String sql = "SELECT pinID, pinPercent, pinHealth, pinStatus, status, userID, vehicleID, stationID FROM dbo.pinSlot WHERE vehicleID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, vehicleID);
                rs = ptm.executeQuery();

                while (rs.next()) {
                    int pinID = rs.getInt("pinID");
                    int pinPercent = rs.getInt("pinPercent");
                    int pinHealth = rs.getInt("pinHealth");
                    int pinStatus = rs.getInt("pinStatus");
                    int status = rs.getInt("status");

                    // Đọc userID, vehicleID và stationID từ database
                    Integer userID = rs.getObject("userID", Integer.class);
                    Integer vehID = rs.getObject("vehicleID", Integer.class);
                    int stationID = rs.getInt("stationID");

                    // Sử dụng constructor với đầy đủ fields bao gồm userID, vehicleID
                    listPinSlot.add(new PinSlotDTO(pinID, pinPercent, pinHealth, pinStatus, status, userID, vehID, stationID));
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException in getListPinSlotByVehicle: " + e.getMessage());
            throw new SQLException("Database driver not found");
        } catch (SQLException e) {
            System.out.println("SQLException in getListPinSlotByVehicle: " + e.getMessage());
            throw new SQLException("Error getting pin slot list by vehicle: " + e.getMessage());
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
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException in updatePinSlot: " + e.getMessage());
            throw new SQLException("Database driver not found");
        } catch (SQLException e) {
            System.out.println("SQLException in updatePinSlot: " + e.getMessage());
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

                check = ptm.executeUpdate() > 0;
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException in updatePinSlotStatus: " + e.getMessage());
            throw new SQLException("Database driver not found");
        } catch (SQLException e) {
            System.out.println("SQLException in updatePinSlotStatus: " + e.getMessage());
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

    public boolean reservePinSlot(int pinID, Integer userID, Integer vehicleID) throws SQLException {
        boolean check = false;
        Connection conn = null;
        PreparedStatement ptm = null;

        String reserveSQL = "UPDATE dbo.pinSlot SET status = 2, userID = ?, vehicleID = ? WHERE pinID = ?";
        String checkStatusSQL = "SELECT status, pinStatus FROM dbo.pinSlot WHERE pinID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                // Kiểm tra trạng thái hiện tại
                ptm = conn.prepareStatement(checkStatusSQL);
                ptm.setInt(1, pinID);
                ResultSet rs = ptm.executeQuery();
                if (rs.next()) {
                    int status = rs.getInt("status");
                    int pinStatus = rs.getInt("pinStatus");
                    // Chỉ cho phép đặt chỗ nếu status hiện tại là 1 (available) và pinStatus là 1 (fully charged)
                    if (status == 1 && pinStatus == 1) {
                        ptm.close(); // Đóng PreparedStatement cũ trước khi tái sử dụng
                        ptm = conn.prepareStatement(reserveSQL);
                        ptm.setInt(1, userID);
                        ptm.setInt(2, vehicleID);
                        ptm.setInt(3, pinID);
                        check = ptm.executeUpdate() > 0;
                        System.out.println("Pin slot " + pinID + " reserved for userID: " + userID + ", vehicleID: " + vehicleID);
                    } else {
                        System.out.println("Pin slot cannot be reserved. Current status: " + status + ", Pin status: " + pinStatus);
                    }
                } else {
                    System.out.println("Pin slot with ID " + pinID + " does not exist.");
                }
                rs.close();
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException in reservePinSlot: " + e.getMessage());
            throw new SQLException("Database driver not found");
        } catch (SQLException e) {
            System.out.println("SQLException in reservePinSlot: " + e.getMessage());
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

        String unreserveSQL = "UPDATE dbo.pinSlot SET status = 1, userID = NULL, vehicleID = NULL WHERE pinID = ?";
        String checkStatusSQL = "SELECT status, pinStatus FROM dbo.pinSlot WHERE pinID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                // Kiểm tra trạng thái hiện tại
                ptm = conn.prepareStatement(checkStatusSQL);
                ptm.setInt(1, pinID);
                ResultSet rs = ptm.executeQuery();
                if (rs.next()) {
                    int status = rs.getInt("status");
                    // Chỉ cho phép unreserve nếu trạng thái hiện tại là 2 (reserved)
                    if (status == 2) {
                        ptm.close(); // Đóng PreparedStatement cũ trước khi tái sử dụng
                        ptm = conn.prepareStatement(unreserveSQL);
                        ptm.setInt(1, pinID);
                        check = ptm.executeUpdate() > 0;
                        System.out.println("Pin slot " + pinID + " unreserved (cleared userID and vehicleID)");
                    } else {
                        System.out.println("Pin slot cannot be unreserved. Current status: " + status + " (must be 2 to unreserve)");
                    }
                } else {
                    System.out.println("Pin slot with ID " + pinID + " does not exist.");
                }
                rs.close();
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException in unreservePinSlot: " + e.getMessage());
            throw new SQLException("Database driver not found");
        } catch (SQLException e) {
            System.out.println("SQLException in unreservePinSlot: " + e.getMessage());
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

    // Method để swap pin data giữa Vehicle và PinSlot - theo yêu cầu từ conversation
    public boolean swapVehiclePinSlotData(int vehicleID, int pinSlotID) throws SQLException {
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        // SQL để lấy dữ liệu từ Vehicle và PinSlot
        String getVehicleSQL = "SELECT pinPercent, pinHealth FROM Vehicle WHERE vehicleID = ?";
        String getPinSlotSQL = "SELECT pinPercent, pinHealth FROM pinSlot WHERE pinID = ?";
        
        // SQL để update dữ liệu sau khi swap
        String updateVehicleSQL = "UPDATE Vehicle SET pinPercent = ?, pinHealth = ? WHERE vehicleID = ?";
        String updatePinSlotSQL = "UPDATE pinSlot SET pinPercent = ?, pinHealth = ? WHERE pinID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                // Bắt đầu transaction
                conn.setAutoCommit(false);

                // Lấy dữ liệu Vehicle (SOH1, SOC1)
                ptm = conn.prepareStatement(getVehicleSQL);
                ptm.setInt(1, vehicleID);
                rs = ptm.executeQuery();
                
                int vehiclePinPercent = 0, vehiclePinHealth = 0;
                if (rs.next()) {
                    vehiclePinPercent = rs.getInt("pinPercent");
                    vehiclePinHealth = rs.getInt("pinHealth");
                } else {
                    throw new SQLException("Vehicle ID " + vehicleID + " not found");
                }
                rs.close();
                ptm.close();

                // Lấy dữ liệu PinSlot (SOH2, SOC2)
                ptm = conn.prepareStatement(getPinSlotSQL);
                ptm.setInt(1, pinSlotID);
                rs = ptm.executeQuery();
                
                int pinSlotPinPercent = 0, pinSlotPinHealth = 0;
                if (rs.next()) {
                    pinSlotPinPercent = rs.getInt("pinPercent");
                    pinSlotPinHealth = rs.getInt("pinHealth");
                } else {
                    throw new SQLException("PinSlot ID " + pinSlotID + " not found");
                }
                rs.close();
                ptm.close();

                // Swap: Vehicle lưu SOH2 SOC2, PinSlot lưu SOH1 SOC1
                ptm = conn.prepareStatement(updateVehicleSQL);
                ptm.setInt(1, pinSlotPinPercent); // Vehicle nhận pinPercent từ PinSlot
                ptm.setInt(2, pinSlotPinHealth); // Vehicle nhận pinHealth từ PinSlot
                ptm.setInt(3, vehicleID);
                int vehicleRowsAffected = ptm.executeUpdate();
                ptm.close();

                ptm = conn.prepareStatement(updatePinSlotSQL);
                ptm.setInt(1, vehiclePinPercent); // PinSlot nhận pinPercent từ Vehicle
                ptm.setInt(2, vehiclePinHealth); // PinSlot nhận pinHealth từ Vehicle
                ptm.setInt(3, pinSlotID);
                int pinSlotRowsAffected = ptm.executeUpdate();

                // Commit transaction nếu cả 2 update thành công
                if (vehicleRowsAffected > 0 && pinSlotRowsAffected > 0) {
                    conn.commit();
                    success = true;
                    System.out.println("Pin data swapped successfully between Vehicle ID " + vehicleID + " and PinSlot ID " + pinSlotID);
                    System.out.println("Vehicle: " + vehiclePinPercent + "%, " + vehiclePinHealth + "% -> " + pinSlotPinPercent + "%, " + pinSlotPinHealth + "%");
                    System.out.println("PinSlot: " + pinSlotPinPercent + "%, " + pinSlotPinHealth + "% -> " + vehiclePinPercent + "%, " + vehiclePinHealth + "%");
                } else {
                    conn.rollback();
                    System.out.println("Failed to swap pin data - rolling back transaction");
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in swapVehiclePinSlotData: " + e.getMessage());
            throw new SQLException("Error swapping vehicle pin slot data: " + e.getMessage());
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ptm != null) {
                ptm.close();
            }
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
        return success;
    }

    // Method để swap pin data giữa 2 PinSlot - dựa trên format swapVehiclePinSlotData
    public boolean swapPinSlotData(int pinSlotID1, int pinSlotID2) throws SQLException {
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        // SQL để lấy dữ liệu từ 2 PinSlot
        String getPinSlot1SQL = "SELECT pinPercent, pinHealth FROM pinSlot WHERE pinID = ?";
        String getPinSlot2SQL = "SELECT pinPercent, pinHealth FROM pinSlot WHERE pinID = ?";
        
        // SQL để update dữ liệu sau khi swap
        String updatePinSlot1SQL = "UPDATE pinSlot SET pinPercent = ?, pinHealth = ? WHERE pinID = ?";
        String updatePinSlot2SQL = "UPDATE pinSlot SET pinPercent = ?, pinHealth = ? WHERE pinID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                // Bắt đầu transaction
                conn.setAutoCommit(false);

                // Lấy dữ liệu PinSlot 1 (SOH1, SOC1)
                ptm = conn.prepareStatement(getPinSlot1SQL);
                ptm.setInt(1, pinSlotID1);
                rs = ptm.executeQuery();
                
                int pinSlot1PinPercent = 0, pinSlot1PinHealth = 0;
                if (rs.next()) {
                    pinSlot1PinPercent = rs.getInt("pinPercent");
                    pinSlot1PinHealth = rs.getInt("pinHealth");
                } else {
                    throw new SQLException("PinSlot ID " + pinSlotID1 + " not found");
                }
                rs.close();
                ptm.close();

                // Lấy dữ liệu PinSlot 2 (SOH2, SOC2)
                ptm = conn.prepareStatement(getPinSlot2SQL);
                ptm.setInt(1, pinSlotID2);
                rs = ptm.executeQuery();
                
                int pinSlot2PinPercent = 0, pinSlot2PinHealth = 0;
                if (rs.next()) {
                    pinSlot2PinPercent = rs.getInt("pinPercent");
                    pinSlot2PinHealth = rs.getInt("pinHealth");
                } else {
                    throw new SQLException("PinSlot ID " + pinSlotID2 + " not found");
                }
                rs.close();
                ptm.close();

                // Swap: PinSlot1 lưu SOH2 SOC2, PinSlot2 lưu SOH1 SOC1
                ptm = conn.prepareStatement(updatePinSlot1SQL);
                ptm.setInt(1, pinSlot2PinPercent); // PinSlot1 nhận pinPercent từ PinSlot2
                ptm.setInt(2, pinSlot2PinHealth); // PinSlot1 nhận pinHealth từ PinSlot2
                ptm.setInt(3, pinSlotID1);
                int pinSlot1RowsAffected = ptm.executeUpdate();
                ptm.close();

                ptm = conn.prepareStatement(updatePinSlot2SQL);
                ptm.setInt(1, pinSlot1PinPercent); // PinSlot2 nhận pinPercent từ PinSlot1
                ptm.setInt(2, pinSlot1PinHealth); // PinSlot2 nhận pinHealth từ PinSlot1
                ptm.setInt(3, pinSlotID2);
                int pinSlot2RowsAffected = ptm.executeUpdate();

                // Commit transaction nếu cả 2 update thành công
                if (pinSlot1RowsAffected > 0 && pinSlot2RowsAffected > 0) {
                    conn.commit();
                    success = true;
                    System.out.println("Pin data swapped successfully between PinSlot ID " + pinSlotID1 + " and PinSlot ID " + pinSlotID2);
                    System.out.println("PinSlot1: " + pinSlot1PinPercent + "%, " + pinSlot1PinHealth + "% -> " + pinSlot2PinPercent + "%, " + pinSlot2PinHealth + "%");
                    System.out.println("PinSlot2: " + pinSlot2PinPercent + "%, " + pinSlot2PinHealth + "% -> " + pinSlot1PinPercent + "%, " + pinSlot1PinHealth + "%");
                } else {
                    conn.rollback();
                    System.out.println("Failed to swap pin data between PinSlots - rolling back transaction");
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in swapPinSlotData: " + e.getMessage());
            throw new SQLException("Error swapping pin slot data: " + e.getMessage());
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ptm != null) {
                ptm.close();
            }
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
        return success;
    }

}