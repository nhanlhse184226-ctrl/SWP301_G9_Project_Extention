package com.example.demo.dto;

/**
 * API Response DTO
 * Cấu trúc chuẩn cho tất cả API response
 */
public class ApiResponse<T> {
    private String status;      // "success" hoặc "error"
    private String message;     // Thông báo
    private T data;            // Dữ liệu (có thể là UserDTO, List, etc.)
    private String error;      // Chi tiết lỗi (nếu có)
    private long timestamp;    // Thời gian response
    
    // Constructors
    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public ApiResponse(String status, String message) {
        this();
        this.status = status;
        this.message = message;
    }
    
    public ApiResponse(String status, String message, T data) {
        this(status, message);
        this.data = data;
    }
    
    // Static methods để tạo response nhanh
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data);
    }
    
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>("success", message);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>("error", message);
        return response;
    }
    
    public static <T> ApiResponse<T> error(String message, String errorDetail) {
        ApiResponse<T> response = new ApiResponse<>("error", message);
        response.setError(errorDetail);
        return response;
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}