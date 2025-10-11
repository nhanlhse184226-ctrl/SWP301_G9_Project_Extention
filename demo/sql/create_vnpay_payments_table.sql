-- SQL Server script to create vnpay_payment table
-- Run in your database: USE YourDatabase; then execute this script

CREATE TABLE dbo.vnpay_payment (
    paymentID INT IDENTITY(1,1) PRIMARY KEY,
    userID INT NULL,
    servicePackID INT NULL,
    vnp_TxnRef VARCHAR(64) NOT NULL UNIQUE,
    vnp_OrderInfo NVARCHAR(512) NULL,
    vnp_Amount BIGINT NULL,
    vnp_TransactionNo VARCHAR(128) NULL,
    vnp_ResponseCode VARCHAR(8) NULL,
    vnp_TransactionStatus VARCHAR(8) NULL,
    vnp_PayDate VARCHAR(32) NULL,
    vnp_BankCode VARCHAR(32) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    createdAt DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updatedAt DATETIME2 NULL,
    expiredAt DATETIME2 NULL
);

-- Indexes for performance
CREATE INDEX idx_vnpay_payments_user ON dbo.vnpay_payment(userID);
CREATE INDEX idx_vnpay_payments_status ON dbo.vnpay_payment(status);
CREATE INDEX idx_vnpay_payments_txnref ON dbo.vnpay_payment(vnp_TxnRef);
