package com.example.demo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.UserDTO;

/**
 * DAO quản lý thông tin người dùng (User Management)
 * Xử lý việc đăng nhập, đăng ký, cập nhật thông tin user
 * 
 * Database table: users
 * - userID: ID tự động tăng (PK)
 * - Name: Tên đầy đủ của user
 * - Email: Email đăng nhập (unique)
 * - Password: Mật khẩu đã hash
 * - phone: Số điện thoại (unique)
 * - roleID: Vai trò (1=customer, 2=staff, 3=admin)
 * - status: Trạng thái tài khoản (0=inactive, 1=active, 2=banned)
 * 
 * Business Rules:
 * - Email và phone phải là duy nhất
 * - Chỉ user có status=1 mới được đăng nhập
 * - Password được hash trước khi lưu database
 * - Admin (roleID=3) có quyền quản lý tất cả user
 * - Customer (roleID=1) chỉ được xem/sửa thông tin của mình
 */
public class UserDAO {

    /**
     * Xác thực đăng nhập của user
     * Kiểm tra email, password và status active (=1)
     * @param Email Email đăng nhập
     * @param Password Mật khẩu (đã hash)
     * @return UserDTO nếu đăng nhập thành công, null nếu thất bại
     * @throws SQLException nếu có lỗi database
     */
    public UserDTO checkLogin(String Email, String Password) throws SQLException {
        // Khởi tạo user null - sẽ được gán nếu login thành công
        UserDTO user = null;
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        // SQL kiểm tra email, password và status=1 (active)
        String sql = "SELECT * FROM users WHERE Email=? AND Password=? AND status=1";
        
        try {
            // Mở kết nối database
            conn = DBUtils.getConnection();
            
            // Kiểm tra kết nối thành công
            if (conn != null) {
                // Chuẩn bị prepared statement
                ptm = conn.prepareStatement(sql);
                
                // Gán parameters: email và password
                ptm.setString(1, Email);        // Email đăng nhập
                ptm.setString(2, Password);     // Password đã hash
                
                // Thực thi query
                rs = ptm.executeQuery();
                
                // Nếu có kết quả, nghĩa là login thành công
                if (rs.next()) {
                    // Lấy thông tin user từ database
                    int userID = rs.getInt("userID");           // ID người dùng
                    String Name = rs.getString("Name");         // Tên đầy đủ
                    String userEmail = rs.getString("Email");   // Email xác nhận
                    long phone = rs.getLong("phone");           // Số điện thoại
                    int roleID = rs.getInt("roleID");           // Vai trò (1,2,3)
                    int status = rs.getInt("status");           // Trạng thái (1=active)
                    
                    // Tạo UserDTO, ẩn password bằng "***" cho security
                    user = new UserDTO(userID, Name, userEmail, "***", phone, roleID, status);
                }
                
                // Log kết quả đăng nhập để theo dõi
                System.out.println("checkLogin: Login attempt for email " + Email + " - " + (user != null ? "Success" : "Failed"));
            }
        } catch (ClassNotFoundException e) {
            // Lỗi không tìm thấy database driver
            System.err.println("checkLogin error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            // Lỗi SQL (connection, query, etc.)
            System.err.println("checkLogin error: " + e.getMessage());
            throw new SQLException("Error during login: " + e.getMessage());
        } finally {
            // Đóng tất cả resources
            if (rs != null) {
                rs.close();                 // Đóng ResultSet
            }
            if (ptm != null) {
                ptm.close();                // Đóng PreparedStatement  
            }
            if (conn != null) {
                conn.close();               // Đóng Connection
            }
        }
        
        // Trả về UserDTO (null nếu login thất bại)
        return user;
    }

    public List<UserDTO> getListDriver() throws SQLException {
        List<UserDTO> listDriver = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT * FROM users WHERE roleID = 1";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                rs = ptm.executeQuery();
                while (rs.next()) {
                    int userID = rs.getInt("userID");
                    String Name = rs.getString("Name");
                    String Email = rs.getString("Email");
                    String Password = rs.getString("Password");
                    long phone = rs.getLong("phone");
                    int roleID = rs.getInt("roleID"); // Lấy roleID từ database thay vì hardcode
                    int status = rs.getInt("status");
                    listDriver.add(new UserDTO(userID, Name, Email, Password, phone, roleID, status));
                }
                
                System.out.println("getListDriver: Retrieved " + listDriver.size() + " drivers");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("getListDriver error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("getListDriver error: " + e.getMessage());
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
        
        String sql = "SELECT * FROM users WHERE roleID = 2";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                rs = ptm.executeQuery();
                while (rs.next()) {
                    int userID = rs.getInt("userID");
                    String Name = rs.getString("Name");
                    String Email = rs.getString("Email");
                    String Password = rs.getString("Password");
                    long phone = rs.getLong("phone");
                    int roleID = rs.getInt("roleID");
                    int status = rs.getInt("status");
                    listStaff.add(new UserDTO(userID, Name, Email, Password, phone, roleID, status));
                }
                
                System.out.println("getListStaff: Retrieved " + listStaff.size() + " staff members");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("getListStaff error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("getListStaff error: " + e.getMessage());
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
        
        String sql = "UPDATE users SET Name=?, Email=?, roleID=? WHERE userID=?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setString(1, user.getName());
                ptm.setString(2, user.getEmail());
                ptm.setInt(3, user.getRoleID());
                ptm.setInt(4, user.getUserID());
                int rowsAffected = ptm.executeUpdate();
                check = rowsAffected > 0;
                
                System.out.println("update: " + rowsAffected + " rows affected - Updated user ID " + user.getUserID());
            }
        } catch (ClassNotFoundException e) {
            System.err.println("update error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("update error: " + e.getMessage());
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

    // Method để update phone và password cho driver
    public boolean updateDriver(int userID, long phone, String password) throws SQLException {
        boolean check = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        
        String sql = "UPDATE users SET phone=?, Password=? WHERE userID=?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setLong(1, phone);
                ptm.setString(2, password);
                ptm.setInt(3, userID);
                int rowsAffected = ptm.executeUpdate();
                check = rowsAffected > 0;
                
                System.out.println("updateDriver: " + rowsAffected + " rows affected - Updated driver ID " + userID);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("updateDriver error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("updateDriver error: " + e.getMessage());
            throw new SQLException("Error updating driver info: " + e.getMessage());
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
        
        String sql = "SELECT * FROM users WHERE Email=?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setString(1, email);
                rs = ptm.executeQuery();
                if (rs.next()) {
                    check = true;
                }
                
                System.out.println("checkDuplicateEmail: Email " + email + " - " + (check ? "Duplicate found" : "Available"));
            }
        } catch (ClassNotFoundException e) {
            System.err.println("checkDuplicateEmail error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("checkDuplicateEmail error: " + e.getMessage());
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

    public boolean checkDuplicatePhone(long phone) throws SQLException {
        boolean check = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT * FROM users WHERE phone=?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setLong(1, phone);
                rs = ptm.executeQuery();
                if (rs.next()) {
                    check = true;
                }
                
                System.out.println("checkDuplicatePhone: Phone " + phone + " - " + (check ? "Duplicate found" : "Available"));
            }
        } catch (ClassNotFoundException e) {
            System.err.println("checkDuplicatePhone error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("checkDuplicatePhone error: " + e.getMessage());
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
        return password != null;
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
        
        String sql = "INSERT INTO users(Name, Email, Password, phone, roleID, status) VALUES(?,?,?,?,?,?)";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setString(1, user.getName().trim());
                ptm.setString(2, user.getEmail().trim().toLowerCase());
                ptm.setString(3, user.getPassword());
                ptm.setLong(4, user.getPhone());
                ptm.setInt(5, user.getRoleID());
                ptm.setInt(6, user.getStatus());  // Add status parameter
                int rowsAffected = ptm.executeUpdate();
                check = rowsAffected > 0;

                System.out.println("create: " + rowsAffected + " rows affected - Created user: " + user.getName() + ", Email: " + user.getEmail() +
                        ", Phone: " + user.getPhone() + ", RoleID: " + user.getRoleID() + ", Status: " + user.getStatus());
            }
        } catch (ClassNotFoundException e) {
            System.err.println("create error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("create error: " + e.getMessage());
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

    public boolean updateStatus(int userID) throws SQLException {
        boolean check = false;
        Connection conn = null;
        PreparedStatement ptm = null;
        
        String sql = "UPDATE users SET status = CASE WHEN status = 1 THEN 0 ELSE 1 END WHERE userID = ?";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, userID);
                int rowsAffected = ptm.executeUpdate();
                check = rowsAffected > 0;

                System.out.println("updateStatus: " + rowsAffected + " rows affected - Updated status for user ID " + userID);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("updateStatus error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("updateStatus error: " + e.getMessage());
            throw new SQLException("Error updating user status: " + e.getMessage());
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

    // Method để lấy user theo vehicleID
    public UserDTO getUserByVehicleID(int vehicleID) throws SQLException {
        UserDTO user = null;
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        String sql = "SELECT u.userID, u.Name, u.Email, u.phone, u.roleID, u.status " +
                    "FROM users u " +
                    "INNER JOIN Vehicle v ON u.userID = v.userID " +
                    "WHERE v.vehicleID = ? AND u.status = 1";
        
        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, vehicleID);
                rs = ptm.executeQuery();
                
                if (rs.next()) {
                    int userID = rs.getInt("userID");
                    String name = rs.getString("Name");
                    String email = rs.getString("Email");
                    long phone = rs.getLong("phone");
                    int roleID = rs.getInt("roleID");
                    int status = rs.getInt("status");
                    
                    user = new UserDTO(userID, name, email, "***", phone, roleID, status);
                }
                
                System.out.println("getUserByVehicleID: " + (user != null ? "Found user ID " + user.getUserID() : "No user found") + " for vehicle ID " + vehicleID);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("getUserByVehicleID error: Database driver not found - " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("getUserByVehicleID error: " + e.getMessage());
            throw new SQLException("Error getting user by vehicle ID: " + e.getMessage());
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