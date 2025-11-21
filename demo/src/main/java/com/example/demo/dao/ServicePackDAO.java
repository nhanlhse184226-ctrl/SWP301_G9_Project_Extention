package com.example.demo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.ServicePackDTO;

/**
 * DAO quản lý các gói dịch vụ (Service Packages)
 * Xử lý việc tạo, cập nhật và lấy danh sách các gói nạp tiền cho user
 * 
 * Database table: ServicePack
 * - packID: ID tự động tăng (PK)
 * - packName: Tên gói dịch vụ (unique)
 * - status: Trạng thái (0=inactive, 1=active)
 * - description: Mô tả gói (optional)
 * - total: Tổng số lượng/dung lượng gói
 * - price: Giá tiền (VND)
 * - createDate: Ngày tạo
 * 
 * Business Rules:
 * - Chỉ admin (roleID=3) được phép CRUD service packs
 * - Tên gói không được trùng lặp
 * - Giá và total phải >= 0
 * - Tất cả user có thể xem danh sách gói
 */
public class ServicePackDAO {

    /**
     * Kiểm tra user có phải admin không (roleID = 3)
     * Sử dụng để validate quyền trước khi thực hiện các thao tác CRUD
     * @param userID ID của user cần kiểm tra
     * @return true nếu user là admin (roleID=3), false nếu không
     * @throws SQLException nếu có lỗi database
     */
    // Method để check admin role (roleID = 3)
    private boolean isAdminUser(int userID) throws SQLException {
        // Khởi tạo các đối tượng database connection
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        // SQL lấy roleID từ bảng users
        String sql = "SELECT roleID FROM users WHERE userID = ?";

        try {
            // Mở kết nối database
            conn = DBUtils.getConnection();
            
            // Kiểm tra kết nối thành công
            if (conn != null) {
                // Chuẩn bị prepared statement
                ptm = conn.prepareStatement(sql);
                
                // Gán userID vào parameter
                ptm.setInt(1, userID);
                
                // Thực thi query
                rs = ptm.executeQuery();

                // Kiểm tra có kết quả (user tồn tại)
                if (rs.next()) {
                    // Lấy roleID từ database
                    int roleID = rs.getInt("roleID");
                    
                    // Kiểm tra roleID = 3 (Admin)
                    return roleID == 3; // Admin role
                }
            }
        } catch (ClassNotFoundException e) {
            // Lỗi không tìm thấy database driver
            throw new SQLException("Database driver not found: " + e.getMessage());
        } finally {
            // Đóng tất cả resources
            if (rs != null) rs.close();        // Đóng ResultSet
            if (ptm != null) ptm.close();      // Đóng PreparedStatement
            if (conn != null) conn.close();    // Đóng Connection
        }

        // Trả về false nếu user không tồn tại hoặc không phải admin
        return false; // User không tồn tại hoặc không phải admin
    }


    /**
     * Kiểm tra tên gói dịch vụ đã tồn tại chưa
     * Đảm bảo tên gói là duy nhất trong hệ thống
     * @param packName Tên gói cần kiểm tra
     * @return true nếu tên đã tồn tại, false nếu chưa
     * @throws SQLException nếu có lỗi database
     */
    // Method để check duplicate pack name
    public boolean isPackNameExists(String packName) throws SQLException {
        // Khởi tạo các đối tượng database connection
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        // SQL tìm kiếm packName trong bảng ServicePack
        String sql = "SELECT packName FROM ServicePack WHERE packName = ?";

        try {
            // Mở kết nối database
            conn = DBUtils.getConnection();
            
            // Kiểm tra kết nối thành công
            if (conn != null) {
                // Chuẩn bị prepared statement
                ptm = conn.prepareStatement(sql);
                
                // Gán packName vào parameter (case-sensitive)
                ptm.setString(1, packName);
                
                // Thực thi query
                rs = ptm.executeQuery();

                // rs.next() trả về true nếu có ít nhất 1 record
                // Nghĩa là packName đã tồn tại trong database
                return rs.next(); // Trả về true nếu có record, false nếu không
            }
        } catch (ClassNotFoundException e) {
            // Lỗi không tìm thấy database driver
            throw new SQLException("Database driver not found: " + e.getMessage());
        } finally {
            // Đóng tất cả resources
            if (rs != null) rs.close();        // Đóng ResultSet
            if (ptm != null) ptm.close();      // Đóng PreparedStatement
            if (conn != null) conn.close();    // Đóng Connection
        }

        // Trả về false nếu không tìm thấy (packName chưa tồn tại)
        return false;
    }

