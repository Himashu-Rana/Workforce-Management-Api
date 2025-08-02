@echo off
echo Checking if Maven is installed...
mvn -v 2>NUL
if %ERRORLEVEL% EQU 0 (
    echo Maven is installed. Running Maven build...
    mvn clean package
    if %ERRORLEVEL% EQU 0 (
        echo Starting the application...
        mvn spring-boot:run
    ) else (
        echo Maven build failed.
    )
) else (
    echo Maven is not installed or not in PATH.
    echo Please install Maven or add it to your PATH.
    echo You can download Maven from: https://maven.apache.org/download.cgi
    pause
)
