#!/usr/bin/env bash
set -e
docker compose up -d mysql
cd frontend
npm install
npm run build
cd ..
mvn clean package -DskipTests
exec java -jar target/secondhand-market-1.0.0.jar
