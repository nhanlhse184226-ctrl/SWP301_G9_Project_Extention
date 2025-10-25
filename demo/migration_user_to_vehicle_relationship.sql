-- Migration Script: Chuyển đổi từ 1 user - 1 xe sang 1 user - N xe
-- Thay đổi bảng Transaction và PinSlot từ userID sang vehicleID
-- Ngày: 25/10/2025

-- ==============================================
-- PHẦN 1: BACKUP DỮ LIỆU TRƯỚC KHI MIGRATION
-- ==============================================

-- Tạo bảng backup cho Transaction
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Transaction_Backup_Before_Migration' AND xtype='U')
BEGIN
    SELECT * INTO Transaction_Backup_Before_Migration 
    FROM [TestSchedule].[dbo].[Transaction];
    PRINT 'Đã tạo backup bảng Transaction_Backup_Before_Migration';
END

-- Tạo bảng backup cho PinSlot  
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='PinSlot_Backup_Before_Migration' AND xtype='U')
BEGIN
    SELECT * INTO PinSlot_Backup_Before_Migration 
    FROM dbo.pinSlot;
    PRINT 'Đã tạo backup bảng PinSlot_Backup_Before_Migration';
END

-- ==============================================
-- PHẦN 2: THÊM CỘT vehicleID VÀO CÁC BẢNG
-- ==============================================

-- Thêm cột vehicleID vào bảng Transaction
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('[TestSchedule].[dbo].[Transaction]') AND name = 'vehicleID')
BEGIN
    ALTER TABLE [TestSchedule].[dbo].[Transaction] 
    ADD vehicleID int NULL;
    PRINT 'Đã thêm cột vehicleID vào bảng Transaction';
END
ELSE
BEGIN
    PRINT 'Cột vehicleID đã tồn tại trong bảng Transaction';
END

-- Thêm cột vehicleID vào bảng PinSlot
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.pinSlot') AND name = 'vehicleID')
BEGIN
    ALTER TABLE dbo.pinSlot 
    ADD vehicleID int NULL;
    PRINT 'Đã thêm cột vehicleID vào bảng PinSlot';
END
ELSE
BEGIN
    PRINT 'Cột vehicleID đã tồn tại trong bảng PinSlot';
END

-- ==============================================
-- PHẦN 3: DI CHUYỂN DỮ LIỆU TỪ userID SANG vehicleID
-- ==============================================

-- Cập nhật vehicleID cho Transaction dựa trên userID
-- Giả định: Mỗi user hiện có 1 vehicle (lấy vehicle đầu tiên nếu có nhiều)
UPDATE T 
SET T.vehicleID = V.vehicleID
FROM [TestSchedule].[dbo].[Transaction] T
INNER JOIN (
    SELECT userID, MIN(vehicleID) as vehicleID 
    FROM Vehicle 
    GROUP BY userID
) V ON T.userID = V.userID
WHERE T.vehicleID IS NULL;

PRINT 'Đã cập nhật vehicleID cho bảng Transaction dựa trên userID';

-- Cập nhật vehicleID cho PinSlot dựa trên userID
-- Chỉ cập nhật những record có userID (đang được reserve)
UPDATE P 
SET P.vehicleID = V.vehicleID
FROM dbo.pinSlot P
INNER JOIN (
    SELECT userID, MIN(vehicleID) as vehicleID 
    FROM Vehicle 
    GROUP BY userID
) V ON P.userID = V.userID
WHERE P.userID IS NOT NULL AND P.vehicleID IS NULL;

PRINT 'Đã cập nhật vehicleID cho bảng PinSlot dựa trên userID';

-- ==============================================
-- PHẦN 4: THÊM FOREIGN KEY CONSTRAINTS
-- ==============================================

-- Thêm foreign key cho Transaction.vehicleID
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_Transaction_Vehicle')
BEGIN
    ALTER TABLE [TestSchedule].[dbo].[Transaction]
    ADD CONSTRAINT FK_Transaction_Vehicle 
    FOREIGN KEY (vehicleID) REFERENCES Vehicle(vehicleID);
    PRINT 'Đã thêm foreign key constraint cho Transaction.vehicleID';
END

-- Thêm foreign key cho PinSlot.vehicleID
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_PinSlot_Vehicle')
BEGIN
    ALTER TABLE dbo.pinSlot
    ADD CONSTRAINT FK_PinSlot_Vehicle 
    FOREIGN KEY (vehicleID) REFERENCES Vehicle(vehicleID);
    PRINT 'Đã thêm foreign key constraint cho PinSlot.vehicleID';
END

-- ==============================================
-- PHẦN 5: CẬP NHẬT CONSTRAINT CHO vehicleID
-- ==============================================

-- Sau khi migration hoàn tất, có thể set vehicleID NOT NULL cho Transaction
-- (Tùy thuộc vào business logic có cho phép transaction không có vehicle hay không)
-- ALTER TABLE [TestSchedule].[dbo].[Transaction] ALTER COLUMN vehicleID int NOT NULL;

-- PinSlot.vehicleID có thể để NULL (pin chưa được reserve)

-- ==============================================
-- PHẦN 6: KIỂM TRA DỮ LIỆU SAU MIGRATION
-- ==============================================

-- Kiểm tra số lượng records đã được cập nhật vehicleID
SELECT 
    'Transaction' as TableName,
    COUNT(*) as TotalRecords,
    COUNT(vehicleID) as RecordsWithVehicleID,
    COUNT(*) - COUNT(vehicleID) as RecordsWithoutVehicleID
FROM [TestSchedule].[dbo].[Transaction]

UNION ALL

SELECT 
    'PinSlot' as TableName,
    COUNT(*) as TotalRecords,
    COUNT(vehicleID) as RecordsWithVehicleID,
    COUNT(*) - COUNT(vehicleID) as RecordsWithoutVehicleID
FROM dbo.pinSlot;

-- Kiểm tra dữ liệu mẫu
SELECT TOP 5 transactionID, userID, vehicleID, amount, stationID, pinID 
FROM [TestSchedule].[dbo].[Transaction]
WHERE vehicleID IS NOT NULL;

SELECT TOP 5 pinID, userID, vehicleID, stationID, pinStatus
FROM dbo.pinSlot
WHERE vehicleID IS NOT NULL;

PRINT 'Migration hoàn tất! Kiểm tra kết quả ở trên.';

-- ==============================================
-- PHẦN 7: SCRIPT XÓA userID (CHẠY SAU KHI KIỂM TRA)
-- Chỉ chạy khi đã đảm bảo migration thành công
-- ==============================================

/*
-- CẢNH BÁO: Chỉ chạy sau khi đã kiểm tra và đảm bảo migration thành công!

-- Xóa cột userID từ Transaction
ALTER TABLE [TestSchedule].[dbo].[Transaction] DROP COLUMN userID;
PRINT 'Đã xóa cột userID từ bảng Transaction';

-- Xóa cột userID từ PinSlot  
ALTER TABLE dbo.pinSlot DROP COLUMN userID;
PRINT 'Đã xóa cột userID từ bảng PinSlot';
*/