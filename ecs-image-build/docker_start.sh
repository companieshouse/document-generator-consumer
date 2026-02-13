#!/bin/bash
#
#
# Start script for document-generator-consumer

PORT=8080
exec java -jar -Dserver.port="${PORT}" -XX:MaxRAMPercentage=80 "document-generator-consumer.jar"
