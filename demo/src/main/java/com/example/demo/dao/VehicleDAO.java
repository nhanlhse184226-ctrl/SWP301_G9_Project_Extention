package com.example.demo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.VehicleDTO;

/**
 * DAO quản lý thông tin xe điện (Vehicle Management)
 * Xử lý việc đăng ký, cập nhật và theo dõi thông tin xe của user
 * 
 * Database table: Vehicle
 * - vehicleID: ID tự động tăng (PK)
 * - userID: ID chủ sở hữu xe (FK)
 * - licensePlate: Biển số xe (unique)
 * - vehicleType: Loại xe (motorcycle, car, truck, etc.)
 * - pinPercent: Phần trăm pin hiện tại (0-100%)
 * - pinHealth: Sức khỏe pin (0-100%)
 * 
 * Business Rules:
 * - Mỗi user có thể sở hữu nhiều xe
 * - Biển số xe phải là duy nhất trong hệ thống
 * - pinPercent và pinHealth nằm trong khoảng 0-100
 * - User chỉ được xem/quản lý xe của mình
 * - Xe với pinHealth < 20% nên được cảnh báo
 */
public class VehicleDAO {

    /**
     * Lấy danh sách tất cả xe điện của một user
     * Sử dụng để hiển thị xe trong profile hoặc khi chọn xe để sạc
     * @param userID ID của user cần lấy danh sách xe
     * @return List chứa tất cả VehicleDTO của user
     * @throws SQLException nếu có lỗi database
     */
    // Method để lấy danh sách xe theo userID
    public List<VehicleDTO> getVehiclesByUserID(int userID) throws SQLException {
        // Khởi tạo list chứa kết quả
        List<VehicleDTO> vehicles = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        // SQL lấy tất cả thông tin xe của user cụ thể
        String sql = "SELECT vehicleID, userID, licensePlate, vehicleType, pinPercent, pinHealth FROM Vehicle WHERE userID = ?";

        try {
            // Mở kết nối database
            conn = DBUtils.getConnection();
            
            // Kiểm tra kết nối thành công
            if (conn != null) {
                // Chuẩn bị prepared statement
                ptm = conn.prepareStatement(sql);
                
                // Gán userID vào parameter
                ptm.setInt(1, userID);
                
                // Thực thi query
                rs = ptm.executeQuery();

                // Duyệt qua tất cả xe của user
                while (rs.next()) {
                    // Lấy từng thông tin xe từ ResultSet
                    int vehicleID = rs.getInt("vehicleID");           // ID xe (PK)
                    int userIDFromDB = rs.getInt("userID");           // Xác nhận userID từ DB
                    String licensePlate = rs.getString("licensePlate"); // Biển số xe
                    String vehicleType = rs.getString("vehicleType"); // Loại xe
                    int pinPercent = rs.getInt("pinPercent");         // % pin hiện tại (0-100)
                    int pinHealth = rs.getInt("pinHealth");           // Sức khỏe pin (0-100)

                    // Tạo VehicleDTO và thêm vào list
                    vehicles.add(new VehicleDTO(vehicleID, userIDFromDB, licensePlate, vehicleType, pinPercent, pinHealth));
                }
            }
        } catch (ClassNotFoundException e) {
            // Lỗi không tìm thấy database driver
            System.out.println("ClassNotFoundException in getVehiclesByUserID: " + e.getMessage());
            throw new SQLException("Database driver not found");
        } catch (SQLException e) {
            // Lỗi SQL (connection, query syntax, etc.)
            System.out.println("SQLException in getVehiclesByUserID: " + e.getMessage());
            throw new SQLException("Error getting vehicles by user ID: " + e.getMessage());
        } finally {
            // Đóng tất cả resources
            if (rs != null) {
                rs.close();                 // Đóng ResultSet
            }
            if (ptm != null) {
                ptm.close();                // Đóng PreparedStatement
            }
            if (conn != null) {
                conn.close();               // Đóng Connection
            }
        }
        
        // Trả về danh sách xe của user
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

    public VehicleDTO getVehiclesByVehicleID(int vehicleID) throws SQLException {
        VehicleDTO vehicle = null;
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        String sql = "SELECT vehicleID, userID, licensePlate, vehicleType, pinPercent, pinHealth FROM Vehicle WHERE vehicleID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, vehicleID);
                rs = ptm.executeQuery();

                if (rs.next()) {
                    int vehicleID1 = rs.getInt("vehicleID");
                    int userID = rs.getInt("userID");
                    String licensePlate = rs.getString("licensePlate");
                    String vehicleType = rs.getString("vehicleType");
                    int pinPercent = rs.getInt("pinPercent");
                    int pinHealth = rs.getInt("pinHealth");

                    vehicle = new VehicleDTO(vehicleID1, userID, licensePlate, vehicleType, pinPercent, pinHealth);}
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
        return vehicle;
    }
}