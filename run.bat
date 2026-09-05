@echo off
docker compose up -d mysql
cd frontend
call npm install
call npm run build
cd ..
call mvn clean package -DskipTests
java -jar target\secondhand-market-1.0.0.jar
