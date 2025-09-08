#!/bin/bash
# Script para listar todos los topics de Kafka

# Configuración del broker
KAFKA_BROKER="localhost:9092"

echo "Listando todos los topics de Kafka:"
docker exec --workdir /opt/kafka/bin/ -it broker sh kafka-topics.sh --list --bootstrap-server $KAFKA_BROKER
