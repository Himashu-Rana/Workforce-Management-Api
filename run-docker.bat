@echo off
echo Checking if Docker is installed...
docker -v >NUL 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Docker is installed. Building and running the application...
    docker-compose up --build
) else (
    echo Docker is not installed or not in PATH.
    echo Please install Docker Desktop from: https://www.docker.com/products/docker-desktop/
    pause
)
