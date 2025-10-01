package com.example.demo.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.UserDTO;

public class UserDAO {
    private static final String LOGIN = "SELECT Name, driverID FROM driver WHERE Email=? AND Password=?";

    public UserDTO checkLogin(String Email, String Password) throws SQLException {
        UserDTO user = null;
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(LOGIN);
                ptm.setString(1, Email);
                ptm.setString(2, Password);
                rs = ptm.executeQuery();
                if (rs.next()) {
                    String Name = rs.getString("Name");
                    int driverID = rs.getInt("driverID");
                    user = new UserDTO(driverID, Name, Email, "***");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        return user;
    }
}
