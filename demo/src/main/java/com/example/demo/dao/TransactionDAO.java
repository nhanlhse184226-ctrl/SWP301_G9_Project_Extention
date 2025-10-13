package com.example.demo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.TransactionDTO;

public class TransactionDAO {

    // Method để lấy tất cả transactions
    public List<TransactionDTO> listTransaction() throws SQLException {
        List<TransactionDTO> listTransaction = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        String sql = "SELECT transactionID, userID, amount, pack, stationID, pinID, status, createAt, expireAt FROM dbo.Transaction";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                rs = ptm.executeQuery();

                while (rs.next()) {
                    int transactionID = rs.getInt("transactionID");
                    int userID = rs.getInt("userID");
                    int amount = rs.getInt("amount");
                    int pack = rs.getInt("pack");
                    int stationID = rs.getInt("stationID");
                    int pinID = rs.getInt("pinID");
                    int status = rs.getInt("status");
                    Date createAt = rs.getTimestamp("createAt");
                    Date expireAt = rs.getTimestamp("expireAt");

                    // Tạo TransactionDTO với constructor đầy đủ
                    TransactionDTO transaction = new TransactionDTO(transactionID, userID, amount, pack, 
                                                                   stationID, pinID, status, createAt, expireAt);
                    listTransaction.add(transaction);
                }

                System.out.println("Retrieved " + listTransaction.size() + " transactions from database");
            }
        } catch (Exception e) {
            System.out.println("Error getting transaction list: " + e.getMessage());
            throw new SQLException("Error getting transaction list: " + e.getMessage());
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ptm != null) {
                ptm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }

        return listTransaction;
    }

    // Method để tạo transaction mới
    public boolean createTransaction(int userID, int amount, int pack, int stationID, int pinID, int status) throws SQLException {
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;

        // SQL với GETDATE() cho createAt và DATEADD để thêm 1 tiếng cho expireAt
        String sql = "INSERT INTO dbo.Transaction (userID, amount, pack, stationID, pinID, status, createAt, expireAt) " +
                    "VALUES (?, ?, ?, ?, ?, ?, GETDATE(), DATEADD(HOUR, 1, GETDATE()))";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                
                // Set parameters
                ptm.setInt(1, userID);
                ptm.setInt(2, amount);
                ptm.setInt(3, pack);
                ptm.setInt(4, stationID);
                ptm.setInt(5, pinID);
                ptm.setInt(6, status);

                int rowsAffected = ptm.executeUpdate();
                success = (rowsAffected > 0);

                System.out.println("Create Transaction - UserID: " + userID + ", Amount: " + amount + 
                                 ", Pack: " + pack + ", StationID: " + stationID + ", PinID: " + pinID + 
                                 ", Status: " + status + ", Rows affected: " + rowsAffected);
            }
        } catch (Exception e) {
            System.out.println("Error creating transaction: " + e.getMessage());
            throw new SQLException("Error creating transaction: " + e.getMessage());
        } finally {
            if (ptm != null) {
                ptm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }

        return success;
    }


    // Method để update status của transaction
    public boolean updateTransactionStatus(int transactionID, int newStatus) throws SQLException {
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;

        String sql = "UPDATE dbo.Transaction SET status = ? WHERE transactionID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, newStatus);
                ptm.setInt(2, transactionID);

                int rowsAffected = ptm.executeUpdate();
                success = (rowsAffected > 0);

                System.out.println("Update Transaction Status - TransactionID: " + transactionID + 
                                 ", New Status: " + newStatus + ", Rows affected: " + rowsAffected);
            }
        } catch (Exception e) {
            System.out.println("Error updating transaction status: " + e.getMessage());
            throw new SQLException("Error updating transaction status: " + e.getMessage());
        } finally {
            if (ptm != null) {
                ptm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }

        return success;
    }

    // Method để lấy transaction theo transactionID
    public TransactionDTO getTransactionById(int transactionID) throws SQLException {
        TransactionDTO transaction = null;
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        String sql = "SELECT transactionID, userID, amount, pack, stationID, pinID, status, createAt, expireAt " +
                    "FROM dbo.Transaction WHERE transactionID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, transactionID);
                rs = ptm.executeQuery();

                if (rs.next()) {
                    int userID = rs.getInt("userID");
                    int amount = rs.getInt("amount");
                    int pack = rs.getInt("pack");
                    int stationID = rs.getInt("stationID");
                    int pinID = rs.getInt("pinID");
                    int status = rs.getInt("status");
                    Date createAt = rs.getTimestamp("createAt");
                    Date expireAt = rs.getTimestamp("expireAt");

                    transaction = new TransactionDTO(transactionID, userID, amount, pack, 
                                                   stationID, pinID, status, createAt, expireAt);
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting transaction by ID: " + e.getMessage());
            throw new SQLException("Error getting transaction by ID: " + e.getMessage());
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ptm != null) {
                ptm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }

        return transaction;
    }

    // Method để chạy stored procedure UpdateExpiredTransactions
    public boolean updateExpiredTransactions() throws SQLException {
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;

        String sql = "EXEC dbo.UpdateExpiredTransactions";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                
                int rowsAffected = ptm.executeUpdate();
                success = true; // Stored procedure executed successfully

                System.out.println("UpdateExpiredTransactions procedure executed successfully. Rows affected: " + rowsAffected);
            }
        } catch (Exception e) {
            System.out.println("Error executing UpdateExpiredTransactions procedure: " + e.getMessage());
            throw new SQLException("Error executing UpdateExpiredTransactions procedure: " + e.getMessage());
        } finally {
            if (ptm != null) {
                ptm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }

        return success;
    }
}