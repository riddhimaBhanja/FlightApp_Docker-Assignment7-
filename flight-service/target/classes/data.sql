SET @flight_status = 'ACTIVE';

INSERT IGNORE INTO flight_inventory (flight_number, airline, origin, destination, departure_time, arrival_time, available_seats, price, status)
VALUES
('AI101', 'Air India', 'DEL', 'BOM', '2025-12-15 08:00:00', '2025-12-15 10:30:00', 150, 5000.00, @flight_status),
('6E202', 'IndiGo', 'DEL', 'BOM', '2025-12-15 14:00:00', '2025-12-15 16:30:00', 180, 4500.00, @flight_status),
('SG303', 'SpiceJet', 'BOM', 'DEL', '2025-12-15 09:00:00', '2025-12-15 11:30:00', 120, 4200.00, @flight_status),
('AI404', 'Air India', 'DEL', 'BLR', '2025-12-16 07:00:00', '2025-12-16 10:00:00', 200, 6000.00, @flight_status),
('6E505', 'IndiGo', 'BLR', 'DEL', '2025-12-16 15:00:00', '2025-12-16 18:00:00', 175, 5800.00, @flight_status);
