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
        return createPayment(userID, servicePackID, vnp_TxnRef, orderInfo, vnp_Amount, null, null, 0);
    }

    // Create payment record in database (parameter order matching service calls)
    public boolean createPayment(Integer userID, Integer packID, Integer stationID, Integer pinID, String vnp_TxnRef, String orderInfo, Long vnp_Amount, int status) throws SQLException {
        return createPayment(userID, packID, vnp_TxnRef, orderInfo, vnp_Amount, stationID, pinID, status);
    }

    // Create payment record in database (new with stationID, pinID, status)
    public boolean createPayment(Integer userID, Integer packID, String vnp_TxnRef, String orderInfo, Long vnp_Amount, Integer stationID, Integer pinID, int status) throws SQLException {
        String sql = "INSERT INTO dbo.VNPayPaymentDTO (userID, packID, stationID, pinID, vnp_TxnRef, vnp_OrderInfo, vnp_Amount, status, createdAt, expiredAt) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), DATEADD(MINUTE, 15, GETDATE()))";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ptm = conn.prepareStatement(sql)) {
            if (userID != null) ptm.setInt(1, userID); else ptm.setNull(1, java.sql.Types.INTEGER);
            if (packID != null) ptm.setInt(2, packID); else ptm.setNull(2, java.sql.Types.INTEGER);
            if (stationID != null) ptm.setInt(3, stationID); else ptm.setNull(3, java.sql.Types.INTEGER);
            if (pinID != null) ptm.setInt(4, pinID); else ptm.setNull(4, java.sql.Types.INTEGER);
            ptm.setString(5, vnp_TxnRef);
            ptm.setString(6, orderInfo);
            if (vnp_Amount != null) ptm.setLong(7, vnp_Amount); else ptm.setNull(7, java.sql.Types.BIGINT);
            ptm.setInt(8, status);

            int rows = ptm.executeUpdate();
            return rows > 0;
        } catch (ClassNotFoundException e) {
            throw new SQLException("DB driver not found: " + e.getMessage());
        }
    }

    // Retrieve payment by txnRef
    public VNPayPaymentDTO getPaymentByTxnRef(String vnp_TxnRef) throws SQLException {
        String sql = "SELECT TOP 1 paymentID, userID, packID, stationID, pinID, vnp_TxnRef, vnp_OrderInfo, vnp_Amount, status, createdAt, updatedAt, expiredAt, vnp_TransactionNo, vnp_ResponseCode, vnp_TransactionStatus, vnp_PayDate, vnp_BankCode " +
                     "FROM dbo.VNPayPaymentDTO WHERE vnp_TxnRef = ? ORDER BY createdAt DESC";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ptm = conn.prepareStatement(sql)) {
            ptm.setString(1, vnp_TxnRef);
            try (ResultSet rs = ptm.executeQuery()) {
                if (rs.next()) {
                    VNPayPaymentDTO dto = new VNPayPaymentDTO();
                    dto.setPaymentID(rs.getInt("paymentID"));
                    int u = rs.getInt("userID"); if (!rs.wasNull()) dto.setUserID(u);
                    int s = rs.getInt("packID"); if (!rs.wasNull()) dto.setPackID(s);
                    int statn = rs.getInt("stationID"); if (!rs.wasNull()) dto.setStationID(statn);
                    int p = rs.getInt("pinID"); if (!rs.wasNull()) dto.setPinID(p);
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
        String sql = "UPDATE dbo.VNPayPaymentDTO SET status = ?, vnp_TransactionNo = ?, vnp_ResponseCode = ?, vnp_TransactionStatus = ?, vnp_PayDate = ?, vnp_BankCode = ?, updatedAt = GETDATE() WHERE vnp_TxnRef = ?";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ptm = conn.prepareStatement(sql)) {
            ptm.setInt(1, status);
            ptm.setString(2, vnp_TransactionNo);
            ptm.setString(3, vnp_ResponseCode);
            ptm.setString(4, vnp_TransactionStatus);
            ptm.setString(5, vnp_PayDate);
            ptm.setString(6, vnp_BankCode);
            ptm.setString(7, vnp_TxnRef);

            int rows = ptm.executeUpdate();
            return rows > 0;
        } catch (ClassNotFoundException e) {
            throw new SQLException("DB driver not found: " + e.getMessage());
        }
    }

    /**
     * Lấy lịch sử số lần đổi pin theo userID
     * @param userID ID của user
     * @return List<VNPayPaymentDTO> chứa thông tin stationID, pinID, createdAt, bankCode, status
     */
    public List<VNPayPaymentDTO> getPinChangeHistory(Integer userID) throws SQLException {
        List<VNPayPaymentDTO> history = new ArrayList<>();
        String sql = "SELECT stationID, pinID, createdAt, vnp_BankCode, status " +
                     "FROM dbo.VNPayPaymentDTO WHERE userID = ? AND vnp_TransactionNo IS NOT NULL ORDER BY createdAt DESC";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ptm = conn.prepareStatement(sql)) {
            ptm.setInt(1, userID);
            try (ResultSet rs = ptm.executeQuery()) {
                while (rs.next()) {
                    Integer stationID = rs.getInt("stationID");
                    if (rs.wasNull()) stationID = null;
                    
                    Integer pinID = rs.getInt("pinID");
                    if (rs.wasNull()) pinID = null;
                    
                    String createdAt = rs.getString("createdAt");
                    String bankCode = rs.getString("vnp_BankCode");
                    int status = rs.getInt("status");
                    
                    // Sử dụng constructor cho pin history
                    VNPayPaymentDTO dto = new VNPayPaymentDTO(stationID, pinID, createdAt, bankCode, status);
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
        String sql = "SELECT packID, vnp_TxnRef, vnp_OrderInfo, vnp_Amount, vnp_BankCode, status, createdAt " +
                     "FROM dbo.VNPayPaymentDTO WHERE userID = ? AND vnp_TransactionNo IS NOT NULL ORDER BY createdAt DESC";

        try (Connection conn = DBUtils.getConnection(); PreparedStatement ptm = conn.prepareStatement(sql)) {
            ptm.setInt(1, userID);
            try (ResultSet rs = ptm.executeQuery()) {
                while (rs.next()) {
                    Integer packID = rs.getInt("packID");
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
}
