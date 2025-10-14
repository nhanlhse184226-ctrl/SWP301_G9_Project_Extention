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

@RestController
@RequestMapping("/api")
@Tag(name = "Service Pack Management", description = "APIs for managing service packages (Admin only - roleID=3)")
public class ServicePackController {

    private final ServicePackDAO servicePackDAO = new ServicePackDAO();

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
            // Validation
            if (adminUserID <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Admin user ID must be greater than 0"));
            }

            if (packName == null || packName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Pack name cannot be null or empty"));
            }

            if (status < 0 || status > 1) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Status must be 0 (inactive) or 1 (active)"));
            }

            if (total < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Total must be non-negative"));
            }

            if (price < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Price must be non-negative"));
            }

            // Tạo ServicePackDTO
            ServicePackDTO servicePack = new ServicePackDTO(packName, status, description, total, price);

            // Gọi DAO để tạo service pack
            boolean success = servicePackDAO.createServicePack(servicePack, adminUserID);

            if (success) {
                String message = "Service pack '" + packName + "' created successfully";
                return ResponseEntity.ok(ApiResponse.success(message, servicePack));
            } else {
                return ResponseEntity.internalServerError()
                        .body(ApiResponse.error("Failed to create service pack"));
            }

        } catch (SQLException e) {
            System.out.println("Database error in createServicePack: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error creating service pack: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error creating service pack: " + e.getMessage()));
        }
    }

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
            // Validation
            if (packID <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Pack ID must be greater than 0"));
            }

            if (adminUserID <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Admin user ID must be greater than 0"));
            }

            if (packName == null || packName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Pack name cannot be null or empty"));
            }

            if (total < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Total must be non-negative"));
            }

            if (price < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Price must be non-negative"));
            }

            // Tạo ServicePackDTO với data mới
            ServicePackDTO servicePack = new ServicePackDTO(packName, description, total, price);

            // Gọi DAO để update service pack
            boolean success = servicePackDAO.updateServicePack(packID, servicePack, adminUserID);

            if (success) {
                // Lấy updated service pack để trả về
                ServicePackDTO updatedPack = servicePackDAO.getServicePackById(packID);
                String message = "Service pack with ID " + packID + " updated successfully";
                return ResponseEntity.ok(ApiResponse.success(message, updatedPack));
            } else {
                return ResponseEntity.internalServerError()
                        .body(ApiResponse.error("Failed to update service pack"));
            }

        } catch (SQLException e) {
            System.out.println("Database error in updateServicePack: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error updating service pack: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error updating service pack: " + e.getMessage()));
        }
    }

    // API để update chỉ status của ServicePack (chỉ admin)
    @PutMapping("/servicePack/updateStatus")
    @Operation(summary = "Update service pack status only", description = "Update only the status of an existing service pack. Only admin users (roleID=3) can access this API.")
    public ResponseEntity<ApiResponse<Object>> updateServicePackStatus(
            @Parameter(description = "Service pack ID to update", required = true) @RequestParam int packID,
            @Parameter(description = "Admin user ID (must have roleID=3)", required = true) @RequestParam int adminUserID,
            @Parameter(description = "New service pack status (0=inactive, 1=active)", required = true) @RequestParam int status) {
        
        try {
            // Validation
            if (packID <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Pack ID must be greater than 0"));
            }

            if (adminUserID <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Admin user ID must be greater than 0"));
            }

            if (status < 0 || status > 1) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Status must be 0 (inactive) or 1 (active)"));
            }

            // Gọi DAO để update chỉ status
            boolean success = servicePackDAO.updateServicePackStatus(packID, status, adminUserID);

            if (success) {
                // Lấy updated service pack để trả về
                ServicePackDTO updatedPack = servicePackDAO.getServicePackById(packID);
                String statusText = (status == 0) ? "inactive" : "active";
                String message = "Service pack status updated successfully to " + statusText;
                return ResponseEntity.ok(ApiResponse.success(message, updatedPack));
            } else {
                return ResponseEntity.internalServerError()
                        .body(ApiResponse.error("Failed to update service pack status"));
            }

        } catch (SQLException e) {
            System.out.println("Database error in updateServicePackStatus: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error updating service pack status: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error updating service pack status: " + e.getMessage()));
        }
    }

    // API để lấy danh sách tất cả ServicePack
    @GetMapping("/servicePack/list")
    @Operation(summary = "Get all service packs", description = "Retrieve all service packs from the database, ordered by creation date (newest first).")
    public ResponseEntity<ApiResponse<Object>> getListServicePack() {
        try {
            List<ServicePackDTO> listServicePack = servicePackDAO.getListServicePack();

            if (listServicePack != null && !listServicePack.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Get service pack list successful", listServicePack));
            } else {
                return ResponseEntity.ok(ApiResponse.success("No service packs found", listServicePack));
            }

        } catch (SQLException e) {
            System.out.println("Database error in getListServicePack: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Database error: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error getting service pack list: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error getting service pack list: " + e.getMessage()));
        }
    }
}
