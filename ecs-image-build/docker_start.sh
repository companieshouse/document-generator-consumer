#!/bin/bash
#
# Start script for document-generator-consumer


PORT=8080
exec java -jar -Dserver.port="${PORT}" "document-generator-consumer.jar"
