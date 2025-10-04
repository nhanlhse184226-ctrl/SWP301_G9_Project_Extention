package com.example.demo.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.PaymentDTO;

public class PaymentDAO {
    
    // Kiểm tra user có tồn tại không
    public boolean isUserExists(int userID) throws SQLException {
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT COUNT(1) FROM userDetails WHERE userID = ?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, userID);
                rs = ptm.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error checking user existence: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        return false;
    }
    
    // Kiểm tra payment status hợp lệ
    public boolean isValidPaymentStatus(String status) {
        return status != null && 
               (status.equals("pending") || status.equals("completed") || 
                status.equals("failed") || status.equals("cancelled"));
    }
    
    // Kiểm tra payment method hợp lệ
    public boolean isValidPaymentMethod(String method) {
        return method != null && 
               (method.equals("credit_card") || method.equals("debit_card") || 
                method.equals("bank_transfer") || method.equals("cash") || 
                method.equals("e_wallet"));
    }
    
    // Kiểm tra amount hợp lệ
    public boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    // Tạo payment mới
    public int createPayment(PaymentDTO payment) throws SQLException {
        // Enhanced validation
        if (payment == null) {
            throw new SQLException("Payment data cannot be null");
        }
        if (payment.getUserID() <= 0) {
            throw new SQLException("Invalid user ID");
        }
        if (!isUserExists(payment.getUserID())) {
            throw new SQLException("User with ID " + payment.getUserID() + " does not exist");
        }
        if (!isValidAmount(payment.getAmount())) {
            throw new SQLException("Payment amount must be greater than 0");
        }
        if (payment.getPaymentStatus() == null || payment.getPaymentStatus().trim().isEmpty()) {
            throw new SQLException("Payment status cannot be null or empty");
        }
        if (!isValidPaymentStatus(payment.getPaymentStatus())) {
            throw new SQLException("Invalid payment status. Valid values: pending, completed, failed, cancelled");
        }
        if (payment.getPaymentMethod() == null || payment.getPaymentMethod().trim().isEmpty()) {
            throw new SQLException("Payment method cannot be null or empty");
        }
        if (!isValidPaymentMethod(payment.getPaymentMethod())) {
            throw new SQLException("Invalid payment method. Valid values: credit_card, debit_card, bank_transfer, cash, e_wallet");
        }
        if (payment.getTransactionID() == null || payment.getTransactionID().trim().isEmpty()) {
            throw new SQLException("Transaction ID cannot be null or empty");
        }
        
        int generatedID = 0;
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "INSERT INTO payment (userID, amount, paymentStatus, paymentMethod, createdTime, description, transactionID) " +
                    "VALUES (?, ?, ?, ?, GETDATE(), ?, ?)";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ptm.setInt(1, payment.getUserID());
                ptm.setBigDecimal(2, payment.getAmount());
                ptm.setString(3, payment.getPaymentStatus().trim());
                ptm.setString(4, payment.getPaymentMethod().trim());
                ptm.setString(5, payment.getDescription() != null ? payment.getDescription().trim() : "");
                ptm.setString(6, payment.getTransactionID().trim());
                
                int rowsAffected = ptm.executeUpdate();
                
                if (rowsAffected > 0) {
                    rs = ptm.getGeneratedKeys();
                    if (rs.next()) {
                        generatedID = rs.getInt(1);
                    }
                }
                
                System.out.println("Payment created - ID: " + generatedID + ", UserID: " + payment.getUserID() + 
                                 ", Amount: " + payment.getAmount() + ", Method: " + payment.getPaymentMethod());
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error in createPayment: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error in createPayment: " + e.getMessage());
            throw new SQLException("Failed to create payment: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return generatedID;
    }
    
    // Lấy payment theo ID
    public PaymentDTO getPaymentById(int paymentID) throws SQLException {
        PaymentDTO payment = null;
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT * FROM payment WHERE paymentID = ?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, paymentID);
                rs = ptm.executeQuery();
                
                if (rs.next()) {
                    int userID = rs.getInt("userID");
                    BigDecimal amount = rs.getBigDecimal("amount");
                    String paymentStatus = rs.getString("paymentStatus");
                    String paymentMethod = rs.getString("paymentMethod");
                    Timestamp createdTimeStamp = rs.getTimestamp("createdTime");
                    LocalDateTime createdTime = (createdTimeStamp != null) ? createdTimeStamp.toLocalDateTime() : null;
                    String description = rs.getString("description");
                    String transactionID = rs.getString("transactionID");
                    
                    payment = new PaymentDTO(paymentID, userID, amount, paymentStatus, 
                                           paymentMethod, createdTime, description, transactionID);
                }
            }
        } catch (Exception e) {
            System.out.println("Error in getPaymentById: " + e.getMessage());
            throw new SQLException("Error getting payment by ID: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return payment;
    }
    
    // Lấy danh sách payments của user
    public List<PaymentDTO> getPaymentsByUserID(int userID) throws SQLException {
        List<PaymentDTO> payments = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT * FROM payment WHERE userID = ? ORDER BY createdTime DESC";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, userID);
                rs = ptm.executeQuery();
                
                while (rs.next()) {
                    int paymentID = rs.getInt("paymentID");
                    BigDecimal amount = rs.getBigDecimal("amount");
                    String paymentStatus = rs.getString("paymentStatus");
                    String paymentMethod = rs.getString("paymentMethod");
                    Timestamp createdTimeStamp = rs.getTimestamp("createdTime");
                    LocalDateTime createdTime = (createdTimeStamp != null) ? createdTimeStamp.toLocalDateTime() : null;
                    String description = rs.getString("description");
                    String transactionID = rs.getString("transactionID");
                    
                    payments.add(new PaymentDTO(paymentID, userID, amount, paymentStatus, 
                                              paymentMethod, createdTime, description, transactionID));
                }
            }
        } catch (Exception e) {
            System.out.println("Error in getPaymentsByUserID: " + e.getMessage());
            throw new SQLException("Error getting payments by user ID: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return payments;
    }
    
    // Cập nhật trạng thái payment
    public boolean updatePaymentStatus(int paymentID, String newStatus, String transactionID) throws SQLException {
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        
        String sql = "UPDATE payment SET paymentStatus = ?, transactionID = ? WHERE paymentID = ?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setString(1, newStatus);
                ptm.setString(2, transactionID);
                ptm.setInt(3, paymentID);
                
                int rowsAffected = ptm.executeUpdate();
                success = (rowsAffected > 0);
                
                System.out.println("Payment status updated - PaymentID: " + paymentID + 
                                 ", New Status: " + newStatus + ", Rows affected: " + rowsAffected);
            }
        } catch (Exception e) {
            System.out.println("Error in updatePaymentStatus: " + e.getMessage());
            throw new SQLException("Error updating payment status: " + e.getMessage());
        } finally {
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return success;
    }
    
    // Lấy tất cả payments (cho admin)
    public List<PaymentDTO> getAllPayments() throws SQLException {
        List<PaymentDTO> payments = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT * FROM payment ORDER BY createdTime DESC";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                rs = ptm.executeQuery();
                
                while (rs.next()) {
                    int paymentID = rs.getInt("paymentID");
                    int userID = rs.getInt("userID");
                    BigDecimal amount = rs.getBigDecimal("amount");
                    String paymentStatus = rs.getString("paymentStatus");
                    String paymentMethod = rs.getString("paymentMethod");
                    Timestamp createdTimeStamp = rs.getTimestamp("createdTime");
                    LocalDateTime createdTime = (createdTimeStamp != null) ? createdTimeStamp.toLocalDateTime() : null;
                    String description = rs.getString("description");
                    String transactionID = rs.getString("transactionID");
                    
                    payments.add(new PaymentDTO(paymentID, userID, amount, paymentStatus, 
                                              paymentMethod, createdTime, description, transactionID));
                }
            }
        } catch (Exception e) {
            System.out.println("Error in getAllPayments: " + e.getMessage());
            throw new SQLException("Error getting all payments: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }
        
        return payments;
    }
}