-- Script để sửa stored procedures cho pinStatus và reserveStatus thành INT
-- Chạy script này trong SQL Server Management Studio hoặc Azure Data Studio

-- 1. Sửa stored procedure UpdatePinPercent cho pinStatus INT
USE [UserManagement] -- Thay thế bằng tên database của bạn
GO

-- Drop procedure cũ nếu tồn tại
IF OBJECT_ID('dbo.UpdatePinPercent', 'P') IS NOT NULL
    DROP PROCEDURE dbo.UpdatePinPercent;
GO

-- Tạo lại procedure với pinStatus INT
CREATE PROCEDURE dbo.UpdatePinPercent
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Tăng pinPercent mỗi phút +1% cho đến khi đạt 100%
    -- pinStatus: 0 = unvaliable, 1 = valiable
    UPDATE dbo.pinSlot
    SET pinPercent = CASE
                        WHEN pinPercent < 100 THEN pinPercent + 1
                        ELSE 100
                    END,
        pinStatus = CASE
                        WHEN pinPercent + 1 >= 100 THEN 1  -- valiable
                        ELSE pinStatus
                    END
    WHERE pinStatus = 0;  -- unvaliable
    
    -- Log số lượng records đã update
    DECLARE @RowsAffected INT = @@ROWCOUNT;
    IF @RowsAffected > 0
        PRINT 'UpdatePinPercent - Rows affected: ' + CAST(@RowsAffected AS VARCHAR(10));
END;
GO

-- 2. Xóa stored procedure ResetExpiredReservations_Test vì không còn dùng reservation
IF OBJECT_ID('dbo.ResetExpiredReservations_Test', 'P') IS NOT NULL
    DROP PROCEDURE dbo.ResetExpiredReservations_Test;
GO

PRINT 'Stored procedures đã được sửa thành công!';
PRINT 'Thay đổi:';
PRINT '- UpdatePinPercent: pinStatus từ VARCHAR thành INT (0=unvaliable, 1=valiable)';
PRINT '- ResetExpiredReservations_Test: Đã xóa vì không còn dùng reserveStatus/reserveTime';