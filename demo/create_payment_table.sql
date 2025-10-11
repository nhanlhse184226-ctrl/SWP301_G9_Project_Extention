-- Script tạo bảng payment cho hệ thống thanh toán VNPay
-- Chạy script này trong SQL Server Management Studio

-- Tạo bảng payment với các trường phù hợp cho VNPay
CREATE TABLE payment (
    id int IDENTITY(1,1) PRIMARY KEY,
    vnp_TxnRef nvarchar(100) NOT NULL UNIQUE,  -- Mã giao dịch duy nhất từ VNPay
    vnp_OrderInfo nvarchar(255),               -- Thông tin đơn hàng
    vnp_OrderType nvarchar(100),               -- Mã danh mục hàng hóa
    vnp_Amount decimal(18,2) NOT NULL,         -- Số tiền thanh toán
    vnp_IpAddr nvarchar(45),                   -- Địa chỉ IP của khách hàng
    vnp_ResponseCode nvarchar(10),             -- Mã phản hồi từ VNPay
    vnp_TransactionStatus nvarchar(10),        -- Trạng thái giao dịch tại VNPay
    vnp_BankCode nvarchar(50),                 -- Mã ngân hàng
    vnp_BankTranNo nvarchar(100),              -- Mã giao dịch ngân hàng
    vnp_CardType nvarchar(50),                 -- Loại thẻ (ATM, QRCODE, INTCARD)
    vnp_PayDate nvarchar(20),                  -- Thời gian thanh toán (yyyyMMddHHmmss)
    vnp_TransactionNo nvarchar(100),           -- Mã giao dịch VNPay
    status nvarchar(20) NOT NULL DEFAULT 'pending',  -- Trạng thái thanh toán cục bộ
    createdAt datetime2 NOT NULL DEFAULT GETDATE(),  -- Thời gian tạo
    updatedAt datetime2 NOT NULL DEFAULT GETDATE(),  -- Thời gian cập nhật

    -- Constraints
    CONSTRAINT CHK_vnp_amount CHECK (vnp_Amount > 0),
    CONSTRAINT CHK_payment_status CHECK (status IN ('pending', 'completed', 'failed', 'cancelled')),
    CONSTRAINT CHK_vnp_response_code CHECK (vnp_ResponseCode IN ('00', '01', '02', '04', '05', '06', '07', '09', '10', '11', '12', '13', '24', '51', '65', '75', '79', '99')),
    CONSTRAINT CHK_vnp_transaction_status CHECK (vnp_TransactionStatus IN ('00', '01', '02', '04', '05', '06', '07', '09'))
);
CREATE TABLE payment (
    id int IDENTITY(1,1) PRIMARY KEY,
    vnp_TxnRef nvarchar(100) NOT NULL UNIQUE,  -- Mã giao dịch duy nhất từ VNPay
    vnp_OrderInfo nvarchar(255),               -- Thông tin đơn hàng
    vnp_Amount decimal(18,2) NOT NULL,         -- Số tiền thanh toán
    vnp_ResponseCode nvarchar(10),             -- Mã phản hồi từ VNPay
    vnp_BankCode nvarchar(50),                 -- Mã ngân hàng
    vnp_BankTranNo nvarchar(100),              -- Mã giao dịch ngân hàng
    vnp_CardType nvarchar(50),                 -- Loại thẻ
    vnp_PayDate nvarchar(20),                  -- Thời gian thanh toán (yyyyMMddHHmmss)
    vnp_TransactionNo nvarchar(100),           -- Mã giao dịch VNPay
    status nvarchar(20) NOT NULL DEFAULT 'pending',  -- Trạng thái thanh toán
    createdAt datetime2 NOT NULL DEFAULT GETDATE(),  -- Thời gian tạo
    updatedAt datetime2 NOT NULL DEFAULT GETDATE(),  -- Thời gian cập nhật

    -- Constraints
    CONSTRAINT CHK_vnp_amount CHECK (vnp_Amount > 0),
    CONSTRAINT CHK_payment_status CHECK (status IN ('pending', 'completed', 'failed', 'cancelled'))
);

-- Tạo indexes để tăng performance
CREATE INDEX IX_payment_vnp_TxnRef ON payment(vnp_TxnRef);
CREATE INDEX IX_payment_status ON payment(status);
CREATE INDEX IX_payment_createdAt ON payment(createdAt DESC);

-- Insert sample data để test (dữ liệu giả lập VNPay)
INSERT INTO payment (vnp_TxnRef, vnp_OrderInfo, vnp_Amount, vnp_ResponseCode, vnp_BankCode, vnp_BankTranNo, vnp_CardType, vnp_PayDate, vnp_TransactionNo, status) VALUES
('TXN001', 'Thanh toan dich vu sac pin', 100000.00, '00', 'NCB', 'VNP140123456', 'ATM', '20231010120000', '140123456', 'completed'),
('TXN002', 'Thanh toan phi do xe', 50000.00, '00', 'VCB', 'VNP140123457', 'ATM', '20231010120100', '140123457', 'completed'),
('TXN003', 'Thanh toan dat cho', 75000.00, '24', NULL, NULL, NULL, NULL, NULL, 'failed');

-- Kiểm tra dữ liệu đã insert
SELECT * FROM payment ORDER BY createdAt DESC;

PRINT 'Payment table for VNPay created successfully with sample data!';
-- Tạo indexes để tăng performance
CREATE INDEX IX_payment_vnp_TxnRef ON payment(vnp_TxnRef);
CREATE INDEX IX_payment_status ON payment(status);
CREATE INDEX IX_payment_createdAt ON payment(createdAt DESC);

-- Insert sample data để test (dữ liệu giả lập VNPay)
INSERT INTO payment (vnp_TxnRef, vnp_OrderInfo, vnp_OrderType, vnp_Amount, vnp_IpAddr, vnp_ResponseCode, vnp_TransactionStatus, vnp_BankCode, vnp_BankTranNo, vnp_CardType, vnp_PayDate, vnp_TransactionNo, status) VALUES
('TXN001', N'Thanh toan dich vu sac pin', 'billpayment', 100000.00, '192.168.1.100', '00', '00', 'NCB', 'VNP140123456', 'ATM', '20231010120000', '140123456', 'completed'),
('TXN002', N'Thanh toan phi do xe', 'billpayment', 50000.00, '192.168.1.101', '00', '00', 'VCB', 'VNP140123457', 'ATM', '20231010120100', '140123457', 'completed'),
('TXN003', N'Thanh toan dat cho', 'billpayment', 75000.00, '192.168.1.102', '24', '02', NULL, NULL, NULL, NULL, NULL, 'failed');

-- Kiểm tra dữ liệu đã insert
SELECT * FROM payment ORDER BY createdAt DESC;

PRINT 'Payment table for VNPay created successfully with sample data!';