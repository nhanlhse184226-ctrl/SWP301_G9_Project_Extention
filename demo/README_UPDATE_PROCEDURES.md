# Hướng dẫn cập nhật stored procedures

## Vấn đề
Database đã đổi tên bảng từ `pin` sang `pinslot`, nhưng các stored procedures vẫn tham chiếu đến bảng `pin` cũ, gây ra lỗi:
```
Invalid object name 'dbo.pin'
```

## Giải pháp
Chạy script SQL `update_stored_procedures.sql` để cập nhật stored procedures.

## Cách chạy script

### 1. Sử dụng SQL Server Management Studio (SSMS)
1. Mở SQL Server Management Studio
2. Kết nối đến SQL Server của bạn
3. Mở file `update_stored_procedures.sql`
4. **Quan trọng**: Sửa dòng `USE [your_database_name]` thành tên database thực tế của bạn
5. Chạy script bằng cách nhấn F5 hoặc Execute

### 2. Sử dụng Azure Data Studio
1. Mở Azure Data Studio
2. Kết nối đến SQL Server của bạn
3. Mở file `update_stored_procedures.sql`
4. **Quan trọng**: Sửa dòng `USE [your_database_name]` thành tên database thực tế của bạn
5. Chạy script

### 3. Sử dụng command line (sqlcmd)
```bash
sqlcmd -S server_name -d database_name -i update_stored_procedures.sql
```

## Stored procedures được cập nhật

1. **UpdatePinPercent**
   - Cũ: Tham chiếu đến `dbo.pin`
   - Mới: Tham chiếu đến `dbo.pinslot`
   - Chức năng: **Tăng `pinPercent` +1% mỗi phút** cho các pin có `pinStatus = 'unvaliable'` cho đến khi đạt 100%, sau đó cập nhật `pinStatus` thành 'valiable'
   - Logic: Mô phỏng quá trình sạc pin tự động

2. **ResetExpiredReservations_Test**
   - Cũ: Tham chiếu đến `dbo.pin`
   - Mới: Tham chiếu đến `dbo.pinslot`
   - Chức năng: **Reset reservation sau 1 phút** kể từ khi đặt lịch (`reserveTime`)
   - Logic: Tự động giải phóng slot nếu user không sử dụng trong 1 phút

## Sau khi chạy script
1. Khởi động lại ứng dụng Spring Boot
2. Kiểm tra logs - không còn lỗi "Invalid object name 'dbo.pin'"
3. Test các chức năng:
   - Update pin percentage
   - Reserve pin slot
   - Scheduled tasks

## Lưu ý
- Backup database trước khi chạy script
- Đảm bảo bạn có quyền CREATE/DROP PROCEDURE
- Script sẽ tự động drop procedures cũ và tạo lại