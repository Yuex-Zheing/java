USE bankingdb;

-- Datos de ejemplo para personas
INSERT INTO personas (identificacionpersona, nombres, genero, edad, direccion, telefono, estado) VALUES
('1234567890', 'Juan Carlos Pérez', 'M', 35, 'Av. Principal 123', '0991234567', true),
('0987654321', 'María Elena López', 'F', 28, 'Calle Secundaria 456', '0987654321', true),
('1122334455', 'Pedro José García', 'M', 42, 'Av. Central 789', '0998877665', true),
('2233445566', 'Ana María Rodríguez', 'F', 31, 'Calle 4ta 567', '0994567890', true),
('3344556677', 'Luis Alberto Torres', 'M', 45, 'Av. 5ta 890', '0987654321', true),
('4455667788', 'Carmen Patricia Mora', 'F', 38, 'Calle 6ta 123', '0991234567', true),
('5566778899', 'Roberto Carlos Paz', 'M', 29, 'Av. 7ma 456', '0998877665', true),
('6677889900', 'Diana Elizabeth Cruz', 'F', 33, 'Calle 8va 789', '0994567890', true),
('7788990011', 'Francisco Javier Lima', 'M', 40, 'Av. 9na 012', '0987654321', true),
('8899001122', 'Sandra Patricia Vega', 'F', 36, 'Calle 10ma 345', '0991234567', true);

-- Datos de ejemplo para clientes (usando los IDs de personas)
INSERT INTO clientes (idpersona, nombreusuario, contrasena, estado)
SELECT idpersona, CONCAT('user', identificacionpersona), CONCAT('pass', identificacionpersona), true
FROM personas;

-- Datos de ejemplo para cuentas (dos por cliente: ahorro y corriente)
INSERT INTO cuentas (numerocuenta, idcliente, tipocuenta, saldoinicial, saldodisponible, estado, fechacreacion)
SELECT 
    100000 + ROW_NUMBER() OVER (ORDER BY c.idcliente) as numerocuenta,
    c.idcliente,
    'AHORROS',
    1000.0000 * (ROW_NUMBER() OVER (ORDER BY c.idcliente)) as saldoinicial,
    1000.0000 * (ROW_NUMBER() OVER (ORDER BY c.idcliente)) as saldodisponible,
    true,
    NOW()
FROM clientes c
UNION ALL
SELECT 
    200000 + ROW_NUMBER() OVER (ORDER BY c.idcliente) as numerocuenta,
    c.idcliente,
    'CORRIENTE',
    2000.0000 * (ROW_NUMBER() OVER (ORDER BY c.idcliente)) as saldoinicial,
    2000.0000 * (ROW_NUMBER() OVER (ORDER BY c.idcliente)) as saldodisponible,
    true,
    NOW()
FROM clientes c;

-- Crear tabla temporal para los tipos de movimientos
DROP TEMPORARY TABLE IF EXISTS temp_movimientos;
CREATE TEMPORARY TABLE temp_movimientos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('RETIRO', 'DEPOSITO'),
    descripcion VARCHAR(300)
);

-- Insertar tipos de movimientos base
INSERT INTO temp_movimientos (tipo, descripcion) VALUES
('DEPOSITO', 'Depósito en efectivo'),
('RETIRO', 'Retiro en cajero'),
('DEPOSITO', 'Transferencia recibida'),
('RETIRO', 'Pago de servicios'),
('DEPOSITO', 'Depósito de cheque'),
('RETIRO', 'Compras con tarjeta'),
('DEPOSITO', 'Pago de nómina'),
('RETIRO', 'Retiro en ventanilla'),
('DEPOSITO', 'Reembolso'),
('RETIRO', 'Pago de préstamo');

-- Generar 20 movimientos para cada cuenta
INSERT INTO movimientos (numerocuenta, estado, fechamovimiento, horamovimiento, tipomovimiento, montomovimiento, saldodisponible, movimientodescripcion)
WITH RECURSIVE mov_series AS (
    SELECT 1 as mov_num
    UNION ALL
    SELECT mov_num + 1 FROM mov_series WHERE mov_num < 20
),
cuenta_saldos AS (
    SELECT numerocuenta, saldoinicial FROM cuentas
)
SELECT 
    c.numerocuenta,
    true,
    DATE_SUB(CURRENT_DATE, INTERVAL ms.mov_num DAY),
    TIME_FORMAT(SEC_TO_TIME(28800 + (ms.mov_num * 1800)), '%H:%i:%s'),
    tm.tipo,
    CASE 
        WHEN tm.tipo = 'DEPOSITO' THEN ROUND(RAND() * 1000 + 100, 4)
        ELSE ROUND(RAND() * -500 - 100, 4)
    END as montomovimiento,
    0 as saldodisponible,  -- Se actualizará después
    CONCAT(tm.descripcion, ' #', ms.mov_num) as movimientodescripcion
FROM cuenta_saldos c
CROSS JOIN mov_series ms
CROSS JOIN temp_movimientos tm
WHERE ms.mov_num <= 20
ORDER BY c.numerocuenta, ms.mov_num;

-- Actualizar saldos disponibles
SET SQL_SAFE_UPDATES = 0;

-- Actualizar saldos en movimientos y cuenta
WITH RECURSIVE saldos_acumulados AS (
    SELECT 
        m.idmovimiento,
        m.numerocuenta,
        m.montomovimiento,
        m.fechamovimiento,
        m.horamovimiento,
        ROW_NUMBER() OVER (PARTITION BY m.numerocuenta ORDER BY m.fechamovimiento, m.horamovimiento, m.idmovimiento) as orden,
        c.saldoinicial as saldo_base
    FROM movimientos m
    JOIN cuentas c ON m.numerocuenta = c.numerocuenta
)
UPDATE movimientos m
INNER JOIN (
    SELECT 
        sa1.idmovimiento,
        sa1.numerocuenta,
        sa1.saldo_base + COALESCE(
            (
                SELECT SUM(sa2.montomovimiento)
                FROM saldos_acumulados sa2
                WHERE sa2.numerocuenta = sa1.numerocuenta
                AND sa2.orden <= sa1.orden
            ),
            0
        ) as saldo_final
    FROM saldos_acumulados sa1
) calc ON m.idmovimiento = calc.idmovimiento
SET m.saldodisponible = calc.saldo_final;

-- Actualizar saldo disponible en cuentas con el último saldo calculado
UPDATE cuentas c
INNER JOIN (
    SELECT 
        numerocuenta,
        saldodisponible as ultimo_saldo
    FROM movimientos m1
    WHERE fechamovimiento = (
        SELECT MAX(fechamovimiento)
        FROM movimientos m2
        WHERE m2.numerocuenta = m1.numerocuenta
    )
    AND horamovimiento = (
        SELECT MAX(horamovimiento)
        FROM movimientos m3
        WHERE m3.numerocuenta = m1.numerocuenta
        AND m3.fechamovimiento = m1.fechamovimiento
    )
) ultimos_movimientos ON c.numerocuenta = ultimos_movimientos.numerocuenta
SET c.saldodisponible = ultimos_movimientos.ultimo_saldo;

SET SQL_SAFE_UPDATES = 1;

-- Limpiar tabla temporal
DROP TEMPORARY TABLE IF EXISTS temp_movimientos;
