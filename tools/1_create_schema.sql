-- Crear la base de datos
DROP DATABASE IF EXISTS bankingdb;
CREATE DATABASE IF NOT EXISTS bankingdb;
USE bankingdb;

-- Tabla Personas
CREATE TABLE personas (
    idpersona BIGINT AUTO_INCREMENT PRIMARY KEY,
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
    idcliente BIGINT AUTO_INCREMENT PRIMARY KEY,
    idpersona BIGINT NOT NULL,
    nombreusuario VARCHAR(50) NOT NULL UNIQUE,
    contrasena VARCHAR(100) NOT NULL,
    estado BOOLEAN DEFAULT true,
    FOREIGN KEY (idpersona) REFERENCES personas(idpersona)
);

-- Tabla Cuentas
CREATE TABLE cuentas (
    numerocuenta INT PRIMARY KEY,
    idcliente BIGINT NOT NULL,
    tipocuenta ENUM('AHORROS', 'CORRIENTE') NOT NULL,
    saldoinicial DECIMAL(10,4) NOT NULL,
    saldodisponible DECIMAL(10,4),
    estado BOOLEAN DEFAULT true,
    fechacreacion DATETIME NOT NULL,
    fechacierre DATETIME,
    FOREIGN KEY (idcliente) REFERENCES clientes(idcliente)
);

-- Tabla Movimientos
CREATE TABLE movimientos (
    idmovimiento BIGINT AUTO_INCREMENT PRIMARY KEY,
    numerocuenta INT NOT NULL,
    estado BOOLEAN DEFAULT true,
    fechamovimiento DATE NOT NULL,
    horamovimiento TIME(3) NOT NULL,
    tipomovimiento ENUM('RETIRO', 'DEPOSITO') NOT NULL,
    montomovimiento DECIMAL(10,4) NOT NULL,
    saldodisponible DECIMAL(10,4) NOT NULL,
    movimientodescripcion VARCHAR(300),
    FOREIGN KEY (numerocuenta) REFERENCES cuentas(numerocuenta)
);
