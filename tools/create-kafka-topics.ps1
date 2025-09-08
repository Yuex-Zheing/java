# Script PowerShell para crear los topics necesarios para el flujo de onboarding

# Configuración del broker
$KAFKA_BROKER = "localhost:9092"

Write-Host "Creando topics de Kafka para onboarding..." -ForegroundColor Green

# Crear topics con 3 particiones y factor de replicación 1
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --create --topic onboarding-persona --partitions 3 --replication-factor 1
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --create --topic onboarding-cliente --partitions 3 --replication-factor 1
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --create --topic onboarding-cuenta --partitions 3 --replication-factor 1
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --create --topic onboarding-movimiento --partitions 3 --replication-factor 1
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --create --topic onboarding-rollback --partitions 3 --replication-factor 1
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --create --topic onboarding-completed --partitions 3 --replication-factor 1

Write-Host "Topics creados exitosamente!" -ForegroundColor Green

# Listar topics para verificar
Write-Host "Listando topics existentes:" -ForegroundColor Yellow
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --list --bootstrap-server $KAFKA_BROKER
