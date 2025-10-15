package com.example.demo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.VNPayPaymentDTO;

public class VNPayPaymentDAO {

    // Create payment record in database (legacy, no station/pin)
    public boolean createPayment(Integer userID, Integer servicePackID, String vnp_TxnRef, String orderInfo, Long vnp_Amount) throws SQLException {
        return createPayment(userID, servicePackID, vnp_TxnRef, orderInfo, vnp_Amount, 0, null);
    }

    // Create payment record (simplified method)
    public boolean createPayment(Integer userID, Integer packID, String vnp_TxnRef, String orderInfo, Long vnp_Amount, int status) throws SQLException {
        return createPayment(userID, packID, vnp_TxnRef, orderInfo, vnp_Amount, status, null);
    }

    // Create payment record with total (main implementation)
    public boolean createPayment(Integer userID, Integer packID, String vnp_TxnRef, String orderInfo, Long vnp_Amount, int status, Integer total) throws SQLException {
        String sql = "INSERT INTO dbo.VNPayPaymentDTO (userID, PackID, total, vnp_TxnRef, vnp_OrderInfo, vnp_Amount, status, createdAt, expiredAt) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE(), DATEADD(MINUTE, 15, GETDATE()))";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ptm = conn.prepareStatement(sql)) {
            if (userID != null) ptm.setInt(1, userID); else ptm.setNull(1, java.sql.Types.INTEGER);
            if (packID != null) ptm.setInt(2, packID); else ptm.setNull(2, java.sql.Types.INTEGER);
            if (total != null) ptm.setInt(3, total); else ptm.setNull(3, java.sql.Types.INTEGER);
            ptm.setString(4, vnp_TxnRef);
            ptm.setString(5, orderInfo);
            if (vnp_Amount != null) ptm.setLong(6, vnp_Amount); else ptm.setNull(6, java.sql.Types.BIGINT);
            ptm.setInt(7, status);

            int rows = ptm.executeUpdate();
            return rows > 0;
        } catch (ClassNotFoundException e) {
            throw new SQLException("DB driver not found: " + e.getMessage());
        }
    }

    // Retrieve payment by txnRef
    public VNPayPaymentDTO getPaymentByTxnRef(String vnp_TxnRef) throws SQLException {
        String sql = "SELECT TOP 1 paymentID, userID, packID, total, vnp_TxnRef, vnp_OrderInfo, vnp_Amount, status, createdAt, updatedAt, expiredAt, vnp_TransactionNo, vnp_ResponseCode, vnp_TransactionStatus, vnp_PayDate, vnp_BankCode " +
                     "FROM dbo.VNPayPaymentDTO WHERE vnp_TxnRef = ? ORDER BY createdAt DESC";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ptm = conn.prepareStatement(sql)) {
            ptm.setString(1, vnp_TxnRef);
            try (ResultSet rs = ptm.executeQuery()) {
                if (rs.next()) {
                    VNPayPaymentDTO dto = new VNPayPaymentDTO();
                    dto.setPaymentID(rs.getInt("paymentID"));
                    int u = rs.getInt("userID"); if (!rs.wasNull()) dto.setUserID(u);
                    int s = rs.getInt("packID"); if (!rs.wasNull()) dto.setPackID(s);
                    int t = rs.getInt("total"); if (!rs.wasNull()) dto.setTotal(t);
                    dto.setVnp_TxnRef(rs.getString("vnp_TxnRef"));
                    dto.setVnp_OrderInfo(rs.getString("vnp_OrderInfo"));
                    long amt = rs.getLong("vnp_Amount"); if (!rs.wasNull()) dto.setVnp_Amount(amt);
                    int st = rs.getInt("status"); dto.setStatus(st);
                    dto.setCreatedAt(rs.getString("createdAt"));
                    dto.setUpdatedAt(rs.getString("updatedAt"));
                    dto.setExpiredAt(rs.getString("expiredAt"));
                    dto.setVnp_TransactionNo(rs.getString("vnp_TransactionNo"));
                    dto.setVnp_ResponseCode(rs.getString("vnp_ResponseCode"));
                    dto.setVnp_TransactionStatus(rs.getString("vnp_TransactionStatus"));
                    dto.setVnp_PayDate(rs.getString("vnp_PayDate"));
                    dto.setVnp_BankCode(rs.getString("vnp_BankCode"));
                    return dto;
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("DB driver not found: " + e.getMessage());
        }

        return null;
    }

    // Update payment status when payment is completed/failed
    public boolean updatePaymentStatus(String vnp_TxnRef, int status, String vnp_TransactionNo, String vnp_ResponseCode, String vnp_TransactionStatus, String vnp_PayDate, String vnp_BankCode) throws SQLException {
        System.out.println("=== VNPayPaymentDAO.updatePaymentStatus START ===");
        System.out.println("Parameters: txnRef=" + vnp_TxnRef + ", status=" + status + ", transactionNo=" + vnp_TransactionNo + ", responseCode=" + vnp_ResponseCode);
        
        String sql = "UPDATE dbo.VNPayPaymentDTO SET status = ?, vnp_TransactionNo = ?, vnp_ResponseCode = ?, vnp_TransactionStatus = ?, vnp_PayDate = ?, vnp_BankCode = ?, updatedAt = GETDATE() WHERE vnp_TxnRef = ?";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ptm = conn.prepareStatement(sql)) {
            ptm.setInt(1, status);
            ptm.setString(2, vnp_TransactionNo);
            ptm.setString(3, vnp_ResponseCode);
            ptm.setString(4, vnp_TransactionStatus);
            ptm.setString(5, vnp_PayDate);
            ptm.setString(6, vnp_BankCode);
            ptm.setString(7, vnp_TxnRef);

            System.out.println("Executing SQL: " + sql);
            int rows = ptm.executeUpdate();
            System.out.println("Updated VNPayPayment status: txnRef=" + vnp_TxnRef + ", status=" + status + ", rows=" + rows);
            System.out.println("=== VNPayPaymentDAO.updatePaymentStatus END ===");
            return rows > 0;
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: DB driver not found: " + e.getMessage());
            throw new SQLException("DB driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("ERROR: SQL Exception in updatePaymentStatus: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Lấy lịch sử giao dịch theo userID
     * @param userID ID của user
     * @return List<VNPayPaymentDTO> chứa thông tin createdAt, bankCode, status
     */
    public List<VNPayPaymentDTO> getPinChangeHistory(Integer userID) throws SQLException {
        List<VNPayPaymentDTO> history = new ArrayList<>();
        String sql = "SELECT createdAt, vnp_BankCode, status " +
                     "FROM dbo.VNPayPaymentDTO WHERE userID = ? ORDER BY createdAt DESC";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ptm = conn.prepareStatement(sql)) {
            ptm.setInt(1, userID);
            try (ResultSet rs = ptm.executeQuery()) {
                while (rs.next()) {
                    String createdAt = rs.getString("createdAt");
                    String bankCode = rs.getString("vnp_BankCode");
                    int status = rs.getInt("status");
                    
                    // Sử dụng constructor mới cho transaction history (không có stationID/pinID)
                    VNPayPaymentDTO dto = new VNPayPaymentDTO(createdAt, bankCode, status);
                    history.add(dto);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("DB driver not found: " + e.getMessage());
        }
        
        return history;
    }

    /**
     * Lấy lịch sử thanh toán theo userID
     * @param userID ID của user
     * @return List<VNPayPaymentDTO> chứa thông tin packID, txnRef, orderInfo, amount, bankCode, status, createdAt
     */
    public List<VNPayPaymentDTO> getPaymentHistory(Integer userID) throws SQLException {
        List<VNPayPaymentDTO> history = new ArrayList<>();
        String sql = "SELECT PackID, vnp_TxnRef, vnp_OrderInfo, vnp_Amount, vnp_BankCode, status, createdAt " +
                     "FROM dbo.VNPayPaymentDTO WHERE userID = ? ORDER BY createdAt DESC";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ptm = conn.prepareStatement(sql)) {
            ptm.setInt(1, userID);
            try (ResultSet rs = ptm.executeQuery()) {
                while (rs.next()) {
                    Integer packID = rs.getInt("PackID");
                    if (rs.wasNull()) packID = null;
                    
                    String txnRef = rs.getString("vnp_TxnRef");
                    String orderInfo = rs.getString("vnp_OrderInfo");
                    long amount = rs.getLong("vnp_Amount");
                    String bankCode = rs.getString("vnp_BankCode");
                    int status = rs.getInt("status");
                    String createdAt = rs.getString("createdAt");
                    
                    // Sử dụng constructor cho payment history
                    VNPayPaymentDTO dto = new VNPayPaymentDTO(packID, txnRef, orderInfo, amount, bankCode, status, createdAt);
                    history.add(dto);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("DB driver not found: " + e.getMessage());
        }
        
        return history;
    }

    /**
     * Debug method: Count total payments
     */
    public int countAllPayments() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM dbo.VNPayPaymentDTO";
        
        try (Connection conn = DBUtils.getConnection(); PreparedStatement ptm = conn.prepareStatement(sql)) {
            try (ResultSet rs = ptm.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("total");
                    System.out.println("Total payments in DB: " + count);
                    return count;
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("DB driver not found: " + e.getMessage());
        }
        
        return 0;
    }
}