    /**
     * Tạo gói dịch vụ mới trong hệ thống
     * Chỉ admin (roleID=3) mới được phép thực hiện thao tác này
     * @param servicePack Thông tin gói dịch vụ cần tạo
     * @param adminUserID ID của admin thực hiện tạo
     * @return true nếu tạo thành công, false nếu thất bại
     * @throws SQLException nếu có lỗi validation hoặc database
     */
    // Method để tạo ServicePack mới (chỉ admin)
    public boolean createServicePack(ServicePackDTO servicePack, int adminUserID) throws SQLException {
        // Kiểm tra quyền admin trước khi thực hiện
        // Validate admin permission
        if (!isAdminUser(adminUserID)) {
            throw new SQLException("Access denied. Only admin users (roleID=3) can create service packs");
        }

        // Validate dữ liệu đầu vào
        // Kiểm tra tên gói không null và không rỗng
        if (servicePack.getPackName() == null || servicePack.getPackName().trim().isEmpty()) {
            throw new SQLException("Pack name cannot be null or empty");
        }

        // Kiểm tra status hợp lệ (0 hoặc 1)
        if (servicePack.getStatus() < 0 || servicePack.getStatus() > 1) {
            throw new SQLException("Status must be 0 (inactive) or 1 (active)");
        }

        // Kiểm tra giá và total phải >= 0
        if (servicePack.getPrice() < 0 || servicePack.getTotal() < 0) {
            throw new SQLException("Price and total must be non-negative");
        }

        // Kiểm tra tên gói không trùng lặp
        // Check duplicate pack name
        if (isPackNameExists(servicePack.getPackName().trim())) {
            throw new SQLException("Service pack name '" + servicePack.getPackName() + "' already exists");
        }

        // Biến theo dõi kết quả thao tác
        boolean success = false;
        Connection conn = null;
        PreparedStatement ptm = null;

        // SQL INSERT với GETDATE() tự động tạo createDate
        String sql = "INSERT INTO ServicePack (packName, status, description, total, price, createDate) VALUES (?, ?, ?, ?, ?, GETDATE())";

        try {
            // Mở kết nối database
            conn = DBUtils.getConnection();
            
            // Kiểm tra kết nối thành công
            if (conn != null) {
                // Chuẩn bị prepared statement
                ptm = conn.prepareStatement(sql);
                
                // Gán các parameter từ ServicePackDTO
                ptm.setString(1, servicePack.getPackName().trim());  // Trim để loại bỏ khoảng trắng
                ptm.setInt(2, servicePack.getStatus());              // 0=inactive, 1=active
                ptm.setString(3, servicePack.getDescription());      // Có thể null
                ptm.setInt(4, servicePack.getTotal());               // Tổng dung lượng/số lượng
                ptm.setInt(5, servicePack.getPrice());               // Giá tiền (VND)

                // Thực thi INSERT command và lấy số dòng bị ảnh hưởng
                int rowsAffected = ptm.executeUpdate();
                
                // Nếu có ít nhất 1 dòng được thêm vào thì thành công
                success = (rowsAffected > 0);

                // Log thông tin để theo dõi (development/debugging)
                System.out.println("Create ServicePack - Name: " + servicePack.getPackName() + 
                                 ", Status: " + servicePack.getStatus() + 
                                 ", Price: " + servicePack.getPrice() + 
                                 ", Rows affected: " + rowsAffected);
            }
        } catch (ClassNotFoundException e) {
            // Lỗi không tìm thấy database driver
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            // Lỗi SQL (constraint violation, syntax error, etc.)
            System.out.println("Database error in createServicePack: " + e.getMessage());
            throw e; // Re-throw để caller xử lý
        } catch (Exception e) {
            // Lỗi không mong đợi khác
            System.out.println("Unexpected error in createServicePack: " + e.getMessage());
            throw new SQLException("Failed to create service pack: " + e.getMessage());
        } finally {
            // Đóng tất cả resources
            if (ptm != null) ptm.close();      // Đóng PreparedStatement
            if (conn != null) conn.close();    // Đóng Connection
        }

        // Trả về kết quả thao tác (true=thành công, false=thất bại)
        return success;
    }

