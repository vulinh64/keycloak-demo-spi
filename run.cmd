@echo off
set DOCKER_COMPOSE_FILE=docker-compose.yaml
set IMAGE_NAME=keycloak-custom-spi-keycloak

call mvn clean install

if %ERRORLEVEL% neq 0 (
    echo Maven build failed with exit code %ERRORLEVEL%.
    exit /b %ERRORLEVEL%
)

docker-compose -f %DOCKER_COMPOSE_FILE% down

docker images -q %IMAGE_NAME% >nul 2>nul
if %errorlevel% equ 0 (
    docker rmi %IMAGE_NAME%
)

docker-compose -f %DOCKER_COMPOSE_FILE% up --detach

pause