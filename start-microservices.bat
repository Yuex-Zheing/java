@echo off
echo ================================================
echo INICIANDO MICROSERVICIOS BANKING
echo ================================================

echo.
echo Iniciando personas-clientes-service en puerto 8081...
start cmd /k "cd personas-clientes-service && mvn clean install spring-boot:run"

echo.
echo Esperando 30 segundos antes de iniciar el segundo servicio...
timeout /t 30 /nobreak > nul

echo.
echo Iniciando cuentas-movimientos-service en puerto 8082...
start cmd /k "cd cuentas-movimientos-service &&mvn clean install spring-boot:run"

echo.
echo ================================================
echo MICROSERVICIOS INICIADOS
echo ================================================
echo Personas-Clientes: http://localhost:8081/swagger-ui.html
echo Cuentas-Movimientos: http://localhost:8082/swagger-ui.html
echo ================================================

pause
