-- Create reports table with foreign keys to users table
-- This script creates the reports table for the report management system

CREATE TABLE reports (
    id INT IDENTITY(1,1) PRIMARY KEY,              -- Auto-increment primary key
    type INT NOT NULL,                              -- 1=Station, 2=Slot, 3=Battery, 4=Other
    description TEXT NOT NULL,                      -- Description with no character limit
    reporter_id INT NOT NULL,                       -- Foreign Key: User who creates report (roleID=3)
    handler_id INT NULL,                           -- Foreign Key: Admin who handles report (roleID=1)
    created_at DATETIME NOT NULL DEFAULT GETDATE(), -- Timestamp when report was created
    status INT NOT NULL DEFAULT 0,                 -- 0=Pending, 1=InProgress, 2=Resolved
    
    -- Foreign key constraints
    CONSTRAINT FK_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(userID),
    CONSTRAINT FK_reports_handler FOREIGN KEY (handler_id) REFERENCES users(userID),
    
    -- Check constraints for data validation
    CONSTRAINT CHK_reports_type CHECK (type IN (1, 2, 3, 4)),
    CONSTRAINT CHK_reports_status CHECK (status IN (0, 1, 2)),
    CONSTRAINT CHK_reports_description CHECK (LEN(TRIM(description)) > 0)
);

-- Create indexes for better performance
CREATE INDEX IX_reports_status ON reports(status);
CREATE INDEX IX_reports_reporter_id ON reports(reporter_id);
CREATE INDEX IX_reports_handler_id ON reports(handler_id);
CREATE INDEX IX_reports_created_at ON reports(created_at);
CREATE INDEX IX_reports_type ON reports(type);

-- Insert sample data for testing (optional)
-- Make sure you have users with userID 1 (admin, roleID=1) and userID 3 (user, roleID=3)
/*
INSERT INTO reports (type, description, reporter_id, handler_id, created_at, status) VALUES
(1, 'Station not working properly, screen is flickering', 3, NULL, GETDATE(), 0),
(2, 'Slot number 5 is stuck, cannot insert battery', 3, 1, DATEADD(HOUR, -2, GETDATE()), 1),
(3, 'Battery is not charging correctly, shows error message', 3, 1, DATEADD(DAY, -1, GETDATE()), 2),
(4, 'General maintenance request for station cleaning', 3, NULL, DATEADD(HOUR, -1, GETDATE()), 0);
*/

-- Verify the table creation
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'reports'
ORDER BY ORDINAL_POSITION;

-- Verify foreign key constraints
SELECT 
    fk.name AS constraint_name,
    OBJECT_NAME(fk.parent_object_id) AS table_name,
    COL_NAME(fkc.parent_object_id, fkc.parent_column_id) AS column_name,
    OBJECT_NAME(fk.referenced_object_id) AS referenced_table_name,
    COL_NAME(fkc.referenced_object_id, fkc.referenced_column_id) AS referenced_column_name
FROM sys.foreign_keys AS fk
INNER JOIN sys.foreign_key_columns AS fkc ON fk.object_id = fkc.constraint_object_id
WHERE OBJECT_NAME(fk.parent_object_id) = 'reports';