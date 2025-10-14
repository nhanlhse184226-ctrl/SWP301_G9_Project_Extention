package com.example.demo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.ServicePackDTO;

public class ServicePackDAO {

    // Method để check admin role (roleID = 3)
    private boolean isAdminUser(int userID) throws SQLException {
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        String sql = "SELECT roleID FROM users WHERE userID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, userID);
                rs = ptm.executeQuery();

                if (rs.next()) {
                    int roleID = rs.getInt("roleID");
                    return roleID == 3; // Admin role
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }

        return false; // User không tồn tại hoặc không phải admin
    }


    // Method để check duplicate pack name
    public boolean isPackNameExists(String packName) throws SQLException {
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        String sql = "SELECT packName FROM ServicePack WHERE packName = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setString(1, packName);
                rs = ptm.executeQuery();

                return rs.next(); // Trả về true nếu có record, false nếu không
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

    // Method để tạo ServicePack mới (chỉ admin)
    public boolean createServicePack(ServicePackDTO servicePack, int adminUserID) throws SQLException {
        // Validate admin permission
        if (!isAdminUser(adminUserID)) {
            throw new SQLException("Access denied. Only admin users (roleID=3) can create service packs");
        }

        // Validate input
        if (servicePack.getPackName() == null || servicePack.getPackName().trim().isEmpty()) {
            throw new SQLException("Pack name cannot be null or empty");
        }

        if (servicePack.getStatus() < 0 || servicePack.getStatus() > 1) {
            throw new SQLException("Status must be 0 (inactive) or 1 (active)");
        }

        if (servicePack.getPrice() < 0 || servicePack.getTotal() < 0) {
            throw new SQLException("Price and total must be non-negative");
        }

        // Check duplicate pack name
        if (isPackNameExists(servicePack.getPackName().trim())) {
            throw new SQLException("Service pack name '" + servicePack.getPackName() + "' already exists");
        }

        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;

        String sql = "INSERT INTO ServicePack (packName, status, description, total, price, createDate) VALUES (?, ?, ?, ?, ?, GETDATE())";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setString(1, servicePack.getPackName().trim());
                ptm.setInt(2, servicePack.getStatus());
                ptm.setString(3, servicePack.getDescription());
                ptm.setInt(4, servicePack.getTotal());
                ptm.setInt(5, servicePack.getPrice());

                int rowsAffected = ptm.executeUpdate();
                success = (rowsAffected > 0);

                System.out.println("Create ServicePack - Name: " + servicePack.getPackName() + 
                                 ", Status: " + servicePack.getStatus() + 
                                 ", Price: " + servicePack.getPrice() + 
                                 ", Rows affected: " + rowsAffected);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error in createServicePack: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error in createServicePack: " + e.getMessage());
            throw new SQLException("Failed to create service pack: " + e.getMessage());
        } finally {
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }

        return success;
    }

    // Method để update ServicePack (chỉ admin) - Update toàn bộ thông tin
    public boolean updateServicePack(int packID, ServicePackDTO servicePack, int adminUserID) throws SQLException {
        // Validate admin permission
        if (!isAdminUser(adminUserID)) {
            throw new SQLException("Access denied. Only admin users (roleID=3) can update service packs");
        }

        // Validate input
        if (servicePack.getPackName() == null || servicePack.getPackName().trim().isEmpty()) {
            throw new SQLException("Pack name cannot be null or empty");
        }

        if (servicePack.getStatus() < 0 || servicePack.getStatus() > 1) {
            throw new SQLException("Status must be 0 (inactive) or 1 (active)");
        }

        if (servicePack.getPrice() < 0 || servicePack.getTotal() < 0) {
            throw new SQLException("Price and total must be non-negative");
        }

        Connection conn = null;
        PreparedStatement ptm = null;

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {

                // Perform update
                String updateSql = "UPDATE ServicePack SET packName = ?, description = ?, total = ?, price = ? WHERE packID = ?";
                ptm = conn.prepareStatement(updateSql);
                ptm.setString(1, servicePack.getPackName().trim());
                ptm.setString(2, servicePack.getDescription());
                ptm.setInt(3, servicePack.getTotal());
                ptm.setInt(4, servicePack.getPrice());
                ptm.setInt(5, packID);

                int rowsAffected = ptm.executeUpdate();
                boolean success = (rowsAffected > 0);

                System.out.println("Update ServicePack - ID: " + packID + 
                                 ", Name: " + servicePack.getPackName() + 
                                 ", Price: " + servicePack.getPrice() + 
                                 ", Rows affected: " + rowsAffected);

                return success;
            }
        } catch (Exception e) {
            System.out.println("Unexpected error in updateServicePack: " + e.getMessage());
            throw new SQLException("Failed to update service pack: " + e.getMessage());
        } finally {
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }

        return false;
    }

    // Method để update chỉ status của ServicePack (chỉ admin)
    public boolean updateServicePackStatus(int packID, int status, int adminUserID) throws SQLException {
        // Validate admin permission
        if (!isAdminUser(adminUserID)) {
            throw new SQLException("Access denied. Only admin users (roleID=3) can update service pack status");
        }

        // Validate status
        if (status < 0 || status > 1) {
            throw new SQLException("Status must be 0 (inactive) or 1 (active)");
        }

        Connection conn = null;
        PreparedStatement ptm = null;

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                String updateSql = "UPDATE ServicePack SET status = ? WHERE packID = ?";
                ptm = conn.prepareStatement(updateSql);
                ptm.setInt(1, status);
                ptm.setInt(2, packID);

                int rowsAffected = ptm.executeUpdate();
                boolean success = (rowsAffected > 0);

                String statusText = (status == 0) ? "inactive" : "active";
                System.out.println("Update ServicePack Status - ID: " + packID + 
                                 ", New Status: " + status + " (" + statusText + ")" +
                                 ", Rows affected: " + rowsAffected);

                return success;
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error in updateServicePackStatus: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error in updateServicePackStatus: " + e.getMessage());
            throw new SQLException("Failed to update service pack status: " + e.getMessage());
        } finally {
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }

        return false;
    }

    // Method để lấy ServicePack theo ID (để verify sau khi create/update)
    public ServicePackDTO getServicePackById(int packID) throws SQLException {
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        String sql = "SELECT packID, packName, status, description, total, price, createDate FROM ServicePack WHERE packID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, packID);
                rs = ptm.executeQuery();

                if (rs.next()) {
                    return new ServicePackDTO(
                        rs.getInt("packID"),
                        rs.getString("packName"),
                        rs.getInt("status"),
                        rs.getString("description"),
                        rs.getInt("total"),
                        rs.getInt("price"),
                        rs.getTimestamp("createDate")
                    );
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }

        return null;
    }

    // Method để lấy danh sách tất cả ServicePack
    public List<ServicePackDTO> getListServicePack() throws SQLException {
        List<ServicePackDTO> listServicePack = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        String sql = "SELECT packID, packName, status, description, total, price, createDate FROM ServicePack ORDER BY createDate DESC";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                rs = ptm.executeQuery();

                while (rs.next()) {
                    ServicePackDTO servicePack = new ServicePackDTO(
                        rs.getInt("packID"),
                        rs.getString("packName"),
                        rs.getInt("status"),
                        rs.getString("description"),
                        rs.getInt("total"),
                        rs.getInt("price"),
                        rs.getTimestamp("createDate")
                    );
                    listServicePack.add(servicePack);
                }

                System.out.println("Retrieved " + listServicePack.size() + " service packs from database");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }

        return listServicePack;
    }
}