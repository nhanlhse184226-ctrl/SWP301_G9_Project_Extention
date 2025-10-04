-- Script để cập nhật database từ 'available' thành 'ready'
-- Chạy script này trong SQL Server Management Studio

USE [your_database_name] -- Thay thế bằng tên database của bạn
GO

-- Cập nhật tất cả 'available' thành 'ready'
UPDATE dbo.pinslot 
SET reserveStatus = 'ready' 
WHERE reserveStatus = 'available';

-- Kiểm tra kết quả
SELECT pinID, pinPercent, pinStatus, reserveStatus, reserveTime, stationID 
FROM dbo.pinslot 
ORDER BY pinID;

PRINT 'Database đã được cập nhật:';
PRINT '- Tất cả reserveStatus = ''available'' đã được đổi thành ''ready''';
PRINT 'Hãy chạy lại ứng dụng để test API reserve!';