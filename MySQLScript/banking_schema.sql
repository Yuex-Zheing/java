-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS bankingdb;
USE bankingdb;

-- Tabla Personas
CREATE TABLE personas (
    idpersona INT(8) AUTO_INCREMENT PRIMARY KEY,
    identificacionpersona VARCHAR(10) NOT NULL UNIQUE,
    nombres VARCHAR(150) NOT NULL,
    genero CHAR(1) NOT NULL,
    edad INT NOT NULL,
    direccion VARCHAR(300),
    telefono VARCHAR(15),
    estado BOOLEAN DEFAULT true
);

-- Tabla Clientes
CREATE TABLE clientes (
    idcliente INT(8) AUTO_INCREMENT PRIMARY KEY,
    idpersona INT(8) NOT NULL,
    nombreusuario VARCHAR(50) NOT NULL UNIQUE,
    contrasena VARCHAR(100) NOT NULL,
    estado BOOLEAN DEFAULT true,
    FOREIGN KEY (idpersona) REFERENCES personas(idpersona)
);

-- Tabla Cuentas
CREATE TABLE cuentas (
    numerocuenta INT(6) PRIMARY KEY,
    idcliente INT(8) NOT NULL,
    tipocuenta ENUM('AHORROS', 'CORRIENTE') NOT NULL,
    saldoinicial DECIMAL(10,4) NOT NULL,
    estado BOOLEAN DEFAULT true,
    fechacreacion DATETIME NOT NULL,
    fechacierre DATETIME,
    FOREIGN KEY (idcliente) REFERENCES clientes(idcliente)
);

-- Tabla Movimientos
CREATE TABLE movimientos (
    idmovimiento INT(8) AUTO_INCREMENT PRIMARY KEY,
    numerocuenta INT(6) NOT NULL,
    estado BOOLEAN DEFAULT true,
    fechamovimiento DATE NOT NULL,
    horamovimiento TIME(3) NOT NULL,
    tipomovimiento ENUM('RETIRO', 'DEPOSITO') NOT NULL,
    montomovimiento DECIMAL(10,4) NOT NULL,
    saldodisponible DECIMAL(10,4) NOT NULL,
    movimientodescripcion VARCHAR(300),
    FOREIGN KEY (numerocuenta) REFERENCES cuentas(numerocuenta)
);

-- Datos de ejemplo

-- Insertar personas
INSERT INTO personas (identificacionpersona, nombres, genero, edad, direccion, telefono, estado) VALUES
('1234567890', 'Juan Carlos Pérez', 'M', 35, 'Av. Principal 123', '0991234567', true),
('0987654321', 'María Elena López', 'F', 28, 'Calle Secundaria 456', '0987654321', true),
('1122334455', 'Pedro José García', 'M', 42, 'Av. Central 789', '0998877665', true);

-- Insertar clientes
INSERT INTO clientes (idpersona, nombreusuario, contrasena, estado) VALUES
(1, 'jperez', 'pass123', true),
(2, 'mlopez', 'pass456', true),
(3, 'pgarcia', 'pass789', true);

-- Insertar cuentas
INSERT INTO cuentas (numerocuenta, idcliente, tipocuenta, saldoinicial, estado, fechacreacion) VALUES
(100001, 1, 'AHORROS', 1000.0000, true, NOW()),
(100002, 1, 'CORRIENTE', 2500.0000, true, NOW()),
(100003, 2, 'AHORROS', 500.0000, true, NOW()),
(100004, 3, 'AHORROS', 1500.0000, true, NOW());

-- Insertar movimientos
INSERT INTO movimientos (numerocuenta, estado, fechamovimiento, horamovimiento, tipomovimiento, montomovimiento, saldodisponible, movimientodescripcion) VALUES
(100001, true, CURDATE(), CURTIME(3), 'DEPOSITO', 500.0000, 1500.0000, 'Depósito en efectivo por 500.0000'),
(100001, true, CURDATE(), CURTIME(3), 'RETIRO', 200.0000, 1300.0000, 'Retiro en efectivo por 200.0000'),
(100002, true, CURDATE(), CURTIME(3), 'DEPOSITO', 1000.0000, 3500.0000, 'Depósito en efectivo por 1000.0000'),
(100003, true, CURDATE(), CURTIME(3), 'RETIRO', 100.0000, 400.0000, 'Retiro en efectivo por 100.0000');
