package com.example.demo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.demo.dbUnits.DBUtils;
import com.example.demo.dto.SubscriptionDTO;

/**
 * DAO quản lý subscription và số dư tài khoản user
 * Xử lý việc lưu trữ, cập nhật và trừ số dư khi user sử dụng dịch vụ
 * 
 * Database table: Subscription
 * - userID: ID của user (FK references users.userID)
 * - total: Số dư hiện tại (số lần sử dụng dịch vụ còn lại)
 * 
 * Workflow:
 * 1. User mua service pack -> gọi updateOrInsertTotal() để cộng thêm số dư
 * 2. User đặt slot -> gọi decrementTotal() để trừ 1 từ số dư
 * 3. User kiểm tra số dư -> gọi getSubscriptionByUserId()
 */
public class SubscriptionDAO {

    /**
     * Lấy thông tin subscription theo userID
     * Trả về số dư hiện tại của user (nếu có)
     * @param userID ID của user cần kiểm tra subscription
     * @return SubscriptionDTO chứa userID và total balance, null nếu chưa có subscription
     * @throws SQLException nếu có lỗi database
     */
    public SubscriptionDTO getSubscriptionByUserId(int userID) throws SQLException {
        // Khởi tạo biến kết quả - mặc định null nếu không tìm thấy
        SubscriptionDTO sub = null;
        
        // Khởi tạo các đối tượng database connection
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        
        try {
            // Mở kết nối đến database thông qua DBUtils
            conn = DBUtils.getConnection();
            
            // Kiểm tra kết nối thành công
            if (conn != null) {
                // SQL query để lấy subscription theo userID
                // Chỉ lấy userID và total (số dư còn lại)
                String sql = "SELECT userID, total FROM Subscription WHERE userID = ?";
                
                // Chuẩn bị prepared statement để tránh SQL injection
                ptm = conn.prepareStatement(sql);
                
                // Gán giá trị userID vào parameter đầu tiên (?)
                ptm.setInt(1, userID);
                
                // Thực thi query và nhận kết quả
                rs = ptm.executeQuery();
                
                // Kiểm tra có record nào được tìm thấy không
                if (rs.next()) {
                    // Lấy giá trị total từ result set
                    int total = rs.getInt("total");
                    
                    // Tạo SubscriptionDTO object với userID và total
                    sub = new SubscriptionDTO(userID, total);
                }
                // Nếu không có rs.next() -> user chưa có subscription -> return null
            }
        } catch (Exception e) {
            // In stack trace để debug lỗi
            e.printStackTrace();
            
            // Ném SQLException với thông báo lỗi chi tiết
            throw new SQLException("Error getting subscription by userID: " + e.getMessage());
        } finally {
            // Đóng tất cả resources theo thứ tự ngược lại
            // Đảm bảo không memory leak
            if (rs != null) rs.close();        // Đóng ResultSet trước
            if (ptm != null) ptm.close();      // Đóng PreparedStatement
            if (conn != null) conn.close();    // Đóng Connection cuối cùng
        }
        
        // Trả về SubscriptionDTO hoặc null
        return sub;
    }

