#!/bin/bash
# Script para crear los topics necesarios para el flujo de onboarding

# Configuración del broker
KAFKA_BROKER="localhost:9092"

# Crear topics con 3 particiones y factor de replicación 1
echo "Creando topics de Kafka para onboarding..."

docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --create --topic onboarding-persona --partitions 3 --replication-factor 1
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --create --topic onboarding-cliente --partitions 3 --replication-factor 1
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --create --topic onboarding-cuenta --partitions 3 --replication-factor 1
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --create --topic onboarding-movimiento --partitions 3 --replication-factor 1
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --create --topic onboarding-rollback --partitions 3 --replication-factor 1
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --create --topic onboarding-completed --partitions 3 --replication-factor 1

echo "Topics creados exitosamente!"

# Listar topics para verificar
echo "Listando topics existentes:"
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --list --bootstrap-server $KAFKA_BROKER
