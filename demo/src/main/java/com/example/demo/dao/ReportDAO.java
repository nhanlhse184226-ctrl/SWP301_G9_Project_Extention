package com.example.demo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.ReportDTO;

public class ReportDAO {

    /**
     * Create a new report
     * Only users with roleID = 1 can create reports
     */
    public boolean createReport(ReportDTO report) throws SQLException {
        // Validate input
        if (report == null || !report.isValidType() || !report.isValidDescription() || !report.isValidReporter()) {
            throw new SQLException("Invalid report data");
        }

        // Check if reporter has correct role (roleID = 1)
        if (!isUserRole(report.getReporterId(), 1)) {
            throw new SQLException("Only users with roleID = 1 can create reports");
        }

        boolean result = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        
        String sql = "INSERT INTO reports(type, description, reporter_id, handler_id, created_at, status) VALUES(?,?,?,?,?,?)";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, report.getType());
                ptm.setString(2, report.getDescription());
                ptm.setInt(3, report.getReporterId());
                ptm.setNull(4, java.sql.Types.INTEGER); // handler_id is null initially
                ptm.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                ptm.setInt(6, 0); // status = 0 (Pending)
                
                int rowsAffected = ptm.executeUpdate();
                result = rowsAffected > 0;
                
                System.out.println("createReport: " + rowsAffected + " rows affected - Type: " + report.getType() + 
                                 ", Reporter: " + report.getReporterId() + 
                                 ", Description length: " + report.getDescription().length());
            }
        } catch (ClassNotFoundException e) {
            System.err.println("createReport error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("createReport error: " + e.getMessage());
            throw new SQLException("Error creating report: " + e.getMessage());
        } finally {
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return result;
    }

    /**
     * Get all reports - Only admins (roleID = 3) can access
     */
    public List<ReportDTO> getAllReports(int adminId) throws SQLException {
        // Check if user is admin
        if (!isUserRole(adminId, 3)) {
            throw new SQLException("Only admins can view all reports");
        }

        List<ReportDTO> reports = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT * FROM reports ORDER BY created_at DESC";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                rs = ptm.executeQuery();
                
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
                
                System.out.println("getAllReports: Retrieved " + reports.size() + " reports");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("getAllReports error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("getAllReports error: " + e.getMessage());
            throw new SQLException("Error getting reports: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return reports;
    }

    /**
     * Get pending reports - Only admins (roleID = 3) can access
     */
    public List<ReportDTO> getPendingReports(int adminId) throws SQLException {
        // Check if user is admin
        if (!isUserRole(adminId, 3)) {
            throw new SQLException("Only admins can view pending reports");
        }

        List<ReportDTO> reports = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT * FROM reports WHERE status = 0 ORDER BY created_at ASC";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                rs = ptm.executeQuery();
                
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
                
                System.out.println("getPendingReports: Retrieved " + reports.size() + " pending reports");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("getPendingReports error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("getPendingReports error: " + e.getMessage());
            throw new SQLException("Error getting pending reports: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return reports;
    }





    /**
     * Update report status - Only admins (roleID = 3) can update
     */
    public boolean updateReportStatus(int reportId, int newStatus, int adminId) throws SQLException {
        // Check if user is admin
        if (!isUserRole(adminId, 3)) {
            throw new SQLException("Only admins can update report status");
        }

        if (newStatus < 0 || newStatus > 2) {
            throw new SQLException("Invalid status value");
        }

        boolean result = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        
        String sql = "UPDATE reports SET status = ?, handler_id = ? WHERE id = ?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, newStatus);
                
                if (newStatus > 0) {
                    // If moving from Pending to InProgress or Resolved, set admin as handler
                    ptm.setInt(2, adminId);
                } else {
                    ptm.setNull(2, java.sql.Types.INTEGER);
                }
                
                ptm.setInt(3, reportId);
                int rowsAffected = ptm.executeUpdate();
                result = rowsAffected > 0;
                
                System.out.println("updateReportStatus: " + rowsAffected + " rows affected - Report " + reportId + " status updated to " + newStatus + " by admin " + adminId);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("updateReportStatus error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("updateReportStatus error: " + e.getMessage());
            throw new SQLException("Error updating report status: " + e.getMessage());
        } finally {
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return result;
    }

    /**
     * Get reports by reporter (for users to view their own reports)
     */
    public List<ReportDTO> getReportsByReporter(int reporterId) throws SQLException {
        // Verify that the reporterId corresponds to a user (roleID = 1)
        if (!isUserRole(reporterId, 1)) {
            throw new SQLException("Invalid reporter ID");
        }

        List<ReportDTO> reports = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT * FROM reports WHERE reporter_id = ? ORDER BY created_at DESC";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, reporterId);
                rs = ptm.executeQuery();
                
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
                
                System.out.println("getReportsByReporter: Retrieved " + reports.size() + " reports for reporter " + reporterId);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("getReportsByReporter error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("getReportsByReporter error: " + e.getMessage());
            throw new SQLException("Error getting reports by reporter: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return reports;
    }

    /**
     * Get reports by user ID (for admin to view any user's reports)
     */
    public List<ReportDTO> getReportsByUserId(int userId, int adminId) throws SQLException {
        // Verify that adminId is an admin (roleID = 3)
        if (!isUserRole(adminId, 3)) {
            throw new SQLException("Access denied. Only admins can view reports by user ID.");
        }

        // Verify that the userId exists and is a user (roleID = 1)
        if (!isUserRole(userId, 1)) {
            throw new SQLException("Invalid user ID or user not found");
        }

        List<ReportDTO> reports = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT * FROM reports WHERE reporter_id = ? ORDER BY created_at DESC";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, userId);
                rs = ptm.executeQuery();
                
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
                
                System.out.println("getReportsByUserId: Retrieved " + reports.size() + " reports for user " + userId + " by admin " + adminId);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("getReportsByUserId error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("getReportsByUserId error: " + e.getMessage());
            throw new SQLException("Error getting reports by user ID: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return reports;
    }



    // Helper method to map ResultSet to ReportDTO
    private ReportDTO mapResultSetToReport(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int type = rs.getInt("type");
        String description = rs.getString("description");
        int reporterId = rs.getInt("reporter_id");
        Integer handlerId = rs.getObject("handler_id", Integer.class);
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        int status = rs.getInt("status");
        
        return new ReportDTO(id, type, description, reporterId, handlerId, createdAt, status);
    }



    // Helper method to check user role
    private boolean isUserRole(int userId, int expectedRole) throws SQLException {
        return getUserRole(userId) == expectedRole;
    }

    // Helper method to get user role
    private int getUserRole(int userId) throws SQLException {
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        int roleID = -1;
        
        String sql = "SELECT roleID FROM users WHERE userID = ?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, userId);
                rs = ptm.executeQuery();
                
                if (rs.next()) {
                    roleID = rs.getInt("roleID");
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("getUserRole error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("getUserRole error: " + e.getMessage());
            throw new SQLException("Error checking user role: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return roleID;
    }
}