    /**
     * Cập nhật hoặc tạo mới subscription với số dư
     * Được gọi khi user mua service pack thành công
     * @param userID ID của user cần cập nhật subscription
     * @param amount Số lượng cần cộng thêm vào total (từ service pack)
     * @return true nếu cập nhật thành công, false nếu thất bại
     * @throws SQLException nếu có lỗi database
     */
    public boolean updateOrInsertTotal(int userID, int amount) throws SQLException {
        // Biến flag để theo dõi kết quả thực thi
        boolean check = false;
        
        // Khởi tạo các đối tượng database connection
        Connection conn = null;
        PreparedStatement ptm = null;
        
        try {
            // Mở kết nối đến database
            conn = DBUtils.getConnection();
            
            // Kiểm tra kết nối thành công
            if (conn != null) {
                // Kiểm tra user đã có subscription chưa bằng cách gọi method getSubscriptionByUserId
                SubscriptionDTO existing = getSubscriptionByUserId(userID);
                
                // Nếu user đã có subscription -> UPDATE
                if (existing != null) {
                    // SQL để cộng thêm amount vào total hiện tại
                    // Sử dụng "total = total + ?" để cộng dồn, không ghi đè
                    String sql = "UPDATE Subscription SET total = total + ? WHERE userID = ?";
                    
                    // Chuẩn bị prepared statement
                    ptm = conn.prepareStatement(sql);
                    
                    // Gán parameter: amount cần cộng thêm
                    ptm.setInt(1, amount);
                    
                    // Gán parameter: userID để xác định record cần update
                    ptm.setInt(2, userID);
                } else {
                    // Nếu user chưa có subscription -> INSERT record mới
                    // Tạo subscription mới với total = amount
                    String sql = "INSERT INTO Subscription (userID, total) VALUES (?, ?)";
                    
                    // Chuẩn bị prepared statement
                    ptm = conn.prepareStatement(sql);
                    
                    // Gán parameter: userID
                    ptm.setInt(1, userID);
                    
                    // Gán parameter: amount làm total ban đầu
                    ptm.setInt(2, amount);
                }
                
                // Thực thi SQL (UPDATE hoặc INSERT) và kiểm tra kết quả
                // executeUpdate() trả về số rows bị ảnh hưởng
                // > 0 nghĩa là có ít nhất 1 row được cập nhật/insert thành công
                check = ptm.executeUpdate() > 0;
            }
        } catch (Exception e) {
            // In stack trace để debug
            e.printStackTrace();
            
            // Ném SQLException với message chi tiết
            throw new SQLException("Error updating subscription total: " + e.getMessage());
        } finally {
            // Đóng resources để tránh memory leak
            if (ptm != null) ptm.close();      // Đóng PreparedStatement
            if (conn != null) conn.close();    // Đóng Connection
        }
        
        // Trả về kết quả thành công/thất bại
        return check;
    }

    /**
     * Trừ 1 từ total balance của user (MAINFLOW CRITICAL)
     * Được gọi khi user đặt slot hoặc sử dụng dịch vụ
     * Chỉ trừ khi total > 0, tránh số dư âm
     * @param userID ID của user cần trừ balance
     * @return true nếu trừ thành công, false nếu thất bại hoặc total = 0
     * @throws SQLException nếu có lỗi database
     */
    public boolean decrementTotal(int userID) throws SQLException {
        // Biến flag để theo dõi kết quả
        boolean check = false;
        
        // Khởi tạo các đối tượng database connection
        Connection conn = null;
        PreparedStatement ptm = null;
        
        try {
            // Mở kết nối đến database
            conn = DBUtils.getConnection();
            
            // Kiểm tra kết nối thành công
            if (conn != null) {
                // SQL với điều kiện AND total > 0 để đảm bảo không trừ khi hết số dư
                // Chỉ cập nhật nếu user có subscription VÀ total > 0
                // Điều này tránh tình huống total trở thành số âm
                String sql = "UPDATE Subscription SET total = total - 1 WHERE userID = ? AND total > 0";
                
                // Chuẩn bị prepared statement
                ptm = conn.prepareStatement(sql);
                
                // Gán userID vào parameter
                ptm.setInt(1, userID);
                
                // Thực thi UPDATE và kiểm tra kết quả
                // executeUpdate() trả về số rows bị ảnh hưởng
                // > 0 nghĩa là có record được update (user có đủ balance)
                // = 0 nghĩa là không có record nào được update (user hết balance hoặc không tồn tại)
                check = ptm.executeUpdate() > 0;
            }
        } catch (Exception e) {
            // In stack trace để debug
            e.printStackTrace();
            
            // Ném SQLException với message chi tiết
            throw new SQLException("Error decrementing subscription total: " + e.getMessage());
        } finally {
            // Đóng resources để tránh memory leak
            if (ptm != null) ptm.close();      // Đóng PreparedStatement
            if (conn != null) conn.close();    // Đóng Connection
        }
        
        // Trả về true nếu trừ thành công, false nếu không đủ balance hoặc lỗi
        return check;
    }
}