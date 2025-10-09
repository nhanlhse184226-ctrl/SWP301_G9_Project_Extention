package com.example.demo.dto;

import java.time.LocalDateTime;

public class ReportDTO {
    private int id;
    private int type;                    // 1=Station, 2=Slot, 3=Battery, 4=Other
    private String description;          // Không giới hạn ký tự
    private int reporterId;              // Foreign Key: Người gửi (roleID=3)
    private Integer handlerId;           // Foreign Key: Người xử lý (roleID=1), nullable
    private LocalDateTime createdAt;
    private int status;                  // 0=Pending, 1=InProgress, 2=Resolved

    // Default constructor
    public ReportDTO() {
        this.id = 0;
        this.type = 0;
        this.description = "";
        this.reporterId = 0;
        this.handlerId = null;
        this.createdAt = LocalDateTime.now();
        this.status = 0; // Default: Pending
    }

    // Constructor with all fields
    public ReportDTO(int id, int type, String description, int reporterId, Integer handlerId, 
                     LocalDateTime createdAt, int status) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.reporterId = reporterId;
        this.handlerId = handlerId;
        this.createdAt = createdAt;
        this.status = status;
    }

    // Constructor for creating new report (without id, handlerId, createdAt)
    public ReportDTO(int type, String description, int reporterId) {
        this.id = 0;
        this.type = type;
        this.description = description;
        this.reporterId = reporterId;
        this.handlerId = null;
        this.createdAt = LocalDateTime.now();
        this.status = 0; // Default: Pending
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getReporterId() {
        return reporterId;
    }

    public void setReporterId(int reporterId) {
        this.reporterId = reporterId;
    }

    public Integer getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(Integer handlerId) {
        this.handlerId = handlerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    // Utility methods for validation
    public boolean isValidType() {
        return type >= 1 && type <= 4; // 1=Station, 2=Slot, 3=Battery, 4=Other
    }

    public boolean isValidStatus() {
        return status >= 0 && status <= 2; // 0=Pending, 1=InProgress, 2=Resolved
    }

    public boolean isValidDescription() {
        return description != null && !description.trim().isEmpty();
    }

    public boolean isValidReporter() {
        return reporterId > 0;
    }

    // Get type name for display
    public String getTypeName() {
        switch (type) {
            case 1: return "Station";
            case 2: return "Slot";
            case 3: return "Battery";
            case 4: return "Other";
            default: return "Unknown";
        }
    }

    // Get status name for display
    public String getStatusName() {
        switch (status) {
            case 0: return "Pending";
            case 1: return "InProgress";
            case 2: return "Resolved";
            default: return "Unknown";
        }
    }

    @Override
    public String toString() {
        return "ReportDTO{" +
                "id=" + id +
                ", type=" + type + " (" + getTypeName() + ")" +
                ", description='" + description + '\'' +
                ", reporterId=" + reporterId +
                ", handlerId=" + handlerId +
                ", createdAt=" + createdAt +
                ", status=" + status + " (" + getStatusName() + ")" +
                '}';
    }
}