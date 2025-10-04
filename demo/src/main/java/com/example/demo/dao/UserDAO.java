package com.example.demo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.UserDTO;

public class UserDAO {
    private static final String LOGIN = "SELECT * FROM users WHERE Email=? AND Password=?";
    private static final String LIST_DRIVER = "SELECT * FROM users WHERE roleID = 1"; 
    private static final String LIST_STAFF = "SELECT * FROM users WHERE roleID = 2"; 
    private static final String UPDATE = "UPDATE users SET Name=?, Email=?, Password=?, phone=?, roleID=? WHERE userID=?";
    private static final String DELETE = "DELETE FROM users WHERE userID=?";
    private static final String DUPLICATE_EMAIL = "SELECT * FROM users WHERE Email=?";
    private static final String DUPLICATE_PHONE = "SELECT * FROM users WHERE phone=?";
    private static final String CREATE = "INSERT INTO users(Name, Email, Password, phone, roleID) VALUES(?,?,?,?,?)"; 

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
                    int userID = rs.getInt("userID");
                    String Name = rs.getString("Name");
                    String userEmail = rs.getString("Email");
                    int phone = rs.getInt("phone");
                    int roleID = rs.getInt("roleID");
                    user = new UserDTO(userID, Name, userEmail, "***", phone, roleID);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error during login: " + e.getMessage());
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

    public List<UserDTO> getListDriver() throws SQLException {
        List<UserDTO> listDriver = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(LIST_DRIVER);
                rs = ptm.executeQuery();
                while (rs.next()) {
                    int userID = rs.getInt("userID"); // Đổi từ driverID thành userID
                    String Name = rs.getString("Name");
                    String Email = rs.getString("Email");
                    String Password = rs.getString("Password");
                    int phone = rs.getInt("phone");
                    int roleID = rs.getInt("roleID"); // Lấy roleID từ database thay vì hardcode
                    listDriver.add(new UserDTO(userID, Name, Email, Password, phone, roleID));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error getting driver list: " + e.getMessage());
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
        return listDriver;
    }

    public List<UserDTO> getListStaff() throws SQLException {
        List<UserDTO> listStaff = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(LIST_STAFF);
                rs = ptm.executeQuery();
                while (rs.next()) {
                    int userID = rs.getInt("userID");
                    String Name = rs.getString("Name");
                    String Email = rs.getString("Email");
                    String Password = rs.getString("Password");
                    int phone = rs.getInt("phone");
                    int roleID = rs.getInt("roleID");
                    listStaff.add(new UserDTO(userID, Name, Email, Password, phone, roleID));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error getting staff list: " + e.getMessage());
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
        return listStaff;
    }

    public boolean update(UserDTO user) throws SQLException {
        boolean check = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(UPDATE);
                ptm.setString(1, user.getName());
                ptm.setString(2, user.getEmail());
                ptm.setString(3, user.getPassword());
                ptm.setInt(4, user.getPhone());
                ptm.setInt(5, user.getRoleID());
                ptm.setInt(6, user.getUserID());
                check = ptm.executeUpdate() > 0 ? true : false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error updating user: " + e.getMessage());
        } finally {
            if (ptm != null) {
                ptm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return check;
    }

    public boolean delete(UserDTO user) throws SQLException {
        boolean check = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(DELETE);
                ptm.setInt(1, user.getUserID());
                check = ptm.executeUpdate() > 0 ? true : false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error deleting user: " + e.getMessage());
        } finally {
            if (ptm != null) {
                ptm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return check;
    }

    public boolean checkDuplicateEmail(String email) throws SQLException {
        boolean check = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(DUPLICATE_EMAIL);
                ptm.setString(1, email);
                rs = ptm.executeQuery();
                if (rs.next()) {
                    check = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error checking duplicate email: " + e.getMessage());
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
        return check;
    }

    public boolean checkDuplicatePhone(int phone) throws SQLException {
        boolean check = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(DUPLICATE_PHONE);
                ptm.setInt(1, phone);
                rs = ptm.executeQuery();
                if (rs.next()) {
                    check = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error checking duplicate phone: " + e.getMessage());
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
        return check;
    }

    // Kiểm tra email format
    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    // Kiểm tra password strength
    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public boolean create(UserDTO user) throws SQLException {
        // Enhanced validation
        if (user == null) {
            throw new SQLException("User data cannot be null");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new SQLException("Name cannot be null or empty");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new SQLException("Email cannot be null or empty");
        }
        if (!isValidEmail(user.getEmail())) {
            throw new SQLException("Invalid email format");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new SQLException("Password cannot be null or empty");
        }
        if (!isValidPassword(user.getPassword())) {
            throw new SQLException("Password must be at least 6 characters long");
        }
        if (user.getPhone() <= 0) {
            throw new SQLException("Invalid phone number");
        }
        if (user.getRoleID() <= 0) {
            throw new SQLException("Invalid role ID");
        }
        
        // Kiểm tra duplicate email
        if (checkDuplicateEmail(user.getEmail())) {
            throw new SQLException("Email '" + user.getEmail() + "' already exists");
        }
        
        // Kiểm tra duplicate phone
        if (checkDuplicatePhone(user.getPhone())) {
            throw new SQLException("Phone number '" + user.getPhone() + "' already exists");
        }
        
        boolean check = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(CREATE);
                ptm.setString(1, user.getName().trim());
                ptm.setString(2, user.getEmail().trim().toLowerCase());
                ptm.setString(3, user.getPassword());
                ptm.setInt(4, user.getPhone());
                ptm.setInt(5, user.getRoleID());
                check = ptm.executeUpdate() > 0 ? true : false;
                
                System.out.println("Create User - Name: " + user.getName() + ", Email: " + user.getEmail() + 
                                 ", Phone: " + user.getPhone() + ", RoleID: " + user.getRoleID());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error creating user: " + e.getMessage());
        } finally {
            if (ptm != null) {
                ptm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return check;
    }

    
}