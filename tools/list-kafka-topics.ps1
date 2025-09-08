# Script PowerShell para listar todos los topics de Kafka

# Configuración del broker
$KAFKA_BROKER = "localhost:9092"

Write-Host "Listando todos los topics de Kafka:" -ForegroundColor Yellow
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --list --bootstrap-server $KAFKA_BROKER
