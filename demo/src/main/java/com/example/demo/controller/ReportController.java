package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.ReportDAO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ReportDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Report Controller for handling report-related operations
 * - roleID=1 (User): Can create reports and view their own reports
 * - roleID=2 (Staff): Can view assigned reports and update status of assigned reports
 * - roleID=3 (Admin): Can view all reports, assign reports to staff, update any report status
 * 
 * Workflow:
 * 1. User creates report (status=Pending)
 * 2. Admin assigns report to Staff
 * 3. Staff updates report status (InProgress -> Resolved)
 * 4. Admin can reassign or override any status
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Report Management", description = "APIs for managing user reports and admin handling")
public class ReportController {

    /**
     * POST /api/report/create - User tạo report
     * userID được gửi tự động từ FE session, user không nhập
     * Only users with roleID=1 can create reports
     */
    @PostMapping("/report/create")
    @Operation(summary = "Create new report", 
               description = "User creates a new report about station/slot/battery issues. " +
                           "UserID is automatically taken from frontend session (only roleID=1). " +
                           "User selects issue type and provides detailed description (no character limit). " +
                           "System automatically sets status=0 (Pending) and created_at=now.")
    public ResponseEntity<ApiResponse<Object>> createReport(
            @Parameter(description = "User ID from frontend session (automatic, user doesn't input)", 
                      required = true, example = "123") 
            @RequestParam int userID,  // FE tự động gửi từ session, không hiển thị cho user
            @Parameter(description = "Issue type: 1=Station Problem, 2=Slot Malfunction, 3=Battery Issue, 4=Other", 
                      required = true, example = "1") 
            @RequestParam int type,    // User chọn: 1=Station, 2=Slot, 3=Battery, 4=Other
            @Parameter(description = "Detailed description of the problem (no character limit, can be very long)", 
                      required = true, example = "Station screen is flickering and not responding to touch input") 
            @RequestParam String description) {  // User nhập mô tả (không giới hạn ký tự)
        try {
            ReportDAO dao = new ReportDAO();
            
            // Validate userID exists and has correct role (roleID=1)
            if (userID <= 0) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Invalid user ID from session"));
            }
            
            // Validate type (1-4)
            if (type < 1 || type > 4) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Invalid type. Must be 1=Station, 2=Slot, 3=Battery, 4=Other"));
            }
            
            // Validate description (không giới hạn ký tự, chỉ check không empty)
            if (description == null || description.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Description cannot be empty"));
            }
            
