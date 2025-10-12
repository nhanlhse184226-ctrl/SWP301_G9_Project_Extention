-- Create Subscription table
CREATE TABLE Subscription (
    userID INT NOT NULL,
    total INT NOT NULL,
    PRIMARY KEY (userID),
    FOREIGN KEY (userID) REFERENCES users(userID)
);

-- Add total column to VNPayPayment table (if not exists)
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'VNPayPaymentDTO' AND COLUMN_NAME = 'total')
BEGIN
    ALTER TABLE VNPayPaymentDTO ADD total INT NULL;
END