package com.example.demo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.PinStationDTO;

/**
 * DAO quản lý trạm sạc xe điện (Charging Station Management)
 * Xử lý việc tạo, cập nhật và lấy thông tin các trạm sạc trong hệ thống
 * 
 * Database table: pinStation
 * - stationID: ID tự động tăng (PK)
 * - stationName: Tên trạm sạc (unique)
 * - location: Địa chỉ trạm sạc
 * - status: Trạng thái (0=maintenance, 1=active, 2=inactive)
 * - x, y: Tọa độ GPS của trạm
 * - userID: ID admin quản lý trạm (FK, nullable)
 * 
 * Related table: pinSlot (1 station có nhiều pin slots)
 * - Khi tạo station mới, trigger tự động tạo 15 pin slots
 * 
 * Business Rules:
 * - Tên trạm phải là duy nhất
 * - Tọa độ GPS phải hợp lệ (-90 ≤ y ≤ 90, -180 ≤ x ≤ 180)
 * - Admin có thể CRUD tất cả stations
 * - User chỉ được xem stations có status=1 (active)
 */
public class PinStationDAO {

    /**
     * Kiểm tra tên trạm sạc đã tồn tại chưa
     * Đảm bảo tên trạm là duy nhất trong hệ thống
     * @param stationName Tên trạm cần kiểm tra
     * @return true nếu tên đã tồn tại, false nếu chưa
     * @throws SQLException nếu có lỗi database
     */
    // Method để kiểm tra duplicate station name
    public boolean isStationNameExists(String stationName) throws SQLException {
        // Khởi tạo các đối tượng database connection
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        // SQL đếm số lượng trạm có tên trùng khớp
        String sql = "SELECT COUNT(*) FROM dbo.pinStation WHERE stationName = ?";

        try {
            // Mở kết nối database
            conn = DBUtils.getConnection();
            
            // Kiểm tra kết nối thành công
            if (conn != null) {
                // Chuẩn bị prepared statement
                ptm = conn.prepareStatement(sql);
                
                // Gán stationName vào parameter
                ptm.setString(1, stationName);
                
                // Thực thi query
                rs = ptm.executeQuery();

                // Lấy kết quả COUNT(*)
                if (rs.next()) {
                    // Nếu COUNT > 0 nghĩa là tên đã tồn tại
                    return rs.getInt(1) > 0;
                }
            }
        } catch (ClassNotFoundException e) {
            // Lỗi không tìm thấy database driver
            throw new SQLException("Database driver not found: " + e.getMessage());
        } finally {
            // Đóng tất cả resources
            if (rs != null)
                rs.close();                     // Đóng ResultSet
            if (ptm != null)
                ptm.close();                    // Đóng PreparedStatement
            if (conn != null)
                conn.close();                   // Đóng Connection
        }

        // Trả về false nếu không tìm thấy (tên chưa tồn tại)
        return false;
    }

    // Method để tạo PinStation mới (trigger sẽ tự động tạo 15 pin slots)
    public boolean createPinStation(String stationName, String location, int status, float x, float y, Integer userID) throws SQLException {
        // Enhanced validation
        if (stationName == null || stationName.trim().isEmpty()) {
            throw new SQLException("Station name cannot be null or empty");
        }
        if (location == null || location.trim().isEmpty()) {
            throw new SQLException("Location cannot be null or empty");
        }
        if (status < 0) {
            throw new SQLException("Status must be a non-negative integer");
        }

        // Kiểm tra duplicate station name
        if (isStationNameExists(stationName.trim())) {
            throw new SQLException("Station name '" + stationName + "' already exists");
        }

        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;

        String sql = "INSERT INTO dbo.pinStation (stationName, location, status, x, y, userID, createAt) VALUES (?, ?, ?, ?, ?, ?, GETDATE())";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setString(1, stationName.trim());
                ptm.setString(2, location.trim());
                ptm.setInt(3, status);
                ptm.setFloat(4, x);
                ptm.setFloat(5, y);
                if (userID != null) {
                    ptm.setInt(6, userID);
                } else {
                    ptm.setNull(6, java.sql.Types.INTEGER);
                }

                int rowsAffected = ptm.executeUpdate();
                success = (rowsAffected > 0);

                System.out.println("Create PinStation - Name: " + stationName + ", Location: " + location + ", Status: "
                        + status + ", Rows affected: " + rowsAffected);
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

        String sql = "SELECT stationID, stationName, location, status, x, y, userID, createAt FROM dbo.pinStation ORDER BY createAt DESC";

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
                            rs.getInt("status"),
                            rs.getTimestamp("createAt"),
                            rs.getFloat("x"),
                            rs.getFloat("y"),
                            rs.getObject("userID", Integer.class));  // Handle nullable Integer
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