            // Create new report với userID từ session
            ReportDTO newReport = new ReportDTO(type, description.trim(), userID);
            boolean result = dao.createReport(newReport);
            
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("Report created successfully", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create report"));
            }
            
        } catch (Exception e) {
            System.out.println("Error at ReportController - createReport: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/report/all - Admin xem tất cả reports
     * Only admins with roleID=3 can access
     */
    @GetMapping("/report/all")
    @Operation(summary = "Get all reports", 
               description = "Admin views all reports in the system (roleID=3 only). " +
                           "Returns complete list of reports with all details including " +
                           "reporter info, handler info, status, type, and timestamps. " +
                           "Used for admin dashboard and system overview.")
    public ResponseEntity<ApiResponse<Object>> getAllReports(
            @Parameter(description = "Admin user ID from session", required = true, example = "1")
            @RequestParam int adminID) {
        try {
            ReportDAO dao = new ReportDAO();
            
            if (adminID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid admin ID"));
            }
            
            List<ReportDTO> reports = dao.getAllReports(adminID);
            
            if (reports != null && !reports.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Retrieved all reports successfully", reports));
            } else {
                return ResponseEntity.ok(ApiResponse.success("No reports found", reports));
            }
            
        } catch (Exception e) {
            System.out.println("Error at ReportController - getAllReports: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/report/pending - Admin xem pending reports
     * Only admins with roleID=3 can access
     */
    @GetMapping("/report/pending")
    @Operation(summary = "Get pending reports", 
               description = "Admin views pending reports (status=0) for processing. " +
                           "Shows reports that need attention and haven't been assigned to any handler yet. " +
                           "This is the main dashboard for admins to see incoming issues. " +
                           "Reports are ordered by creation time (oldest first).")
    public ResponseEntity<ApiResponse<Object>> getPendingReports(
            @Parameter(description = "Admin user ID from session", required = true, example = "1")
            @RequestParam int adminID) {
        try {
            ReportDAO dao = new ReportDAO();
            
            if (adminID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid admin ID"));
            }
            
            List<ReportDTO> pendingReports = dao.getPendingReports(adminID);
            
            if (pendingReports != null && !pendingReports.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Retrieved pending reports successfully", pendingReports));
            } else {
                return ResponseEntity.ok(ApiResponse.success("No pending reports found", pendingReports));
            }
            
        } catch (Exception e) {
            System.out.println("Error at ReportController - getPendingReports: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error: " + e.getMessage()));
        }
    }





    /**
     * PUT /api/report/{reportId}/status - Enhanced status update for both Admin and Staff
     * Admin (roleID=3): Can update any report status
     * Staff (roleID=2): Can update status of assigned reports only
     */
    @PutMapping("/report/{reportId}/status")
    @Operation(summary = "Update report status", 
               description = "Enhanced status update supporting both Admin and Staff workflows. " +
                           "Admin (roleID=3) can update any report status and change handler assignment. " +
                           "Staff (roleID=2) can only update status of reports assigned to them. " +
                           "Status flow: 0(Pending) → 1(InProgress) → 2(Resolved). " +
                           "Supports role-based access control for better workflow management.")
    public ResponseEntity<ApiResponse<Object>> updateReportStatus(
            @Parameter(description = "Report ID to update", required = true, example = "1")
            @PathVariable int reportId,
            @Parameter(description = "New status: 0=Pending, 1=InProgress, 2=Resolved", required = true, example = "2")
            @RequestParam int status,
            @Parameter(description = "User ID from session (Admin or Staff)", required = true, example = "456")
            @RequestParam int userID) {
        try {
            ReportDAO dao = new ReportDAO();
            
            if (reportId <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid report ID"));
            }
            
            if (userID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID"));
            }
            
            if (status < 0 || status > 2) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Invalid status. Must be 0=Pending, 1=InProgress, 2=Resolved"));
            }
            
            boolean result = dao.updateReportStatusV2(reportId, status, userID);
            
            if (result) {
                String statusName = (status == 0) ? "Pending" : (status == 1) ? "InProgress" : "Resolved";
                return ResponseEntity.ok(ApiResponse.success("Report status updated to " + statusName + " successfully", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update report status or report not found"));
            }
            
        } catch (Exception e) {
            System.out.println("Error at ReportController - updateReportStatus: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/report/my-reports - User xem reports của mình
     * Only for users with roleID=1
     */
    @GetMapping("/report/my-reports")
    @Operation(summary = "Get my reports", 
               description = "User views their own submitted reports (roleID=1 only). " +
                           "Shows all reports created by the current user with current status, " +
                           "handler information (if assigned), and resolution progress. " +
                           "Users can track the status of their submitted issues. " +
                           "Ordered by creation time (newest first).")
    public ResponseEntity<ApiResponse<Object>> getMyReports(
            @Parameter(description = "User ID from session", required = true, example = "123")
            @RequestParam int userID) {
        try {
            ReportDAO dao = new ReportDAO();
            
            if (userID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID"));
            }
            
            List<ReportDTO> myReports = dao.getReportsByReporter(userID);
            
            if (myReports != null && !myReports.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Retrieved your reports successfully", myReports));
            } else {
                return ResponseEntity.ok(ApiResponse.success("You have no reports yet", myReports));
            }
            
        } catch (Exception e) {
            System.out.println("Error at ReportController - getMyReports: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/report/user/{userId} - Admin xem reports theo user ID
     * Only for admins with roleID=3
     */
    @GetMapping("/report/user/{userId}")
    @Operation(summary = "Get reports by user ID", 
               description = "Admin views all reports from specific user (roleID=3 only). " +
                           "Returns complete list of reports submitted by the specified user " +
                           "including status, descriptions, and handler assignments. " +
                           "Useful for admin to track user activity and support history.")
    public ResponseEntity<ApiResponse<Object>> getReportsByUserId(
            @Parameter(description = "Target user ID to get reports from", required = true, example = "123")
            @PathVariable int userId,
            @Parameter(description = "Admin ID from session", required = true, example = "456") 
            @RequestParam int adminID) {
        try {
            ReportDAO dao = new ReportDAO();
            
            if (userId <= 0 || adminID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid user ID or admin ID"));
            }
            
            List<ReportDTO> userReports = dao.getReportsByUserId(userId, adminID);
            
            if (userReports != null && !userReports.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Retrieved user reports successfully", userReports));
            } else {
                return ResponseEntity.ok(ApiResponse.success("User has no reports", userReports));
            }
            
        } catch (Exception e) {
            System.out.println("Error at ReportController - getReportsByUserId: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/report/{reportId}/assign - Admin assign report to staff
     * Only for admins with roleID=3
     */
    @PutMapping("/report/{reportId}/assign")
    @Operation(summary = "Assign report to staff", 
               description = "Admin assigns a report to a staff member for handling (roleID=3 only). " +
                           "Admin selects which staff member will be responsible for resolving the issue. " +
                           "Once assigned, the staff member can update the report status. " +
                           "Used for distributing workload among staff members.")
    public ResponseEntity<ApiResponse<Object>> assignReportToStaff(
            @Parameter(description = "Report ID to assign", required = true, example = "1")
            @PathVariable int reportId,
            @Parameter(description = "Staff user ID to assign report to", required = true, example = "789")
            @RequestParam int staffID,
            @Parameter(description = "Admin user ID from session", required = true, example = "456")
            @RequestParam int adminID) {
        try {
            ReportDAO dao = new ReportDAO();
            
            if (reportId <= 0 || staffID <= 0 || adminID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid report ID, staff ID, or admin ID"));
            }
            
            boolean result = dao.assignReportToStaff(reportId, staffID, adminID);
            
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("Report assigned to staff successfully", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to assign report or report not found"));
            }
            
        } catch (Exception e) {
            System.out.println("Error at ReportController - assignReportToStaff: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/report/assigned - Staff view assigned reports
     * Only for staff with roleID=2
     */
    @GetMapping("/report/assigned")
    @Operation(summary = "Get assigned reports", 
               description = "Staff views reports assigned to them for handling (roleID=2 only). " +
                           "Shows reports that admin has assigned to this staff member. " +
                           "Staff can update status of these reports from InProgress to Resolved. " +
                           "Ordered by creation time (oldest first for priority handling).")
    public ResponseEntity<ApiResponse<Object>> getAssignedReports(
            @Parameter(description = "Staff user ID from session", required = true, example = "789")
            @RequestParam int staffID) {
        try {
            ReportDAO dao = new ReportDAO();
            
            if (staffID <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid staff ID"));
            }
            
            List<ReportDTO> assignedReports = dao.getReportsByHandler(staffID, staffID);
            
            if (assignedReports != null && !assignedReports.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Retrieved assigned reports successfully", assignedReports));
            } else {
                return ResponseEntity.ok(ApiResponse.success("No reports assigned to you", assignedReports));
            }
            
        } catch (Exception e) {
            System.out.println("Error at ReportController - getAssignedReports: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error: " + e.getMessage()));
        }
    }
}