USE bankingdb;

-- Datos de ejemplo para 5 personas
INSERT INTO personas (identificacionpersona, nombres, genero, edad, direccion, telefono, estado) VALUES
('1234567890', 'Juan Carlos Pérez', 'M', 35, 'Av. Principal 123', '0991234567', true),
('0987654321', 'María Elena López', 'F', 28, 'Calle Secundaria 456', '0987654321', true),
('1122334455', 'Pedro José García', 'M', 42, 'Av. Central 789', '0998877665', true),
('2233445566', 'Ana María Rodríguez', 'F', 31, 'Calle 4ta 567', '0994567890', true),
('3344556677', 'Luis Alberto Torres', 'M', 45, 'Av. 5ta 890', '0987654321', true);

-- Datos de ejemplo para clientes (usando los IDs de personas)
INSERT INTO clientes (idpersona, nombreusuario, contrasena, estado)
SELECT idpersona, 
       CONCAT('user', identificacionpersona), 
       CONCAT('pass', identificacionpersona), 
       true
FROM personas;

-- Datos de ejemplo para cuentas (una cuenta de ahorros por cliente)
INSERT INTO cuentas (numerocuenta, idcliente, tipocuenta, saldoinicial, saldodisponible, estado, fechacreacion)
SELECT 
    100000 + ROW_NUMBER() OVER (ORDER BY c.idcliente) as numerocuenta,
    c.idcliente,
    'AHORROS',
    2000.0000 as saldoinicial,
    2000.0000 as saldodisponible,
    true,
    NOW()
FROM clientes c;

-- Datos de movimientos predefinidos (3 por persona)
INSERT INTO movimientos (
    numerocuenta, 
    estado, 
    fechamovimiento, 
    horamovimiento, 
    tipomovimiento, 
    montomovimiento, 
    saldodisponible, 
    movimientodescripcion
)
SELECT m.* FROM (
    -- Cliente 1
    SELECT 100001 as numerocuenta, true as estado, '2025-08-30' as fechamovimiento, '09:00:00' as horamovimiento, 
           'DEPOSITO' as tipomovimiento, 500.0000 as montomovimiento, 0 as saldodisponible, 'Depósito inicial' as movimientodescripcion UNION ALL
    SELECT 100001, true, '2025-08-30', '14:30:00', 'RETIRO', -200.0000, 0, 'Retiro en cajero' UNION ALL
    SELECT 100001, true, '2025-08-31', '10:15:00', 'DEPOSITO', 300.0000, 0, 'Transferencia recibida' UNION ALL
    -- Cliente 2
    SELECT 100002, true, '2025-08-30', '08:45:00', 'DEPOSITO', 1000.0000, 0, 'Depósito de nómina' UNION ALL
    SELECT 100002, true, '2025-08-30', '16:20:00', 'RETIRO', -450.0000, 0, 'Pago de servicios' UNION ALL
    SELECT 100002, true, '2025-08-31', '11:30:00', 'DEPOSITO', 250.0000, 0, 'Depósito en efectivo' UNION ALL
    -- Cliente 3
    SELECT 100003, true, '2025-08-30', '10:30:00', 'DEPOSITO', 800.0000, 0, 'Depósito de cheque' UNION ALL
    SELECT 100003, true, '2025-08-30', '15:45:00', 'RETIRO', -300.0000, 0, 'Compras con tarjeta' UNION ALL
    SELECT 100003, true, '2025-08-31', '09:20:00', 'DEPOSITO', 400.0000, 0, 'Transferencia recibida' UNION ALL
    -- Cliente 4
    SELECT 100004, true, '2025-08-30', '11:15:00', 'DEPOSITO', 1500.0000, 0, 'Depósito inicial' UNION ALL
    SELECT 100004, true, '2025-08-30', '17:00:00', 'RETIRO', -600.0000, 0, 'Retiro en ventanilla' UNION ALL
    SELECT 100004, true, '2025-08-31', '12:45:00', 'DEPOSITO', 350.0000, 0, 'Depósito en efectivo' UNION ALL
    -- Cliente 5
    SELECT 100005, true, '2025-08-30', '09:45:00', 'DEPOSITO', 700.0000, 0, 'Depósito inicial' UNION ALL
    SELECT 100005, true, '2025-08-30', '16:30:00', 'RETIRO', -250.0000, 0, 'Retiro en cajero' UNION ALL
    SELECT 100005, true, '2025-08-31', '10:45:00', 'DEPOSITO', 450.0000, 0, 'Transferencia recibida'
) m
ORDER BY m.numerocuenta, m.fechamovimiento, m.horamovimiento;

-- Actualizar saldos disponibles
SET SQL_SAFE_UPDATES = 0;

-- Actualizar saldos en movimientos secuencialmente
WITH RECURSIVE saldos_acumulados AS (
    -- Primera fila por cuenta: saldo inicial + primer movimiento
    SELECT 
        m.idmovimiento,
        m.numerocuenta,
        m.montomovimiento,
        m.fechamovimiento,
        m.horamovimiento,
        c.saldoinicial + m.montomovimiento as saldodisponible,
        1 as nivel
    FROM movimientos m
    JOIN cuentas c ON m.numerocuenta = c.numerocuenta
    WHERE (m.fechamovimiento, m.horamovimiento) = (
        SELECT MIN(m2.fechamovimiento), MIN(m2.horamovimiento)
        FROM movimientos m2
        WHERE m2.numerocuenta = m.numerocuenta
    )
    
    UNION ALL
    
    -- Filas subsiguientes: saldo anterior + movimiento actual
    SELECT 
        m.idmovimiento,
        m.numerocuenta,
        m.montomovimiento,
        m.fechamovimiento,
        m.horamovimiento,
        sa.saldodisponible + m.montomovimiento,
        sa.nivel + 1
    FROM saldos_acumulados sa
    JOIN movimientos m ON m.numerocuenta = sa.numerocuenta
    WHERE (m.fechamovimiento > sa.fechamovimiento) 
       OR (m.fechamovimiento = sa.fechamovimiento AND m.horamovimiento > sa.horamovimiento)
)
UPDATE movimientos m
JOIN saldos_acumulados sa ON m.idmovimiento = sa.idmovimiento
SET m.saldodisponible = sa.saldodisponible;

-- Actualizar saldo disponible en cuentas
-- Este saldo refleja la suma del saldo inicial más todos los movimientos
UPDATE cuentas c
SET c.saldodisponible = (
    SELECT c.saldoinicial + COALESCE(SUM(m.montomovimiento), 0)
    FROM movimientos m
    WHERE m.numerocuenta = c.numerocuenta
    GROUP BY m.numerocuenta
);

SET SQL_SAFE_UPDATES = 1;

-- Limpiar tabla temporal
DROP TEMPORARY TABLE IF EXISTS temp_movimientos;
