-- Script tạo bảng payment cho hệ thống thanh toán
-- Chạy script này trong SQL Server Management Studio

-- Tạo bảng payment
CREATE TABLE payment (
    paymentID int IDENTITY(1,1) PRIMARY KEY,
    userID int NOT NULL,
    amount decimal(18,2) NOT NULL,
    paymentStatus nvarchar(20) NOT NULL DEFAULT 'pending',
    paymentMethod nvarchar(50) NOT NULL,
    createdTime datetime2 NOT NULL DEFAULT GETDATE(),
    description nvarchar(255),
    transactionID nvarchar(100),
    
    -- Constraints
    CONSTRAINT CHK_amount CHECK (amount > 0),
    CONSTRAINT CHK_payment_status CHECK (paymentStatus IN ('pending', 'completed', 'failed', 'cancelled'))
);

-- Tạo indexes để tăng performance
CREATE INDEX IX_payment_userID ON payment(userID);
CREATE INDEX IX_payment_status ON payment(paymentStatus);
CREATE INDEX IX_payment_createdTime ON payment(createdTime DESC);

-- Insert sample data để test
INSERT INTO payment (userID, amount, paymentStatus, paymentMethod, description, transactionID) VALUES
(1, 100000.00, 'completed', 'credit_card', 'Payment for charging service', 'TXN001'),
(1, 50000.00, 'pending', 'momo', 'Payment for parking fee', 'TXN002'),
(2, 75000.00, 'completed', 'banking', 'Payment for reservation', 'TXN003');

-- Kiểm tra dữ liệu đã insert
SELECT * FROM payment ORDER BY createdTime DESC;

PRINT 'Payment table created successfully with sample data!';