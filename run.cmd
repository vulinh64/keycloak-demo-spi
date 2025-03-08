@echo off
set DOCKER_COMPOSE_FILE=docker-compose.yaml
set IMAGE_NAME=customized-keycloak

call mvn clean install

if %ERRORLEVEL% neq 0 (
    echo Maven build failed with exit code %ERRORLEVEL%.
    exit /b %ERRORLEVEL%
)

docker-compose -f %DOCKER_COMPOSE_FILE% down

docker rmi %IMAGE_NAME%

docker-compose -f %DOCKER_COMPOSE_FILE% up --detach

pause