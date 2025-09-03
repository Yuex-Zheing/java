-- Garantizar acceso remoto para el usuario root
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'Pa$$w0rd';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;

-- Garantizar acceso remoto para el usuario zheing
CREATE USER IF NOT EXISTS 'zheing'@'%' IDENTIFIED WITH mysql_native_password BY 'Pa$$w0rd';
GRANT ALL PRIVILEGES ON bankingdb.* TO 'zheing'@'%';

-- Aplicar los cambios
FLUSH PRIVILEGES;
