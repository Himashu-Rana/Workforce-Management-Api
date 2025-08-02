@echo off
echo Compiling the application...
if not exist ".\target\classes\" mkdir .\target\classes\

echo Setting up classpath...
set CLASSPATH=.\target\classes;.\lib\*

echo Downloading Spring Boot dependencies...
mkdir lib
curl -o lib\spring-boot-starter-web-3.0.4.jar https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-starter-web/3.0.4/spring-boot-starter-web-3.0.4.jar
curl -o lib\lombok-1.18.26.jar https://repo1.maven.org/maven2/org/projectlombok/lombok/1.18.26/lombok-1.18.26.jar
curl -o lib\mapstruct-1.5.3.Final.jar https://repo1.maven.org/maven2/org/mapstruct/mapstruct/1.5.3.Final/mapstruct-1.5.3.Final.jar

echo Compiling Java files...
javac -d .\target\classes -cp %CLASSPATH% src\main\java\com\railse\hiring\workforcemgmt\WorkforcemgmtApplication.java

echo Starting the application...
java -cp %CLASSPATH% com.railse.hiring.workforcemgmt.WorkforcemgmtApplication

pause
