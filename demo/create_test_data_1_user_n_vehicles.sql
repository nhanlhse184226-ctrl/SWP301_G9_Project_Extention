-- Script tạo dữ liệu test cho mô hình 1 User - N Vehicles
-- Ngày: 25/10/2025
-- Mục đích: Test tất cả API sau khi migration

-- ==============================================
-- BƯỚC 1: XÓA DỮ LIỆU CŨ (NẾU CÓ)
-- ==============================================
PRINT 'Xóa dữ liệu cũ...';

-- Xóa theo thứ tự để tránh vi phạm foreign key
DELETE FROM [TestSchedule].[dbo].[Transaction];
DELETE FROM dbo.pinSlot;
DELETE FROM Vehicle;
DELETE FROM users;

PRINT 'Đã xóa tất cả dữ liệu cũ';

-- ==============================================
-- BƯỚC 2: TẠO DỮ LIỆU USERS
-- ==============================================
PRINT 'Tạo dữ liệu users...';

INSERT INTO users (Name, Email, Password, phone, roleID, status) VALUES
-- Drivers (roleID = 1)
(N'Nguyễn Văn A', 'nguyenvana@gmail.com', 'password123', 0987654321, 1, 1),
(N'Trần Thị B', 'tranthib@gmail.com', 'password123', 0987654322, 1, 1),
(N'Lê Văn C', 'levanc@gmail.com', 'password123', 0987654323, 1, 1),
(N'Phạm Thị D', 'phamthid@gmail.com', 'password123', 0987654324, 1, 1),

-- Staff (roleID = 2)
(N'Hoàng Văn E', 'hoangvane@company.com', 'staff123', 0987654325, 2, 1),
(N'Vũ Thị F', 'vuthif@company.com', 'staff123', 0987654326, 2, 1),

-- Admin (roleID = 3)
(N'Admin System', 'admin@system.com', 'admin123', 0987654327, 3, 1);

PRINT 'Đã tạo 7 users (4 drivers, 2 staff, 1 admin)';

-- ==============================================
-- BƯỚC 3: TẠO DỮ LIỆU VEHICLES (1 USER - N VEHICLES)
-- ==============================================
PRINT 'Tạo dữ liệu vehicles...';

INSERT INTO Vehicle (userID, licensePlate, vehicleType, pinPercent, pinHealth) VALUES
-- User 1 (Nguyễn Văn A) có 3 xe
(1, '30A-12345', 'Tesla Model 3', 45, 85),
(1, '30A-12346', 'Tesla Model S', 78, 92),
(1, '30A-12347', 'Tesla Model Y', 23, 76),

-- User 2 (Trần Thị B) có 2 xe
(2, '51G-67890', 'VinFast VF8', 89, 88),
(2, '51G-67891', 'VinFast VF9', 56, 94),

-- User 3 (Lê Văn C) có 2 xe
(3, '29B-24680', 'BMW i3', 67, 82),
(3, '29B-24681', 'BMW iX', 34, 90),

-- User 4 (Phạm Thị D) có 1 xe
(4, '60A-13579', 'Hyundai Kona Electric', 91, 86),

-- User 5,6,7 không có xe (staff và admin)
-- Tổng: 8 vehicles cho 4 users (test mô hình 1:N)
;

PRINT 'Đã tạo 8 vehicles cho 4 users (test 1 user - N vehicles)';

-- ==============================================
-- BƯỚC 4: TẠO DỮ LIỆU PIN STATIONS
-- ==============================================
PRINT 'Tạo dữ liệu pin stations...';

-- Kiểm tra và tạo bảng pinStation nếu chưa có
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='pinStation' AND xtype='U')
BEGIN
    CREATE TABLE pinStation (
        stationID int IDENTITY(1,1) PRIMARY KEY,
        stationName nvarchar(100) NOT NULL,
        address nvarchar(200),
        userID int, -- staff quản lý station
        createAt datetime DEFAULT GETDATE()
    );
    PRINT 'Đã tạo bảng pinStation';
END

-- Xóa dữ liệu cũ trong pinStation nếu có
DELETE FROM pinStation;

