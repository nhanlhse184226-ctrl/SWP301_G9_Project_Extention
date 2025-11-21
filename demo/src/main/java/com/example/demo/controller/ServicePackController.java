package com.example.demo.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.ServicePackDAO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ServicePackDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller quản lý gói dịch vụ (Service Packages)
 * Xử lý các gói nạp tiền/dịch vụ cho user
 * 
 * Phân quyền:
 * - roleID=1 (User): Xem danh sách service packs có sẵn
 * - roleID=2 (Staff): Không có quyền truy cập service packs
 * - roleID=3 (Admin): Toàn quyền CRUD service packages
 * 
 * Workflow:
 * 1. Admin tạo service packages với giá khác nhau
 * 2. User xem danh sách packages và chọn để mua
 * 3. Admin có thể update/disable packages khi cần
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Service Pack Management", description = "APIs for managing service packages (Admin only - roleID=3)")
public class ServicePackController {

    // Khởi tạo DAO để truy cập database
    private final ServicePackDAO servicePackDAO = new ServicePackDAO();

    /**
     * API admin tạo service pack mới
     * Chỉ admin (roleID=3) có quyền tạo gói dịch vụ
     * @param adminUserID - ID admin từ session (verify quyền)
     * @param packName - Tên gói (VD: "Gói cơ bản", "Gói VIP")
     * @param status - Trạng thái: 0=inactive, 1=active
     * @param description - Mô tả gói (optional)
     * @param total - Số lượng/tổng dung lượng gói
     * @param price - Giá tiền (VND)
     * @return ResponseEntity chứa kết quả tạo service pack
     */
    // API để tạo ServicePack mới (chỉ admin)
    @PostMapping("/servicePack/create")
    @Operation(summary = "Create service pack", description = "Create a new service pack. Only admin users (roleID=3) can access this API.")
    public ResponseEntity<ApiResponse<Object>> createServicePack(
            @Parameter(description = "Admin user ID (must have roleID=3)", required = true) @RequestParam int adminUserID,
            @Parameter(description = "Service pack name", required = true) @RequestParam String packName,
            @Parameter(description = "Service pack status (0=inactive, 1=active)", required = true) @RequestParam int status,
            @Parameter(description = "Service pack description", required = false) @RequestParam(required = false) String description,
            @Parameter(description = "Total amount/quantity", required = true) @RequestParam int total,
            @Parameter(description = "Price in VND", required = true) @RequestParam int price) {
        
        try {
            // Kiểm tra adminUserID hợp lệ (verify quyền admin trong DAO)
            if (adminUserID <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Admin user ID must be greater than 0"));
            }

            // Kiểm tra tên gói không được rỗng
            if (packName == null || packName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Pack name cannot be null or empty"));
            }

            // Kiểm tra status hợp lệ (0 hoặc 1)
            if (status < 0 || status > 1) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Status must be 0 (inactive) or 1 (active)"));
            }

            // Kiểm tra total không âm
            if (total < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Total must be non-negative"));
            }

            // Kiểm tra giá không âm
            if (price < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Price must be non-negative"));
            }

            // Tạo đối tượng ServicePackDTO với data đã validate
            ServicePackDTO servicePack = new ServicePackDTO(packName, status, description, total, price);

            // Gọi DAO để lưu service pack vào database
            boolean success = servicePackDAO.createServicePack(servicePack, adminUserID);

            // Kiểm tra kết quả tạo service pack
            if (success) {
                String message = "Service pack '" + packName + "' created successfully";
                return ResponseEntity.ok(ApiResponse.success(message, servicePack));
            } else {
                return ResponseEntity.internalServerError()
                        .body(ApiResponse.error("Failed to create service pack"));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database và in ra console
            System.out.println("Database error in createServicePack: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi hệ thống khác
            System.out.println("Error creating service pack: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error creating service pack: " + e.getMessage()));
        }
    }

