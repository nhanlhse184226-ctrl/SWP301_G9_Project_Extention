-- Script để kiểm tra và sửa tất cả references đến bảng 'dbo.pin' cũ trong database
-- Chạy script này để tìm và sửa tất cả dependencies

-- 1. Kiểm tra các Foreign Key Constraints
PRINT '=== CHECKING FOREIGN KEY CONSTRAINTS ===';
SELECT 
    fk.name AS FK_Name,
    tp.name AS Parent_Table,
    cp.name AS Parent_Column,
    tr.name AS Referenced_Table,
    cr.name AS Referenced_Column
FROM sys.foreign_keys fk
    INNER JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id
    INNER JOIN sys.tables tp ON tp.object_id = fkc.parent_object_id
    INNER JOIN sys.columns cp ON cp.object_id = fkc.parent_object_id AND cp.column_id = fkc.parent_column_id
    INNER JOIN sys.tables tr ON tr.object_id = fkc.referenced_object_id
    INNER JOIN sys.columns cr ON cr.object_id = fkc.referenced_object_id AND cr.column_id = fkc.referenced_column_id
WHERE tr.name = 'pin' OR tp.name = 'pin';

-- 2. Kiểm tra các Triggers
PRINT '=== CHECKING TRIGGERS ===';
SELECT 
    t.name AS Trigger_Name,
    tab.name AS Table_Name,
    t.type_desc AS Trigger_Type
FROM sys.triggers t
    INNER JOIN sys.tables tab ON tab.object_id = t.parent_id
WHERE tab.name = 'pin' OR tab.name = 'pinStation' OR tab.name = 'pinslot';

-- 3. Kiểm tra các Views
PRINT '=== CHECKING VIEWS ===';
SELECT 
    v.name AS View_Name,
    m.definition AS View_Definition
FROM sys.views v
    INNER JOIN sys.sql_modules m ON m.object_id = v.object_id
WHERE m.definition LIKE '%dbo.pin%' AND m.definition NOT LIKE '%dbo.pinStation%' AND m.definition NOT LIKE '%dbo.pinslot%';

-- 4. Kiểm tra các Stored Procedures
PRINT '=== CHECKING STORED PROCEDURES ===';
SELECT 
    p.name AS Procedure_Name,
    m.definition AS Procedure_Definition
FROM sys.procedures p
    INNER JOIN sys.sql_modules m ON m.object_id = p.object_id
WHERE m.definition LIKE '%dbo.pin%' AND m.definition NOT LIKE '%dbo.pinStation%' AND m.definition NOT LIKE '%dbo.pinslot%';

-- 5. Kiểm tra các Functions
PRINT '=== CHECKING FUNCTIONS ===';
SELECT 
    f.name AS Function_Name,
    m.definition AS Function_Definition
FROM sys.objects f
    INNER JOIN sys.sql_modules m ON m.object_id = f.object_id
WHERE f.type IN ('FN', 'IF', 'TF') 
    AND m.definition LIKE '%dbo.pin%' 
    AND m.definition NOT LIKE '%dbo.pinStation%' 
    AND m.definition NOT LIKE '%dbo.pinslot%';

-- 6. Kiểm tra xem bảng 'pin' cũ có tồn tại không
PRINT '=== CHECKING IF OLD TABLE EXISTS ===';
IF OBJECT_ID('dbo.pin', 'U') IS NOT NULL
BEGIN
    PRINT 'WARNING: Old table dbo.pin still exists!';
    PRINT 'Table schema:';
    SELECT 
        c.name AS Column_Name,
        t.name AS Data_Type,
        c.max_length,
        c.is_nullable
    FROM sys.columns c
        INNER JOIN sys.types t ON c.user_type_id = t.user_type_id
    WHERE c.object_id = OBJECT_ID('dbo.pin');
END
ELSE
BEGIN
    PRINT 'Good: Old table dbo.pin does not exist.';
END

-- 7. Kiểm tra bảng pinslot mới
PRINT '=== CHECKING NEW TABLE ===';
IF OBJECT_ID('dbo.pinslot', 'U') IS NOT NULL
BEGIN
    PRINT 'Good: New table dbo.pinslot exists.';
END
ELSE
BEGIN
    PRINT 'ERROR: New table dbo.pinslot does not exist!';
END

-- 8. Đề xuất các câu lệnh để sửa
PRINT '=== SUGGESTED FIX COMMANDS ===';
PRINT 'If any foreign keys reference old table, use:';
PRINT 'ALTER TABLE [referencing_table] DROP CONSTRAINT [constraint_name];';
PRINT 'ALTER TABLE [referencing_table] ADD CONSTRAINT [new_constraint_name] FOREIGN KEY ([column]) REFERENCES dbo.pinslot([column]);';
PRINT '';
PRINT 'If any triggers reference old table, drop and recreate them with new table reference.';
PRINT '';
PRINT 'Run the update_stored_procedures.sql script to fix stored procedures.';