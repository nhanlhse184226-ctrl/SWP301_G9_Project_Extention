-- =====================================================
-- SCRIPT SỬA DATABASE CHO STATUS INT
-- Chạy script này trong SQL Server Management Studio
-- =====================================================

USE [UserManagement] -- Thay bằng tên database của bạn
GO

PRINT '=== BẮTĐẦU SỬA DATABASE SCHEMA ==='

-- 1. SỬA BẢNG pinSlot - Đổi pinStatus và reserveStatus thành INT
PRINT 'Đang sửa cột pinStatus trong bảng pinSlot...'

-- Kiểm tra nếu cột vẫn là VARCHAR
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
           WHERE TABLE_NAME = 'pinSlot' AND COLUMN_NAME = 'pinStatus' 
           AND DATA_TYPE = 'varchar')
BEGIN
    -- Update data trước khi đổi kiểu
    UPDATE dbo.pinSlot 
    SET pinStatus = CASE 
                        WHEN pinStatus = 'valiable' THEN '1'
                        WHEN pinStatus = 'unvaliable' THEN '0'
                        ELSE '0'
                    END
    WHERE pinStatus IN ('valiable', 'unvaliable')

    -- Đổi kiểu cột
    ALTER TABLE dbo.pinSlot 
    ALTER COLUMN pinStatus INT NOT NULL

    PRINT 'Đã đổi pinStatus thành INT (0=unvaliable, 1=valiable)'
END
ELSE
BEGIN
    PRINT 'pinStatus đã là INT'
END

-- 2. XÓA CỘT reserveStatus và reserveTime (nếu tồn tại)
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
           WHERE TABLE_NAME = 'pinSlot' AND COLUMN_NAME = 'reserveStatus')
BEGIN
    ALTER TABLE dbo.pinSlot DROP COLUMN reserveStatus
    PRINT 'Đã xóa cột reserveStatus'
END

IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
           WHERE TABLE_NAME = 'pinSlot' AND COLUMN_NAME = 'reserveTime')
BEGIN
    ALTER TABLE dbo.pinSlot DROP COLUMN reserveTime
    PRINT 'Đã xóa cột reserveTime'
END

-- 3. SỬA BẢNG pinStation - Đổi status thành INT  
PRINT 'Đang sửa cột status trong bảng pinStation...'

IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
           WHERE TABLE_NAME = 'pinStation' AND COLUMN_NAME = 'status' 
           AND DATA_TYPE = 'varchar')
BEGIN
    -- Update data trước khi đổi kiểu
    UPDATE dbo.pinStation 
    SET status = CASE 
                     WHEN status = 'active' THEN '1'
                     WHEN status = 'inactive' THEN '0'  
                     WHEN status = 'maintenance' THEN '2'
                     ELSE '1'
                 END
    WHERE status IN ('active', 'inactive', 'maintenance')

    -- Đổi kiểu cột
    ALTER TABLE dbo.pinStation 
    ALTER COLUMN status INT NOT NULL

    PRINT 'Đã đổi status thành INT (0=inactive, 1=active, 2=maintenance)'
END
ELSE
BEGIN
    PRINT 'status đã là INT'
END

-- 4. SỬA STORED PROCEDURE UpdatePinPercent
PRINT 'Đang sửa stored procedure UpdatePinPercent...'

-- Drop procedure cũ
IF OBJECT_ID('dbo.UpdatePinPercent', 'P') IS NOT NULL
    DROP PROCEDURE dbo.UpdatePinPercent
GO

-- Tạo procedure mới với INT
CREATE PROCEDURE dbo.UpdatePinPercent
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Tăng pinPercent +1% cho pin chưa đầy (pinStatus = 0)
    -- Khi đạt 100% thì đổi thành valiable (pinStatus = 1)
    UPDATE dbo.pinSlot
    SET pinPercent = CASE
                        WHEN pinPercent < 100 THEN pinPercent + 1
                        ELSE 100
                    END,
        pinStatus = CASE
                        WHEN pinPercent + 1 >= 100 THEN 1  -- valiable
                        ELSE 0  -- unvaliable
                    END
    WHERE pinStatus = 0  -- chỉ update pin unvaliable
      AND pinPercent < 100;
    
    DECLARE @RowsAffected INT = @@ROWCOUNT;
    IF @RowsAffected > 0
        PRINT 'UpdatePinPercent - Updated ' + CAST(@RowsAffected AS VARCHAR(10)) + ' slots';
END
GO

-- 5. XÓA STORED PROCEDURE cũ không dùng
IF OBJECT_ID('dbo.ResetExpiredReservations_Test', 'P') IS NOT NULL
BEGIN
    DROP PROCEDURE dbo.ResetExpiredReservations_Test
    PRINT 'Đã xóa stored procedure ResetExpiredReservations_Test'
END

PRINT '=== HOÀN THÀNH SỬA DATABASE ==='
PRINT 'Các thay đổi:'
PRINT '✅ pinSlot.pinStatus: VARCHAR → INT (0=unvaliable, 1=valiable)'
PRINT '✅ pinStation.status: VARCHAR → INT (0=inactive, 1=active, 2=maintenance)'  
PRINT '✅ Xóa pinSlot.reserveStatus và reserveTime'
PRINT '✅ Sửa stored procedure UpdatePinPercent cho INT'
PRINT '✅ Xóa stored procedure ResetExpiredReservations_Test'
PRINT ''
PRINT '🚀 RESTART SPRING BOOT APPLICATION ĐỂ HOÀN TẤT!'