INSERT INTO pinStation (stationName, address, userID) VALUES
(N'Trạm sạc Hà Nội 1', N'123 Nguyễn Trãi, Thanh Xuân, Hà Nội', 5),
(N'Trạm sạc Hà Nội 2', N'456 Láng Hạ, Đống Đa, Hà Nội', 5),
(N'Trạm sạc TP.HCM 1', N'789 Nguyễn Huệ, Quận 1, TP.HCM', 6),
(N'Trạm sạc TP.HCM 2', N'321 Lê Lợi, Quận 1, TP.HCM', 6);

PRINT 'Đã tạo 4 pin stations';

-- ==============================================
-- BƯỚC 5: TẠO DỮ LIỆU PIN SLOTS
-- ==============================================
PRINT 'Tạo dữ liệu pin slots...';

-- Kiểm tra cấu trúc bảng pinSlot
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.pinSlot') AND name = 'vehicleID')
BEGIN
    ALTER TABLE dbo.pinSlot ADD vehicleID int NULL;
    PRINT 'Đã thêm cột vehicleID vào bảng pinSlot';
END

INSERT INTO dbo.pinSlot (pinPercent, pinHealth, pinStatus, status, vehicleID, stationID) VALUES
-- Station 1: 6 slots
(100, 95, 1, 1, NULL, 1),          -- Slot available
(100, 92, 1, 1, NULL, 1),          -- Slot available  
(85, 88, 0, 2, 1, 1),              -- Slot reserved by vehicle 1
(90, 90, 0, 2, 3, 1),              -- Slot reserved by vehicle 3
(100, 94, 1, 1, NULL, 1),          -- Slot available
(75, 85, 0, 0, NULL, 1),           -- Slot charging

-- Station 2: 4 slots
(100, 96, 1, 1, NULL, 2),          -- Slot available
(95, 89, 0, 2, 2, 2),              -- Slot reserved by vehicle 2
(100, 91, 1, 1, NULL, 2),          -- Slot available
(88, 87, 0, 0, NULL, 2),           -- Slot charging

-- Station 3: 5 slots  
(100, 93, 1, 1, NULL, 3),          -- Slot available
(100, 95, 1, 1, NULL, 3),          -- Slot available
(92, 86, 0, 2, 5, 3),              -- Slot reserved by vehicle 5
(100, 92, 1, 1, NULL, 3),          -- Slot available
(80, 84, 0, 0, NULL, 3),           -- Slot charging

-- Station 4: 3 slots
(100, 97, 1, 1, NULL, 4),          -- Slot available
(87, 83, 0, 2, 7, 4),              -- Slot reserved by vehicle 7
(100, 89, 1, 1, NULL, 4);          -- Slot available

PRINT 'Đã tạo 18 pin slots cho 4 stations (một số được reserve bởi vehicles)';

-- ==============================================
-- BƯỚC 6: TẠO DỮ LIỆU TRANSACTIONS
-- ==============================================
PRINT 'Tạo dữ liệu transactions...';

-- Kiểm tra cấu trúc bảng Transaction
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('[TestSchedule].[dbo].[Transaction]') AND name = 'vehicleID')
BEGIN
    ALTER TABLE [TestSchedule].[dbo].[Transaction] ADD vehicleID int NULL;
    PRINT 'Đã thêm cột vehicleID vào bảng Transaction';
END

INSERT INTO [TestSchedule].[dbo].[Transaction] (vehicleID, amount, pack, stationID, pinID, status, createAt, expireAt) VALUES
-- Transactions cho vehicle 1 (Tesla Model 3 của user 1)
(1, 50000, 1, 1, 3, 1, DATEADD(HOUR, -2, GETDATE()), DATEADD(HOUR, -1, GETDATE())),
(1, 75000, 2, 2, 8, 1, DATEADD(HOUR, -5, GETDATE()), DATEADD(HOUR, -4, GETDATE())),

-- Transactions cho vehicle 2 (Tesla Model S của user 1)  
(2, 60000, 1, 2, 8, 0, DATEADD(MINUTE, -30, GETDATE()), DATEADD(MINUTE, 30, GETDATE())),

-- Transactions cho vehicle 3 (Tesla Model Y của user 1)
(3, 45000, 1, 1, 4, 1, DATEADD(HOUR, -1, GETDATE()), GETDATE()),

-- Transactions cho vehicle 4 (VinFast VF8 của user 2)
(4, 55000, 2, 3, 13, 1, DATEADD(HOUR, -3, GETDATE()), DATEADD(HOUR, -2, GETDATE())),