        String sql = "SELECT stationID, stationName, location, status, x, y, userID, createAt FROM dbo.pinStation WHERE stationID = ?";

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
                            rs.getInt("status"),
                            rs.getTimestamp("createAt"),
                            rs.getFloat("x"),
                            rs.getFloat("y"),
                            rs.getObject("userID", Integer.class));  // Handle nullable Integer
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

    // Method để lấy danh sách PinStation theo userID
    public List<PinStationDTO> getStationsByUserID(int userID) throws SQLException {
        List<PinStationDTO> listPinStation = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        String sql = "SELECT stationID, stationName, location, status, x, y, userID, createAt " +
                    "FROM dbo.pinStation WHERE userID = ? ORDER BY createAt DESC";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, userID);
                rs = ptm.executeQuery();

                while (rs.next()) {
                    PinStationDTO station = new PinStationDTO(
                            rs.getInt("stationID"),
                            rs.getString("stationName"),
                            rs.getString("location"),
                            rs.getInt("status"),
                            rs.getTimestamp("createAt"),
                            rs.getFloat("x"),
                            rs.getFloat("y"),
                            rs.getObject("userID", Integer.class));  // Handle nullable Integer
                    
                    listPinStation.add(station);
                }

                System.out.println("getStationsByUserID: Retrieved " + listPinStation.size() + " stations for user " + userID);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("getStationsByUserID error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("getStationsByUserID error: " + e.getMessage());
            throw new SQLException("Error getting stations for user: " + e.getMessage());
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
            if (rs != null)
                rs.close();
            if (ptm != null)
                ptm.close();
            if (conn != null)
                conn.close();
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
            if (rs != null)
                rs.close();
            if (ptm != null)
                ptm.close();
            if (conn != null)
                conn.close();
        }

        return false;
    }

    // Method để kiểm tra status hợp lệ
    public boolean isValidStatus(int status) {
        return status >= 0 && status <= 2; // 0: inactive, 1: active, 2: maintenance
    }

    // Method để update PinStation với validation đầy đủ
    public boolean updatePinStation(int stationID, String newStationName, String newLocation, float newX,
            float newY) throws SQLException {
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
        // Validate coordinates (optional - tùy theo yêu cầu business)
        if (Float.isNaN(newX) || Float.isInfinite(newX)) {
            throw new SQLException("Invalid X coordinate");
        }
        if (Float.isNaN(newY) || Float.isInfinite(newY)) {
            throw new SQLException("Invalid Y coordinate");
        }

        // Kiểm tra có thay đổi gì không
        boolean hasNameChanged = !currentStation.getStationName().equals(newStationName.trim());
        boolean hasLocationChanged = !currentStation.getLocation().equals(newLocation.trim());
        boolean hasXChanged = (currentStation.getX() != newX);
        boolean hasYChanged = (currentStation.getY() != newY);

        if (!hasNameChanged && !hasLocationChanged && !hasXChanged && !hasYChanged) {
            throw new SQLException("At least one field (name, location, or coordinates) must be changed");
        }

        // Kiểm tra duplicate name nếu name thay đổi
        if (hasNameChanged && isStationNameExistsForUpdate(newStationName.trim(), stationID)) {
            throw new SQLException("Station name '" + newStationName + "' already exists");
        }

        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;

        String sql = "UPDATE dbo.pinStation SET stationName = ?, location = ?, x = ?, y = ? WHERE stationID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setString(1, newStationName.trim());
                ptm.setString(2, newLocation.trim());
                ptm.setFloat(3, newX);
                ptm.setFloat(4, newY);
                ptm.setInt(5, stationID);

                int rowsAffected = ptm.executeUpdate();
                success = (rowsAffected > 0);

                System.out.println("Update PinStation - ID: " + stationID +
                        ", Name: " + newStationName +
                        ", Location: " + newLocation +
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

    // Method để đảo ngược status của PinStation và PinSlot
    public boolean updateStatus(int stationID) throws SQLException {
        boolean check_pinStation = false;
        Connection conn = null;
        PreparedStatement ptm = null;

        String UPDATE_PIN_STATION = "UPDATE dbo.pinStation SET status = CASE WHEN status = 0 THEN 1 ELSE 0 END WHERE stationID=?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(UPDATE_PIN_STATION);
                ptm.setInt(1, stationID);
                check_pinStation = ptm.executeUpdate() > 0 ? true : false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error updating status: " + e.getMessage());
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

    // Method để kiểm tra user role
    private int getUserRole(int userID) throws SQLException {
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        int roleID = -1;

        String sql = "SELECT roleID FROM users WHERE userID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, userID);
                rs = ptm.executeQuery();

                if (rs.next()) {
                    roleID = rs.getInt("roleID");
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }

        return roleID;
    }

    // Method để kiểm tra staff đã được assign vào trạm nào chưa
    public Integer getStaffAssignedStation(int userID) throws SQLException {
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        String sql = "SELECT stationID FROM dbo.pinStation WHERE userID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, userID);
                rs = ptm.executeQuery();

                if (rs.next()) {
                    return rs.getInt("stationID");
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + e.getMessage());
        } finally {
            if (rs != null)
                rs.close();
            if (ptm != null)
                ptm.close();
            if (conn != null)
                conn.close();
        }

        return null; // Staff chưa được assign vào trạm nào
    }

    // Method để assign staff vào station
    public boolean assignStaffToStation(int stationID, Integer userID) throws SQLException {
        // Validate station exists
        if (!isStationExists(stationID)) {
            throw new SQLException("Station with ID " + stationID + " does not exist");
        }

        // Validate user exists and has correct role if userID is provided
        if (userID != null) {
            
            // Check if user has staff role (roleID = 2)
            int userRole = getUserRole(userID);
            if (userRole != 2) {
                throw new SQLException("User with ID " + userID + " is not a staff member (roleID must be 2)");
            }

            // Check if staff is already assigned to another station
            Integer currentStationID = getStaffAssignedStation(userID);
            if (currentStationID != null && currentStationID != stationID) {
                throw new SQLException("Staff with ID " + userID + " is already assigned to station " + currentStationID + ". Please unassign from the current station first.");
            }
        }

        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;

        String sql = "UPDATE dbo.pinStation SET userID = ? WHERE stationID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                
                if (userID != null) {
                    ptm.setInt(1, userID);
                } else {
                    ptm.setNull(1, java.sql.Types.INTEGER);
                }
                ptm.setInt(2, stationID);

                int rowsAffected = ptm.executeUpdate();
                success = (rowsAffected > 0);

                if (success) {
                    String action = (userID != null) ? "assigned to user " + userID : "unassigned";
                    System.out.println("Station " + stationID + " successfully " + action);
                } else {
                    System.out.println("Failed to assign staff to station " + stationID);
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error in assignStaffToStation: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error in assignStaffToStation: " + e.getMessage());
            throw new SQLException("Failed to assign staff to station: " + e.getMessage());
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