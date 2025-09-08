#!/bin/bash
# Script para eliminar los topics de onboarding si es necesario

# Configuración del broker
KAFKA_BROKER="localhost:9092"

echo "Eliminando topics de Kafka para onboarding..."

docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --delete --topic onboarding-persona
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --delete --topic onboarding-cliente
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --delete --topic onboarding-cuenta
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --delete --topic onboarding-movimiento
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --delete --topic onboarding-rollback
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --bootstrap-server $KAFKA_BROKER --delete --topic onboarding-completed

echo "Topics eliminados exitosamente!"

# Listar topics para verificar
echo "Listando topics existentes:"
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --list --bootstrap-server $KAFKA_BROKER
