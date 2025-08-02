@echo off
echo Starting the Spring Boot application...
call gradlew.bat bootRun
if %ERRORLEVEL% NEQ 0 (
    echo Failed to start the application. Make sure Java 17 is installed.
    pause
)
