# Hướng dẫn Migration từ 1 User - 1 Vehicle sang 1 User - N Vehicles

## Tổng quan thay đổi
Dự án đã được cập nhật để hỗ trợ mô hình **1 User có nhiều Vehicles** thay vì **1 User - 1 Vehicle**. Các thay đổi chính:

### 1. Database Schema Changes
- **Transaction table**: Thêm cột `vehicleID`, thay thế `userID`
- **PinSlot table**: Thêm cột `vehicleID`, thay thế `userID`
- **Foreign Keys**: Tạo khóa ngoại từ `vehicleID` đến bảng `Vehicle`

### 2. DTO Updates
- **TransactionDTO**: Thay `userID` → `vehicleID`
- **PinSlotDTO**: Thay `userID` → `vehicleID`

### 3. DAO Updates
- **TransactionDAO**:
  - `getTransactionsByVehicle(int vehicleID)` - Lấy transactions theo vehicleID
  - `getTransactionsByUser(int userID)` - Lấy transactions của user qua tất cả vehicles
  - `createTransactionWithVehicle(int vehicleID, ...)` - Tạo transaction mới với vehicleID
- **PinSlotDAO**:
  - `getListPinSlotByVehicle(int vehicleID)` - Lấy pin slots theo vehicleID
- **UserDAO**:
  - `getUserByVehicleID(int vehicleID)` - Lấy user owner của vehicle

### 4. Controller Updates
- **TransactionController**: Thêm endpoint `/transaction/getByVehicle`
- **PinSlotController**: Thêm endpoint `/pinSlot/getByVehicle`
- **UserController**: Thêm endpoint `/user/getByVehicle`

## Cách thực hiện Migration

### Bước 1: Backup Database
```sql
-- Chạy script backup trong migration_user_to_vehicle_relationship.sql
-- Tạo backup bảng Transaction và PinSlot
```

### Bước 2: Chạy Migration Script
```sql
-- Chạy file: migration_user_to_vehicle_relationship.sql
-- Script sẽ:
-- 1. Thêm cột vehicleID vào Transaction và PinSlot
-- 2. Di chuyển dữ liệu từ userID sang vehicleID
-- 3. Tạo foreign key constraints
-- 4. Kiểm tra dữ liệu sau migration
```

### Bước 3: Cập nhật Application Code
- Code đã được cập nhật trong các commit này
- Rebuild và restart application

### Bước 4: Test APIs

#### API mới được thêm:
```
GET /api/transaction/getByVehicle?vehicleID={id}
GET /api/pinSlot/getByVehicle?vehicleID={id}  
GET /api/user/getByVehicle?vehicleID={id}
```

#### API được cập nhật:
```
GET /api/transaction/getByUser?userID={id}
// Bây giờ trả về transactions của tất cả vehicles thuộc user
```

## Lệnh SQL Server để Migration

### 1. Thêm cột vehicleID
```sql
-- Thêm vào Transaction
ALTER TABLE [TestSchedule].[dbo].[Transaction] ADD vehicleID int NULL;

-- Thêm vào PinSlot
ALTER TABLE dbo.pinSlot ADD vehicleID int NULL;
```

### 2. Migrate dữ liệu
```sql
-- Cập nhật Transaction
UPDATE T 
SET T.vehicleID = V.vehicleID
FROM [TestSchedule].[dbo].[Transaction] T
INNER JOIN (
    SELECT userID, MIN(vehicleID) as vehicleID 
    FROM Vehicle 
    GROUP BY userID
) V ON T.userID = V.userID;

-- Cập nhật PinSlot
UPDATE P 
SET P.vehicleID = V.vehicleID
FROM dbo.pinSlot P
INNER JOIN (
    SELECT userID, MIN(vehicleID) as vehicleID 
    FROM Vehicle 
    GROUP BY userID
) V ON P.userID = V.userID
WHERE P.userID IS NOT NULL;
```

### 3. Thêm Foreign Keys
```sql
-- Transaction
ALTER TABLE [TestSchedule].[dbo].[Transaction]
ADD CONSTRAINT FK_Transaction_Vehicle 
FOREIGN KEY (vehicleID) REFERENCES Vehicle(vehicleID);

-- PinSlot
ALTER TABLE dbo.pinSlot
ADD CONSTRAINT FK_PinSlot_Vehicle 
FOREIGN KEY (vehicleID) REFERENCES Vehicle(vehicleID);
```

### 4. Xóa cột userID (sau khi test thành công)
```sql
-- CẢNH BÁO: Chỉ chạy sau khi đảm bảo migration thành công!
ALTER TABLE [TestSchedule].[dbo].[Transaction] DROP COLUMN userID;
ALTER TABLE dbo.pinSlot DROP COLUMN userID;
```

## API Examples

### 1. Lấy transactions theo vehicle
```http
GET /api/transaction/getByVehicle?vehicleID=1
```

### 2. Lấy tất cả transactions của user (qua vehicles)
```http
GET /api/transaction/getByUser?userID=1
```

### 3. Lấy pin slots được reserve bởi vehicle
```http
GET /api/pinSlot/getByVehicle?vehicleID=1
```

### 4. Lấy user owner của vehicle
```http
GET /api/user/getByVehicle?vehicleID=1
```

### 5. Lấy tất cả vehicles của user
```http
GET /api/vehicle/user?userID=1
```

## Lưu ý quan trọng

### Backward Compatibility
- API cũ `/transaction/getByUser` vẫn hoạt động nhưng logic đã thay đổi
- Bây giờ trả về transactions của TẤT CẢ vehicles thuộc user
- Method `createTransaction(userID, ...)` được đánh dấu `@Deprecated`
- Sử dụng `createTransactionWithVehicle(vehicleID, ...)` cho code mới

### Business Logic Changes
- **Transaction creation**: Bây giờ cần vehicleID thay vì userID
- **Pin reservation**: PinSlot được reserve bởi vehicle, không phải user
- **User lookup**: Có thể tìm user từ vehicleID

### Migration Rollback
Nếu cần rollback:
1. Restore từ backup tables: `Transaction_Backup_Before_Migration`, `PinSlot_Backup_Before_Migration`
2. Revert code changes
3. Restart application

## Testing Checklist

- [ ] Migration script chạy thành công
- [ ] Dữ liệu được migrate đúng (check bằng SELECT queries)
- [ ] API mới hoạt động: `/transaction/getByVehicle`, `/pinSlot/getByVehicle`, `/user/getByVehicle`
- [ ] API cũ vẫn hoạt động: `/transaction/getByUser` (với logic mới)
- [ ] Foreign key constraints hoạt động
- [ ] Tạo transaction mới với vehicleID thành công
- [ ] 1 user có thể có nhiều vehicles
- [ ] Performance không bị ảnh hưởng

## Thời gian thực hiện
- **Migration script**: ~5-10 phút (tùy thuộc volume dữ liệu)
- **Application restart**: ~2-3 phút
- **Total downtime**: ~10-15 phút