    // Method để update ServicePack (chỉ admin) - Update toàn bộ thông tin
    public boolean updateServicePack(int packID, ServicePackDTO servicePack, int adminUserID) throws SQLException {
        // Validate admin permission
        if (!isAdminUser(adminUserID)) {
            throw new SQLException("Access denied. Only admin users (roleID=3) can update service packs");
        }

        // Validate input
        if (servicePack.getPackName() == null || servicePack.getPackName().trim().isEmpty()) {
            throw new SQLException("Pack name cannot be null or empty");
        }

        if (servicePack.getStatus() < 0 || servicePack.getStatus() > 1) {
            throw new SQLException("Status must be 0 (inactive) or 1 (active)");
        }

        if (servicePack.getPrice() < 0 || servicePack.getTotal() < 0) {
            throw new SQLException("Price and total must be non-negative");
        }

        Connection conn = null;
        PreparedStatement ptm = null;

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {

                // Perform update
                String updateSql = "UPDATE ServicePack SET packName = ?, description = ?, total = ?, price = ? WHERE packID = ?";
                ptm = conn.prepareStatement(updateSql);
                ptm.setString(1, servicePack.getPackName().trim());
                ptm.setString(2, servicePack.getDescription());
                ptm.setInt(3, servicePack.getTotal());
                ptm.setInt(4, servicePack.getPrice());
                ptm.setInt(5, packID);

                int rowsAffected = ptm.executeUpdate();
                boolean success = (rowsAffected > 0);

                System.out.println("Update ServicePack - ID: " + packID + 
                                 ", Name: " + servicePack.getPackName() + 
                                 ", Price: " + servicePack.getPrice() + 
                                 ", Rows affected: " + rowsAffected);

                return success;
            }
        } catch (Exception e) {
            System.out.println("Unexpected error in updateServicePack: " + e.getMessage());
            throw new SQLException("Failed to update service pack: " + e.getMessage());
        } finally {
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }

        return false;
    }

    // Method để update chỉ status của ServicePack (chỉ admin)
    public boolean updateServicePackStatus(int packID, int status, int adminUserID) throws SQLException {
        // Validate admin permission
        if (!isAdminUser(adminUserID)) {
            throw new SQLException("Access denied. Only admin users (roleID=3) can update service pack status");
        }

        // Validate status
        if (status < 0 || status > 1) {
            throw new SQLException("Status must be 0 (inactive) or 1 (active)");
        }

        Connection conn = null;
        PreparedStatement ptm = null;

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                String updateSql = "UPDATE ServicePack SET status = ? WHERE packID = ?";
                ptm = conn.prepareStatement(updateSql);
                ptm.setInt(1, status);
                ptm.setInt(2, packID);

                int rowsAffected = ptm.executeUpdate();
                boolean success = (rowsAffected > 0);

                String statusText = (status == 0) ? "inactive" : "active";
                System.out.println("Update ServicePack Status - ID: " + packID + 
                                 ", New Status: " + status + " (" + statusText + ")" +
                                 ", Rows affected: " + rowsAffected);

                return success;
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error in updateServicePackStatus: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error in updateServicePackStatus: " + e.getMessage());
            throw new SQLException("Failed to update service pack status: " + e.getMessage());
        } finally {
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }

        return false;
    }

    // Method để lấy ServicePack theo ID (để verify sau khi create/update)
    public ServicePackDTO getServicePackById(int packID) throws SQLException {
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        String sql = "SELECT packID, packName, status, description, total, price, createDate FROM ServicePack WHERE packID = ?";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setInt(1, packID);
                rs = ptm.executeQuery();

                if (rs.next()) {
                    return new ServicePackDTO(
                        rs.getInt("packID"),
                        rs.getString("packName"),
                        rs.getInt("status"),
                        rs.getString("description"),
                        rs.getInt("total"),
                        rs.getInt("price"),
                        rs.getTimestamp("createDate")
                    );
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }

        return null;
    }

    // Method để lấy danh sách tất cả ServicePack
    public List<ServicePackDTO> getListServicePack() throws SQLException {
        List<ServicePackDTO> listServicePack = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;

        String sql = "SELECT packID, packName, status, description, total, price, createDate FROM ServicePack ORDER BY createDate DESC";

        try {
            conn = DBUtils.getConnection();
            if (conn != null) {
                ptm = conn.prepareStatement(sql);
                rs = ptm.executeQuery();

                while (rs.next()) {
                    ServicePackDTO servicePack = new ServicePackDTO(
                        rs.getInt("packID"),
                        rs.getString("packName"),
                        rs.getInt("status"),
                        rs.getString("description"),
                        rs.getInt("total"),
                        rs.getInt("price"),
                        rs.getTimestamp("createDate")
                    );
                    listServicePack.add(servicePack);
                }

                System.out.println("Retrieved " + listServicePack.size() + " service packs from database");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ptm != null) ptm.close();
            if (conn != null) conn.close();
        }

        return listServicePack;
    }
}