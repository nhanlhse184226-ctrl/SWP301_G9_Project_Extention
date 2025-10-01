/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo.dbUnits;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBUtils for SQL Server connection
 * Giống như code cũ của bạn
 * @author hd
 */
public class DBUtils {
    // Cấu hình SQL Server (giống như code cũ)
    private static final String DB_NAME = "TestSchedule";      // Tên database SQL Server
    private static final String USER_NAME = "sa";               // Username SQL Server
    private static final String PASSWORD = "12345";             // Password SQL Server
    
    /**
     * Tạo kết nối đến SQL Server database
     * @return Connection object
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Connection conn = null;
        
        // Load SQL Server JDBC Driver
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        
        // Tạo SQL Server connection URL
        String url = "jdbc:sqlserver://localhost:1433;databaseName=" + DB_NAME + ";trustServerCertificate=true";
        
        // Tạo connection với SQL Server
        conn = DriverManager.getConnection(url, USER_NAME, PASSWORD);
        
        return conn;
    }
}