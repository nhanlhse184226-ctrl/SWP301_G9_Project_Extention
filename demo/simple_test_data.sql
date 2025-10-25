-- Script tạo dữ liệu test đơn giản - chỉ users, vehicles, transactions
-- Bỏ qua pinSlot và pinStation để tránh foreign key issues

-- Xóa dữ liệu cũ
DELETE FROM [TestSchedule].[dbo].[Transaction];
DELETE FROM Vehicle;
DELETE FROM users;

PRINT 'Đã xóa dữ liệu cũ';

-- Tạo users (3 drivers)
INSERT INTO users (Name, Email, Password, phone, roleID, status) VALUES
('Nguyen Van A', 'nguyenvana@test.com', 'pass123', 0987654321, 1, 1),
('Tran Thi B', 'tranthib@test.com', 'pass123', 0987654322, 1, 1),
('Le Van C', 'levanc@test.com', 'pass123', 0987654323, 1, 1);

PRINT 'Đã tạo 3 users';

-- Tạo vehicles - Test mô hình 1 user có nhiều vehicles
INSERT INTO Vehicle (userID, licensePlate, vehicleType, pinPercent, pinHealth) VALUES
-- User 1 có 3 xe
(1, '30A-111', 'Tesla Model 3', 80, 90),
(1, '30A-222', 'Tesla Model S', 60, 85),
(1, '30A-333', 'Tesla Model Y', 90, 95),
-- User 2 có 2 xe  
(2, '51G-111', 'VinFast VF8', 70, 88),
(2, '51G-222', 'VinFast VF9', 85, 92),
-- User 3 có 1 xe
(3, '29B-111', 'BMW i3', 75, 87);

PRINT 'Đã tạo 6 vehicles (User 1: 3 xe, User 2: 2 xe, User 3: 1 xe)';

-- Kiểm tra cấu trúc bảng Transaction
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('[TestSchedule].[dbo].[Transaction]') AND name = 'vehicleID')
BEGIN
    ALTER TABLE [TestSchedule].[dbo].[Transaction] ADD vehicleID int NULL;
    PRINT 'Đã thêm cột vehicleID vào bảng Transaction';
END

-- Tạo transactions với vehicleID (không cần pinID để tránh foreign key)
INSERT INTO [TestSchedule].[dbo].[Transaction] (vehicleID, amount, pack, stationID, pinID, status, createAt, expireAt) VALUES
-- Transactions cho vehicles của User 1
(1, 50000, 1, 1, 1, 1, DATEADD(HOUR, -2, GETDATE()), DATEADD(HOUR, -1, GETDATE())),
(1, 60000, 2, 1, 2, 1, DATEADD(HOUR, -4, GETDATE()), DATEADD(HOUR, -3, GETDATE())),
(2, 75000, 2, 1, 3, 0, DATEADD(MINUTE, -30, GETDATE()), DATEADD(MINUTE, 30, GETDATE())),
(3, 45000, 1, 2, 4, 1, DATEADD(HOUR, -1, GETDATE()), GETDATE()),
-- Transactions cho vehicles của User 2
(4, 55000, 1, 2, 5, 1, DATEADD(HOUR, -3, GETDATE()), DATEADD(HOUR, -2, GETDATE())),
(5, 70000, 2, 2, 6, 0, DATEADD(MINUTE, -45, GETDATE()), DATEADD(MINUTE, 15, GETDATE())),
-- Transactions cho vehicle của User 3
(6, 40000, 1, 3, 7, 1, DATEADD(HOUR, -5, GETDATE()), DATEADD(HOUR, -4, GETDATE()));

PRINT 'Đã tạo 7 transactions';

-- Kiểm tra kết quả
PRINT '=== KIỂM TRA DỮ LIỆU ===';

SELECT 'Users' as TableType, COUNT(*) as RecordCount FROM users;
SELECT 'Vehicles' as TableType, COUNT(*) as RecordCount FROM Vehicle;  
SELECT 'Transactions' as TableType, COUNT(*) as RecordCount FROM [TestSchedule].[dbo].[Transaction];

PRINT 'Chi tiết vehicles theo user:';
SELECT 
    u.userID,
    u.Name as UserName,
    COUNT(v.vehicleID) as VehicleCount,
    STRING_AGG(CAST(v.vehicleID as NVARCHAR), ', ') as VehicleIDs
FROM users u
LEFT JOIN Vehicle v ON u.userID = v.userID  
GROUP BY u.userID, u.Name
ORDER BY u.userID;

PRINT 'Chi tiết transactions:';
SELECT 
    t.transactionID,
    t.vehicleID,
    v.licensePlate,
    u.Name as OwnerName,
    t.amount,
    t.status,
    t.createAt
FROM [TestSchedule].[dbo].[Transaction] t
INNER JOIN Vehicle v ON t.vehicleID = v.vehicleID
INNER JOIN users u ON v.userID = u.userID
ORDER BY u.userID, t.vehicleID;

PRINT '=== DATA READY FOR TESTING ===';
PRINT 'Test với các API sau:';
PRINT '1. GET /api/vehicle/user?userID=1 (expect: 3 vehicles)';
PRINT '2. GET /api/vehicle/user?userID=2 (expect: 2 vehicles)';
PRINT '3. GET /api/transaction/getByUser?userID=1 (expect: 4 transactions từ 3 vehicles)';
PRINT '4. GET /api/transaction/getByVehicle?vehicleID=1 (expect: 2 transactions)';
PRINT '5. GET /api/user/getByVehicle?vehicleID=1 (expect: User 1 info)';
PRINT '6. POST /api/transaction/create với vehicleID=2';