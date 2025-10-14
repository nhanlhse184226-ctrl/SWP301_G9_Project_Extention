-- Create Vehicle table for pin swapping functionality
-- Table structure matches VehicleDTO requirements and database schema
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Vehicle' AND xtype='U')
BEGIN
    CREATE TABLE Vehicle (
        vehicleID int IDENTITY(1,1) PRIMARY KEY,
        userID int NOT NULL,
        licensePlate nvarchar(20) NOT NULL UNIQUE,
        vehicleType nvarchar(50) NOT NULL,
        pinPercent int NOT NULL DEFAULT 0 CHECK (pinPercent >= 0 AND pinPercent <= 100),
        pinHealth int NOT NULL DEFAULT 100 CHECK (pinHealth >= 0 AND pinHealth <= 100),
        FOREIGN KEY (userID) REFERENCES users(userID)
    );
    
    PRINT 'Vehicle table created successfully';
END
ELSE
BEGIN
    PRINT 'Vehicle table already exists';
END

-- Insert sample vehicle data for testing pin swap functionality
-- SOH (State of Health) = pinHealth, SOC (State of Charge) = pinPercent
IF NOT EXISTS (SELECT * FROM Vehicle WHERE licensePlate = '30A-12345')
BEGIN
    INSERT INTO Vehicle (userID, licensePlate, vehicleType, pinPercent, pinHealth) VALUES
    (1, '30A-12345', 'Tesla Model 3', 45, 85),     -- Vehicle 1: 45% charge, 85% health
    (2, '51G-67890', 'VinFast VF8', 78, 92),       -- Vehicle 2: 78% charge, 92% health  
    (1, '29B-24680', 'BMW i3', 23, 76),            -- Vehicle 3: 23% charge, 76% health
    (3, '60A-13579', 'Hyundai Kona Electric', 89, 88); -- Vehicle 4: 89% charge, 88% health
    
    PRINT 'Sample vehicle data inserted successfully';
END
ELSE
BEGIN
    PRINT 'Sample vehicle data already exists';
END

-- Verify the data
SELECT vehicleID, userID, licensePlate, vehicleType, pinPercent, pinHealth FROM Vehicle;
PRINT 'Vehicle table verification complete';