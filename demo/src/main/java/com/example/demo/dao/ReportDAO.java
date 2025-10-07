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
    
    // SQL Queries
    private static final String CREATE_REPORT = 
        "INSERT INTO reports(type, description, reporter_id, handler_id, created_at, status) VALUES(?,?,?,?,?,?)";
    
    private static final String GET_ALL_REPORTS = 
        "SELECT * FROM reports ORDER BY created_at DESC";
    
    private static final String GET_PENDING_REPORTS = 
        "SELECT * FROM reports WHERE status = 0 ORDER BY created_at ASC";
    
    private static final String GET_REPORTS_BY_STATUS = 
        "SELECT * FROM reports WHERE status = ? ORDER BY created_at DESC";
    
    private static final String GET_REPORT_BY_ID = 
        "SELECT * FROM reports WHERE id = ?";
    
    private static final String UPDATE_REPORT_STATUS = 
        "UPDATE reports SET status = ?, handler_id = ? WHERE id = ?";
    
    private static final String GET_REPORTS_BY_REPORTER = 
        "SELECT * FROM reports WHERE reporter_id = ? ORDER BY created_at DESC";

    // Check if user has admin role (roleID = 1)
    private static final String CHECK_USER_ROLE = 
        "SELECT roleID FROM users WHERE userID = ?";

    /**
     * Create a new report
     * Only users with roleID = 3 can create reports
     */
    public boolean createReport(ReportDTO report) throws SQLException {
        // Validate input
        if (report == null || !report.isValidType() || !report.isValidDescription() || !report.isValidReporter()) {
            throw new SQLException("Invalid report data");
        }

        // Check if reporter has correct role (roleID = 3)
        if (!isUserRole(report.getReporterId(), 3)) {
            throw new SQLException("Only users with roleID = 3 can create reports");
        }

        boolean result = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(CREATE_REPORT);
                ptm.setInt(1, report.getType());
                ptm.setString(2, report.getDescription());
                ptm.setInt(3, report.getReporterId());
                ptm.setNull(4, java.sql.Types.INTEGER); // handler_id is null initially
                ptm.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                ptm.setInt(6, 0); // status = 0 (Pending)
                
                result = ptm.executeUpdate() > 0;
                
                System.out.println("Report created - Type: " + report.getType() + 
                                 ", Reporter: " + report.getReporterId() + 
                                 ", Description length: " + report.getDescription().length());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error creating report: " + e.getMessage());
        } finally {
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return result;
    }

    /**
     * Get all reports - Only admins (roleID = 1) can access
     */
    public List<ReportDTO> getAllReports(int adminId) throws SQLException {
        // Check if user is admin
        if (!isUserRole(adminId, 1)) {
            throw new SQLException("Only admins can view all reports");
        }

        return getReportsList(GET_ALL_REPORTS, null);
    }

    /**
     * Get pending reports - Only admins (roleID = 1) can access
     */
    public List<ReportDTO> getPendingReports(int adminId) throws SQLException {
        // Check if user is admin
        if (!isUserRole(adminId, 1)) {
            throw new SQLException("Only admins can view pending reports");
        }

        return getReportsList(GET_PENDING_REPORTS, null);
    }

    /**
     * Get reports by status - Only admins (roleID = 1) can access
     */
    public List<ReportDTO> getReportsByStatus(int adminId, int status) throws SQLException {
        // Check if user is admin
        if (!isUserRole(adminId, 1)) {
            throw new SQLException("Only admins can filter reports by status");
        }

        if (status < 0 || status > 2) {
            throw new SQLException("Invalid status value");
        }

        return getReportsList(GET_REPORTS_BY_STATUS, status);
    }

    /**
     * Get report by ID
     */
    public ReportDTO getReportById(int reportId, int userId) throws SQLException {
        // Admin can view any report, user can only view their own reports
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        ReportDTO report = null;
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(GET_REPORT_BY_ID);
                ptm.setInt(1, reportId);
                rs = ptm.executeQuery();
                
                if (rs.next()) {
                    report = mapResultSetToReport(rs);
                    
                    // Check permission: admin can view all, user can only view their own
                    int userRole = getUserRole(userId);
                    if (userRole != 1 && report.getReporterId() != userId) {
                        throw new SQLException("Access denied: You can only view your own reports");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error getting report: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return report;
    }

    /**
     * Update report status - Only admins (roleID = 1) can update
     */
    public boolean updateReportStatus(int reportId, int newStatus, int adminId) throws SQLException {
        // Check if user is admin
        if (!isUserRole(adminId, 1)) {
            throw new SQLException("Only admins can update report status");
        }

        if (newStatus < 0 || newStatus > 2) {
            throw new SQLException("Invalid status value");
        }

        boolean result = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(UPDATE_REPORT_STATUS);
                ptm.setInt(1, newStatus);
                
                if (newStatus > 0) {
                    // If moving from Pending to InProgress or Resolved, set admin as handler
                    ptm.setInt(2, adminId);
                } else {
                    ptm.setNull(2, java.sql.Types.INTEGER);
                }
                
                ptm.setInt(3, reportId);
                result = ptm.executeUpdate() > 0;
                
                System.out.println("Report " + reportId + " status updated to " + newStatus + 
                                 " by admin " + adminId);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        // Verify that the reporterId corresponds to a user (roleID = 3)
        if (!isUserRole(reporterId, 3)) {
            throw new SQLException("Invalid reporter ID");
        }

        return getReportsList(GET_REPORTS_BY_REPORTER, reporterId);
    }

    /**
     * Get reports by user ID (for admin to view any user's reports)
     */
    public List<ReportDTO> getReportsByUserId(int userId, int adminId) throws SQLException {
        // Verify that adminId is an admin (roleID = 1)
        if (!isUserRole(adminId, 1)) {
            throw new SQLException("Access denied. Only admins can view reports by user ID.");
        }

        // Verify that the userId exists and is a user (roleID = 3)
        if (!isUserRole(userId, 3)) {
            throw new SQLException("Invalid user ID or user not found");
        }

        return getReportsList(GET_REPORTS_BY_REPORTER, userId);
    }

    // Helper method to execute SELECT queries and return list of reports
    private List<ReportDTO> getReportsList(String query, Integer parameter) throws SQLException {
        List<ReportDTO> reports = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(query);
                if (parameter != null) {
                    ptm.setInt(1, parameter);
                }
                rs = ptm.executeQuery();
                
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error getting reports: " + e.getMessage());
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
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(CHECK_USER_ROLE);
                ptm.setInt(1, userId);
                rs = ptm.executeQuery();
                
                if (rs.next()) {
                    roleID = rs.getInt("roleID");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error checking user role: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return roleID;
    }
}