-- Transactions cho vehicle 5 (VinFast VF9 của user 2)
(5, 70000, 2, 3, 13, 0, DATEADD(MINUTE, -45, GETDATE()), DATEADD(MINUTE, 15, GETDATE())),

-- Transactions cho vehicle 6 (BMW i3 của user 3)
(6, 40000, 1, 4, 17, 1, DATEADD(HOUR, -4, GETDATE()), DATEADD(HOUR, -3, GETDATE())),

-- Transactions cho vehicle 7 (BMW iX của user 3)
(7, 80000, 3, 4, 17, 0, DATEADD(MINUTE, -20, GETDATE()), DATEADD(MINUTE, 40, GETDATE())),

-- Transactions cho vehicle 8 (Hyundai Kona của user 4)
(8, 35000, 1, 1, 1, 1, DATEADD(HOUR, -6, GETDATE()), DATEADD(HOUR, -5, GETDATE()));

PRINT 'Đã tạo 9 transactions cho 8 vehicles của 4 users';

-- ==============================================
-- BƯỚC 7: KIỂM TRA DỮ LIỆU ĐƯỢC TẠO
-- ==============================================
PRINT '=== KIỂM TRA DỮ LIỆU ===';

-- Kiểm tra users
SELECT 'USERS' as TableName, COUNT(*) as RecordCount FROM users;
SELECT userID, Name, Email, roleID, status FROM users ORDER BY userID;

-- Kiểm tra vehicles theo user (test 1 user - N vehicles)
SELECT 'VEHICLES' as TableName, COUNT(*) as RecordCount FROM Vehicle;
SELECT 
    u.userID,
    u.Name as UserName,
    COUNT(v.vehicleID) as VehicleCount,
    STRING_AGG(v.licensePlate, ', ') as LicensePlates
FROM users u
LEFT JOIN Vehicle v ON u.userID = v.userID
GROUP BY u.userID, u.Name
ORDER BY u.userID;

-- Kiểm tra transactions với vehicleID
SELECT 'TRANSACTIONS' as TableName, COUNT(*) as RecordCount FROM [TestSchedule].[dbo].[Transaction];
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
ORDER BY t.createAt DESC;

-- Kiểm tra pin slots với vehicleID
SELECT 'PINSLOTS' as TableName, COUNT(*) as RecordCount FROM dbo.pinSlot;
SELECT 
    p.pinID,
    p.vehicleID,
    v.licensePlate,
    u.Name as ReservedBy,
    p.stationID,
    p.status as SlotStatus
FROM dbo.pinSlot p
LEFT JOIN Vehicle v ON p.vehicleID = v.vehicleID
LEFT JOIN users u ON v.userID = u.userID
WHERE p.vehicleID IS NOT NULL
ORDER BY p.stationID, p.pinID;

PRINT '=== TẠO DỮ LIỆU TEST HOÀN TẤT ===';

-- ==============================================
-- BƯỚC 8: CÂU LỆNH TEST API
-- ==============================================
PRINT '=== CÂU LỆNH TEST ===';
PRINT 'Sử dụng các endpoint sau để test:';
PRINT '';
PRINT '1. List vehicles của user 1: GET /api/vehicle/user?userID=1';
PRINT '2. List transactions của user 1: GET /api/transaction/getByUser?userID=1';
PRINT '3. List transactions của vehicle 1: GET /api/transaction/getByVehicle?vehicleID=1';
PRINT '4. Get user owner của vehicle 1: GET /api/user/getByVehicle?vehicleID=1';
PRINT '5. List pin slots reserved bởi vehicle 1: GET /api/pinSlot/getByVehicle?vehicleID=1';
PRINT '6. Create transaction mới: POST /api/transaction/create với vehicleID=2';
PRINT '';
PRINT 'Test data summary:';
PRINT '- User 1: có 3 vehicles (vehicleID: 1,2,3)';
PRINT '- User 2: có 2 vehicles (vehicleID: 4,5)';  
PRINT '- User 3: có 2 vehicles (vehicleID: 6,7)';
PRINT '- User 4: có 1 vehicle (vehicleID: 8)';
PRINT '- Total: 9 transactions, 18 pin slots, 4 stations';