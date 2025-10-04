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
    
    // Method để tạo PinStation mới (trigger sẽ tự động tạo 15 pin slots)
    public boolean createPinStation(String stationName, String location, String status) throws SQLException {
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        
        String sql = "INSERT INTO dbo.pinStation (stationName, location, status, createAt) VALUES (?, ?, ?, GETDATE())";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setString(1, stationName);
                ptm.setString(2, location);
                ptm.setString(3, status);
                
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