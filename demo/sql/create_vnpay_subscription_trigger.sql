-- Trigger để tự động cập nhật bảng Subscription khi VNPayPayment status = 1
CREATE OR ALTER TRIGGER tr_UpdateSubscription_OnPaymentSuccess
ON VNPayPaymentDTO
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Chỉ xử lý khi status được update thành 1 (SUCCESS) và có total > 0
    IF UPDATE(status)
    BEGIN
        -- Sử dụng MERGE để INSERT hoặc UPDATE
        MERGE Subscription AS target
        USING (
            SELECT 
                i.userID, 
                i.total
            FROM inserted i
            INNER JOIN deleted d ON i.paymentID = d.paymentID
            WHERE i.status = 1  -- Payment thành công
              AND d.status != 1  -- Trước đó không phải SUCCESS (tránh duplicate)
              AND i.userID IS NOT NULL 
              AND i.total IS NOT NULL 
              AND i.total > 0
        ) AS source (userID, total)
        ON target.userID = source.userID
        
        WHEN MATCHED THEN
            UPDATE SET total = target.total + source.total
            
        WHEN NOT MATCHED THEN
            INSERT (userID, total) VALUES (source.userID, source.total);
            
        -- Log để debug
        IF @@ROWCOUNT > 0
        BEGIN
            DECLARE @affectedRows INT = @@ROWCOUNT;
            DECLARE @message NVARCHAR(200) = 'Trigger updated Subscription for ' + CAST(@affectedRows AS NVARCHAR(10)) + ' payments';
            PRINT @message;
        END
    END
END;