    /**
     * API admin cập nhật thông tin service pack
     * Chỉ admin (roleID=3) có quyền cập nhật gói dịch vụ
     * @param packID - ID của service pack cần cập nhật
     * @param adminUserID - ID admin từ session (verify quyền)
     * @param packName - Tên gói mới (optional)
     * @param description - Mô tả mới (optional)
     * @param total - Tổng dung lượng mới
     * @param price - Giá mới (VND)
     * @return ResponseEntity chứa kết quả cập nhật
     */
    // API để update ServicePack (chỉ admin)
    @PutMapping("/servicePack/update")
    @Operation(summary = "Update service pack", description = "Update an existing service pack. Only admin users (roleID=3) can access this API.")
    public ResponseEntity<ApiResponse<Object>> updateServicePack(
            @Parameter(description = "Service pack ID to update", required = true) @RequestParam int packID,
            @Parameter(description = "Admin user ID (must have roleID=3)", required = true) @RequestParam int adminUserID,
            @Parameter(description = "Service pack name") @RequestParam(required = false) String packName,
            @Parameter(description = "Service pack description", required = false) @RequestParam(required = false) String description,
            @Parameter(description = "Total amount/quantity", required = true) @RequestParam int total,
            @Parameter(description = "Price in VND", required = true) @RequestParam int price) {

        try {
            // Kiểm tra packID hợp lệ
            if (packID <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Pack ID must be greater than 0"));
            }

            // Kiểm tra adminUserID hợp lệ (verify quyền admin trong DAO)
            if (adminUserID <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Admin user ID must be greater than 0"));
            }

            // Kiểm tra tên gói không được rỗng (nếu có)
            if (packName == null || packName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Pack name cannot be null or empty"));
            }

            // Kiểm tra total không âm
            if (total < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Total must be non-negative"));
            }

            // Kiểm tra giá không âm
            if (price < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Price must be non-negative"));
            }

            // Tạo ServicePackDTO với data mới đã validate
            ServicePackDTO servicePack = new ServicePackDTO(packName, description, total, price);

            // Gọi DAO để cập nhật service pack trong database
            boolean success = servicePackDAO.updateServicePack(packID, servicePack, adminUserID);

            // Kiểm tra kết quả cập nhật
            if (success) {
                // Lấy service pack đã cập nhật để trả về cho client
                ServicePackDTO updatedPack = servicePackDAO.getServicePackById(packID);
                String message = "Service pack with ID " + packID + " updated successfully";
                return ResponseEntity.ok(ApiResponse.success(message, updatedPack));
            } else {
                return ResponseEntity.internalServerError()
                        .body(ApiResponse.error("Failed to update service pack"));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database và in ra console
            System.out.println("Database error in updateServicePack: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi hệ thống khác
            System.out.println("Error updating service pack: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error updating service pack: " + e.getMessage()));
        }
    }

    /**
     * API admin cập nhật chỉ trạng thái của service pack
     * Chỉ admin (roleID=3) có quyền enable/disable gói
     * @param packID - ID của service pack cần cập nhật
     * @param adminUserID - ID admin từ session (verify quyền)
     * @param status - Trạng thái mới: 0=inactive, 1=active
     * @return ResponseEntity chứa kết quả cập nhật trạng thái
     */
    // API để update chỉ status của ServicePack (chỉ admin)
    @PutMapping("/servicePack/updateStatus")
    @Operation(summary = "Update service pack status only", description = "Update only the status of an existing service pack. Only admin users (roleID=3) can access this API.")
    public ResponseEntity<ApiResponse<Object>> updateServicePackStatus(
            @Parameter(description = "Service pack ID to update", required = true) @RequestParam int packID,
            @Parameter(description = "Admin user ID (must have roleID=3)", required = true) @RequestParam int adminUserID,
            @Parameter(description = "New service pack status (0=inactive, 1=active)", required = true) @RequestParam int status) {
        
        try {
            // Kiểm tra packID hợp lệ
            if (packID <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Pack ID must be greater than 0"));
            }

            // Kiểm tra adminUserID hợp lệ (verify quyền admin trong DAO)
            if (adminUserID <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Admin user ID must be greater than 0"));
            }

            // Kiểm tra status hợp lệ (chỉ 0 hoặc 1)
            if (status < 0 || status > 1) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Status must be 0 (inactive) or 1 (active)"));
            }

            // Gọi DAO để cập nhật chỉ status của service pack
            // Không thay đổi các thông tin khác (tên, giá, description...)
            boolean success = servicePackDAO.updateServicePackStatus(packID, status, adminUserID);

            // Kiểm tra kết quả cập nhật
            if (success) {
                // Lấy service pack đã cập nhật để trả về
                ServicePackDTO updatedPack = servicePackDAO.getServicePackById(packID);
                // Chuyển số status thành text cho dễ hiểu
                String statusText = (status == 0) ? "inactive" : "active";
                String message = "Service pack status updated successfully to " + statusText;
                return ResponseEntity.ok(ApiResponse.success(message, updatedPack));
            } else {
                return ResponseEntity.internalServerError()
                        .body(ApiResponse.error("Failed to update service pack status"));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database và in ra console
            System.out.println("Database error in updateServicePackStatus: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi hệ thống khác
            System.out.println("Error updating service pack status: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error updating service pack status: " + e.getMessage()));
        }
    }

    /**
     * API lấy danh sách tất cả service packages
     * Tất cả role có thể truy cập để xem gói có sẵn
     * @return ResponseEntity chứa danh sách service packages
     */
    // API để lấy danh sách tất cả ServicePack
    @GetMapping("/servicePack/list")
    @Operation(summary = "Get all service packs", description = "Retrieve all service packs from the database, ordered by creation date (newest first).")
    public ResponseEntity<ApiResponse<Object>> getListServicePack() {
        try {
            // Gọi DAO để lấy tất cả service packages
            // Sắp xếp theo thời gian tạo (mới nhất trước)
            // Không cần verify role vì tất cả user đều có thể xem danh sách
            List<ServicePackDTO> listServicePack = servicePackDAO.getListServicePack();

            // Kiểm tra kết quả và trả về response
            if (listServicePack != null && !listServicePack.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Get service pack list successful", listServicePack));
            } else {
                // Không có service packs nào (admin chưa tạo)
                return ResponseEntity.ok(ApiResponse.success("No service packs found", listServicePack));
            }

        } catch (SQLException e) {
            // Xử lý lỗi database và in ra console
            System.out.println("Database error in getListServicePack: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            // Xử lý lỗi hệ thống khác
            System.out.println("Error getting service pack list: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error getting service pack list: " + e.getMessage()));
        }
    }
}
