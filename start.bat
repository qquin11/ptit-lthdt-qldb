@echo off
chcp 65001 >nul
cd /d "%~dp0"

set JAR=target\ptit-lthdt-qldb-1.0-SNAPSHOT.jar

if not exist "%JAR%" (
    echo Chua co file: %JAR%
    echo Hay build truoc: mvn clean package -DskipTests
    pause
    exit /b 1
)

start "" javaw -jar "%JAR%"
