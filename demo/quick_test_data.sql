-- SCRIPT TEST NHANH - Tạo dữ liệu minimal để test 1 User - N Vehicles
-- Chạy script này nếu muốn test nhanh, không cần dữ liệu phức tạp

-- Xóa dữ liệu cũ
DELETE FROM [TestSchedule].[dbo].[Transaction];
DELETE FROM dbo.pinSlot;  
DELETE FROM Vehicle;
DELETE FROM users;

-- Tạo 3 users drivers
INSERT INTO users (Name, Email, Password, phone, roleID, status) VALUES
('User A', 'usera@test.com', 'pass123', 0987654321, 1, 1),
('User B', 'userb@test.com', 'pass123', 0987654322, 1, 1),
('User C', 'userc@test.com', 'pass123', 0987654323, 1, 1);

-- Tạo vehicles: User 1 có 3 xe, User 2 có 2 xe, User 3 có 1 xe
INSERT INTO Vehicle (userID, licensePlate, vehicleType, pinPercent, pinHealth) VALUES
(1, '30A-001', 'Tesla Model 3', 80, 90),    -- Vehicle ID 1
(1, '30A-002', 'Tesla Model S', 60, 85),    -- Vehicle ID 2  
(1, '30A-003', 'Tesla Model Y', 90, 95),    -- Vehicle ID 3
(2, '51G-001', 'VinFast VF8', 70, 88),      -- Vehicle ID 4
(2, '51G-002', 'VinFast VF9', 85, 92),      -- Vehicle ID 5
(3, '29B-001', 'BMW i3', 75, 87);           -- Vehicle ID 6

-- Tạo pin slots với một số được reserve bởi vehicles
INSERT INTO dbo.pinSlot (pinPercent, pinHealth, pinStatus, status, vehicleID, stationID) VALUES
(100, 95, 1, 1, NULL, 1),      -- Available
(85, 88, 0, 2, 1, 1),          -- Reserved by vehicle 1
(90, 90, 0, 2, 4, 1),          -- Reserved by vehicle 4
(100, 92, 1, 1, NULL, 1),      -- Available
(95, 89, 0, 2, 5, 2),          -- Reserved by vehicle 5
(100, 91, 1, 1, NULL, 2);      -- Available

-- Tạo transactions với vehicleID
INSERT INTO [TestSchedule].[dbo].[Transaction] (vehicleID, amount, pack, stationID, pinID, status, createAt, expireAt) VALUES
(1, 50000, 1, 1, 2, 1, DATEADD(HOUR, -2, GETDATE()), DATEADD(HOUR, -1, GETDATE())),
(1, 60000, 2, 1, 2, 1, DATEADD(HOUR, -4, GETDATE()), DATEADD(HOUR, -3, GETDATE())),
(2, 75000, 2, 1, 4, 0, DATEADD(MINUTE, -30, GETDATE()), DATEADD(MINUTE, 30, GETDATE())),
(4, 55000, 1, 1, 3, 1, DATEADD(HOUR, -1, GETDATE()), GETDATE()),
(5, 70000, 2, 2, 5, 0, DATEADD(MINUTE, -45, GETDATE()), DATEADD(MINUTE, 15, GETDATE())),
(6, 40000, 1, 2, 6, 1, DATEADD(HOUR, -3, GETDATE()), DATEADD(HOUR, -2, GETDATE()));

-- Kiểm tra kết quả
PRINT '=== TEST DATA CREATED ===';
SELECT 'Users' as Type, COUNT(*) as Count FROM users;
SELECT 'Vehicles' as Type, COUNT(*) as Count FROM Vehicle;
SELECT 'Transactions' as Type, COUNT(*) as Count FROM [TestSchedule].[dbo].[Transaction];
SELECT 'PinSlots' as Type, COUNT(*) as Count FROM dbo.pinSlot;

PRINT 'User-Vehicle mapping:';
SELECT u.userID, u.Name, COUNT(v.vehicleID) as VehicleCount
FROM users u LEFT JOIN Vehicle v ON u.userID = v.userID
GROUP BY u.userID, u.Name;

PRINT 'Test ready! Use these IDs:';
PRINT 'User 1: 3 vehicles (IDs: 1,2,3)';
PRINT 'User 2: 2 vehicles (IDs: 4,5)';  
PRINT 'User 3: 1 vehicle (ID: 6)';

PRINT 'Test commands:';
PRINT 'GET /api/vehicle/user?userID=1';
PRINT 'GET /api/transaction/getByUser?userID=1';
PRINT 'GET /api/transaction/getByVehicle?vehicleID=1';
PRINT 'POST /api/transaction/create with vehicleID=2';