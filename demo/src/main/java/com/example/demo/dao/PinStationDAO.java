package com.example.demo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.PinStationDTO;

public class PinStationDAO {
    
    // Method để kiểm tra duplicate station name
    public boolean isStationNameExists(String stationName) throws SQLException {
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT COUNT(*) FROM dbo.pinStation WHERE stationName = ?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setString(1, stationName);
                rs = ptm.executeQuery();
                
                if (rs.next()) {
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
    
    // Method để tạo PinStation mới (trigger sẽ tự động tạo 15 pin slots)
    public boolean createPinStation(String stationName, String location, String status) throws SQLException {
        // Enhanced validation
        if (stationName == null || stationName.trim().isEmpty()) {
            throw new SQLException("Station name cannot be null or empty");
        }
        if (location == null || location.trim().isEmpty()) {
            throw new SQLException("Location cannot be null or empty");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new SQLException("Status cannot be null or empty");
        }
        
        // Kiểm tra duplicate station name
        if (isStationNameExists(stationName.trim())) {
            throw new SQLException("Station name '" + stationName + "' already exists");
        }
        
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        
        String sql = "INSERT INTO dbo.pinStation (stationName, location, status, createAt) VALUES (?, ?, ?, GETDATE())";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setString(1, stationName.trim());
                ptm.setString(2, location.trim());
                ptm.setString(3, status.trim());
                
                int rowsAffected = ptm.executeUpdate();
                success = (rowsAffected > 0);
                
                System.out.println("Create PinStation - Name: " + stationName + ", Location: " + location + ", Status: " + status + ", Rows affected: " + rowsAffected);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error in createPinStation: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error in createPinStation: " + e.getMessage());
            throw new SQLException("Failed to create pin station: " + e.getMessage());
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
    
    // Method để lấy danh sách tất cả PinStations
    public List<PinStationDTO> getListPinStation() throws SQLException {
        List<PinStationDTO> listPinStation = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT stationID, stationName, location, status, createAt FROM dbo.pinStation ORDER BY createAt DESC";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                rs = ptm.executeQuery();
                
                while (rs.next()) {
                    PinStationDTO station = new PinStationDTO(
                        rs.getInt("stationID"),
                        rs.getString("stationName"),
                        rs.getString("location"),
                        rs.getString("status"),
                        rs.getTimestamp("createAt")
                    );
                    listPinStation.add(station);
                }
                
                System.out.println("Retrieved " + listPinStation.size() + " pin stations from database");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error in getListPinStation: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error in getListPinStation: " + e.getMessage());
            throw new SQLException("Failed to get pin station list: " + e.getMessage());
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
        
        return listPinStation;
    }
    
    // Method để lấy PinStation theo ID
    public PinStationDTO getPinStationById(int stationID) throws SQLException {
        PinStationDTO station = null;
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT stationID, stationName, location, status, createAt FROM dbo.pinStation WHERE stationID = ?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, stationID);
                rs = ptm.executeQuery();
                
                if (rs.next()) {
                    station = new PinStationDTO(
                        rs.getInt("stationID"),
                        rs.getString("stationName"),
                        rs.getString("location"),
                        rs.getString("status"),
                        rs.getTimestamp("createAt")
                    );
                }
                
                System.out.println("Retrieved pin station with ID: " + stationID);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error in getPinStationById: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error in getPinStationById: " + e.getMessage());
            throw new SQLException("Failed to get pin station: " + e.getMessage());
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
        
        return station;
    }
    
    // Method để kiểm tra duplicate station name khi update (loại trừ chính nó)
    public boolean isStationNameExistsForUpdate(String stationName, int excludeStationID) throws SQLException {
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT COUNT(*) FROM dbo.pinStation WHERE stationName = ? AND stationID != ?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setString(1, stationName);
                ptm.setInt(2, excludeStationID);
                rs = ptm.executeQuery();
                
                if (rs.next()) {
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
    
    // Method để kiểm tra station có tồn tại không
    public boolean isStationExists(int stationID) throws SQLException {
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT COUNT(*) FROM dbo.pinStation WHERE stationID = ?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, stationID);
                rs = ptm.executeQuery();
                
                if (rs.next()) {
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
    
    // Method để kiểm tra status hợp lệ
    public boolean isValidStatus(String status) {
        return status != null && 
               (status.equals("active") || status.equals("inactive") || status.equals("maintenance"));
    }
    
    // Method để update PinStation với validation đầy đủ
    public boolean updatePinStation(int stationID, String newStationName, String newLocation, String newStatus) throws SQLException {
        // Enhanced validation
        if (stationID <= 0) {
            throw new SQLException("Invalid station ID");
        }
        
        // Kiểm tra station có tồn tại không
        if (!isStationExists(stationID)) {
            throw new SQLException("Station with ID " + stationID + " does not exist");
        }
        
        // Lấy thông tin hiện tại để so sánh
        PinStationDTO currentStation = getPinStationById(stationID);
        if (currentStation == null) {
            throw new SQLException("Cannot retrieve current station information");
        }
        
        // Validate inputs
        if (newStationName == null || newStationName.trim().isEmpty()) {
            throw new SQLException("Station name cannot be null or empty");
        }
        if (newLocation == null || newLocation.trim().isEmpty()) {
            throw new SQLException("Location cannot be null or empty");
        }
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new SQLException("Status cannot be null or empty");
        }
        if (!isValidStatus(newStatus.trim())) {
            throw new SQLException("Invalid status. Valid values: active, inactive, maintenance");
        }
        
        // Kiểm tra có thay đổi gì không
        boolean hasNameChanged = !currentStation.getStationName().equals(newStationName.trim());
        boolean hasLocationChanged = !currentStation.getLocation().equals(newLocation.trim());
        boolean hasStatusChanged = !currentStation.getStatus().equals(newStatus.trim());
        
        if (!hasNameChanged && !hasLocationChanged && !hasStatusChanged) {
            throw new SQLException("At least one field (name, location, or status) must be changed");
        }
        
        // Kiểm tra duplicate name nếu name thay đổi
        if (hasNameChanged && isStationNameExistsForUpdate(newStationName.trim(), stationID)) {
            throw new SQLException("Station name '" + newStationName + "' already exists");
        }
        
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        
        String sql = "UPDATE dbo.pinStation SET stationName = ?, location = ?, status = ? WHERE stationID = ?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setString(1, newStationName.trim());
                ptm.setString(2, newLocation.trim());
                ptm.setString(3, newStatus.trim());
                ptm.setInt(4, stationID);
                
                int rowsAffected = ptm.executeUpdate();
                success = (rowsAffected > 0);
                
                System.out.println("Update PinStation - ID: " + stationID + 
                                 ", Name: " + newStationName + 
                                 ", Location: " + newLocation + 
                                 ", Status: " + newStatus + 
                                 ", Rows affected: " + rowsAffected);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error in updatePinStation: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error in updatePinStation: " + e.getMessage());
            throw new SQLException("Failed to update pin station: " + e.getMessage());
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

    // Method để xóa PinStation (copy từ UserDAO)
    public boolean delete(PinStationDTO pinStation) throws SQLException {
        boolean check_pinStation = false;
        boolean check_pinSlot = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        
        String DELETE_PIN_STATION = "DELETE FROM dbo.pinStation WHERE stationID=?";
        String DELETE_PIN_SLOT = "DELETE FROM dbo.pinSlot WHERE stationID=?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(DELETE_PIN_SLOT);
                ptm.setInt(1, pinStation.getStationID());
                check_pinSlot = ptm.executeUpdate() > 0 ? true : false;
                if(check_pinSlot) {
                    ptm = conn.prepareStatement(DELETE_PIN_STATION);
                    ptm.setInt(1, pinStation.getStationID());
                    check_pinStation = ptm.executeUpdate() > 0 ? true : false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error deleting pin station: " + e.getMessage());
        } finally {
            if (ptm != null) {
                ptm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return check_pinStation;
    }
}