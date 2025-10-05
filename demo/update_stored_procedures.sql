-- Script để cập nhật stored procedures từ bảng 'pin' sang 'pinslot'
-- Chạy script này trong SQL Server Management Studio hoặc Azure Data Studio

-- 1. Cập nhật stored procedure UpdatePinPercent
USE [your_database_name] -- Thay thế bằng tên database của bạn
GO

-- Drop procedure cũ nếu tồn tại
IF OBJECT_ID('dbo.UpdatePinPercent', 'P') IS NOT NULL
    DROP PROCEDURE dbo.UpdatePinPercent;
GO

-- Tạo lại procedure với tham chiếu đến bảng pinslot
CREATE PROCEDURE dbo.UpdatePinPercent
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Tăng pinPercent mỗi phút +1% cho đến khi đạt 100%
    UPDATE dbo.pinslot
    SET pinPercent = CASE
                        WHEN pinPercent < 100 THEN pinPercent + 1
                        ELSE 100
                    END,
        pinStatus = CASE
                        WHEN pinPercent + 1 >= 100 THEN 'valiable'
                        ELSE pinStatus
                    END
    WHERE pinStatus = 'unvaliable';
    
    -- Log số lượng records đã update
    DECLARE @RowsAffected INT = @@ROWCOUNT;
    IF @RowsAffected > 0
        PRINT 'UpdatePinPercent - Rows affected: ' + CAST(@RowsAffected AS VARCHAR(10));
END;
GO

-- 2. Cập nhật stored procedure ResetExpiredReservations_Test
-- Drop procedure cũ nếu tồn tại
IF OBJECT_ID('dbo.ResetExpiredReservations_Test', 'P') IS NOT NULL
    DROP PROCEDURE dbo.ResetExpiredReservations_Test;
GO

-- Tạo lại procedure với tham chiếu đến bảng pinslot
CREATE OR ALTER PROCEDURE ResetExpiredReservations_Test
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Reset chỉ những reservations đã quá 1 phút tính từ reserveTime
    UPDATE dbo.pinslot 
    SET reserveStatus = 'ready', 
        reserveTime = NULL 
    WHERE reserveStatus = 'not ready' 
        AND reserveTime IS NOT NULL
        AND DATEDIFF(MINUTE, reserveTime, GETDATE()) >= 1;
    
    -- Log số lượng records đã reset
    DECLARE @RowsAffected INT = @@ROWCOUNT;
    IF @RowsAffected > 0
        PRINT 'Reset expired reservations (Test 1min) - Rows affected: ' + CAST(@RowsAffected AS VARCHAR(10));
END
GO

PRINT 'Tất cả stored procedures đã được cập nhật thành công!';
PRINT 'Các procedures đã được cập nhật:';
PRINT '- UpdatePinPercent: Tăng pinPercent +1% mỗi phút cho đến 100%, cập nhật status thành valiable khi đạt 100%';
PRINT '- ResetExpiredReservations_Test: Reset reservation và reserveTime sau 1 phút kể từ khi đặt lịch';