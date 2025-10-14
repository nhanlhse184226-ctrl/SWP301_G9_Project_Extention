package com.example.demo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.VehicleDTO;

public class VehicleDAO {

    // Method để lấy danh sách xe theo userID
    public List<VehicleDTO> getVehiclesByUserID(int userID) throws SQLException {
        List<VehicleDTO> vehicles = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        String sql = "SELECT vehicleID, userID, licensePlate, vehicleType, pinPercent, pinHealth FROM Vehicle WHERE userID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, userID);
                rs = ptm.executeQuery();

                while (rs.next()) {
                    int vehicleID = rs.getInt("vehicleID");
                    int userIDFromDB = rs.getInt("userID");
                    String licensePlate = rs.getString("licensePlate");
                    String vehicleType = rs.getString("vehicleType");
                    int pinPercent = rs.getInt("pinPercent");
                    int pinHealth = rs.getInt("pinHealth");

                    vehicles.add(new VehicleDTO(vehicleID, userIDFromDB, licensePlate, vehicleType, pinPercent, pinHealth));
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException in getVehiclesByUserID: " + e.getMessage());
            throw new SQLException("Database driver not found");
        } catch (SQLException e) {
            System.out.println("SQLException in getVehiclesByUserID: " + e.getMessage());
            throw new SQLException("Error getting vehicles by user ID: " + e.getMessage());
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
        return vehicles;
    }

    // Method để tạo xe mới
    public boolean createVehicle(int userID, String licensePlate, String vehicleType, int pinPercent, int pinHealth) throws SQLException {
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;

        String sql = "INSERT INTO Vehicle (userID, licensePlate, vehicleType, pinPercent, pinHealth) VALUES (?, ?, ?, ?, ?)";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, userID);
                ptm.setString(2, licensePlate);
                ptm.setString(3, vehicleType);
                ptm.setInt(4, pinPercent);
                ptm.setInt(5, pinHealth);

                int rowsAffected = ptm.executeUpdate();
                success = (rowsAffected > 0);

                System.out.println("Created Vehicle - UserID: " + userID + ", LicensePlate: " + licensePlate + ", VehicleType: " + vehicleType + ", PinPercent: " + pinPercent + "%, PinHealth: " + pinHealth + "%, Rows affected: " + rowsAffected);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException in createVehicle: " + e.getMessage());
            throw new SQLException("Database driver not found");
        } catch (SQLException e) {
            System.out.println("SQLException in createVehicle: " + e.getMessage());
            throw new SQLException("Error creating vehicle: " + e.getMessage());
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

    // Method để lấy tất cả xe
    public List<VehicleDTO> getAllVehicles() throws SQLException {
        List<VehicleDTO> vehicles = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        String sql = "SELECT vehicleID, userID, licensePlate, vehicleType, pinPercent, pinHealth FROM Vehicle";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                rs = ptm.executeQuery();

                while (rs.next()) {
                    int vehicleID = rs.getInt("vehicleID");
                    int userID = rs.getInt("userID");
                    String licensePlate = rs.getString("licensePlate");
                    String vehicleType = rs.getString("vehicleType");
                    int pinPercent = rs.getInt("pinPercent");
                    int pinHealth = rs.getInt("pinHealth");

                    vehicles.add(new VehicleDTO(vehicleID, userID, licensePlate, vehicleType, pinPercent, pinHealth));
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException in getAllVehicles: " + e.getMessage());
            throw new SQLException("Database driver not found");
        } catch (SQLException e) {
            System.out.println("SQLException in getAllVehicles: " + e.getMessage());
            throw new SQLException("Error getting all vehicles: " + e.getMessage());
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
        return vehicles;
    }
}