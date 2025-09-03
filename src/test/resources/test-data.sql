-- Limpiar datos existentes
DELETE FROM movimientos;
DELETE FROM cuentas;
DELETE FROM clientes;
DELETE FROM personas;

-- Reiniciar secuencias
ALTER TABLE personas AUTO_INCREMENT = 1;
ALTER TABLE clientes AUTO_INCREMENT = 1;
ALTER TABLE movimientos AUTO_INCREMENT = 1;

-- Insertar datos de prueba
INSERT INTO personas (identificacionpersona, nombres, genero, edad, direccion, telefono, estado)
VALUES ('1234567890', 'Test User', 'M', 30, 'Test Address', '0987654321', true);

INSERT INTO clientes (idpersona, nombreusuario, contrasena, estado)
VALUES (1, 'testuser', 'testpass', true);

INSERT INTO cuentas (numerocuenta, idcliente, tipocuenta, saldoinicial, saldodisponible, estado, fechacreacion)
VALUES (100001, 1, 'AHORROS', 1000.00, 1000.00, true, NOW());
