# Script PowerShell para eliminar los topics de onboarding si es necesario

# Configuración del broker
$KAFKA_BROKER = "localhost:9092"

Write-Host "Eliminando topics de Kafka para onboarding..." -ForegroundColor Red

docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --delete --topic onboarding-persona
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --delete --topic onboarding-cliente
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --delete --topic onboarding-cuenta
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --delete --topic onboarding-movimiento
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --delete --topic onboarding-rollback
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --delete --topic onboarding-completed

Write-Host "Topics eliminados exitosamente!" -ForegroundColor Green

# Listar topics para verificar
Write-Host "Listando topics existentes:" -ForegroundColor Yellow
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --list --bootstrap-server $KAFKA_BROKER
