@echo off
title PostgreSQL - Stilum
echo ========================================
echo  Iniciando servicio PostgreSQL...
echo ========================================

REM Intentar iniciar el servicio de PostgreSQL (nombre tipico en Windows)
net start postgresql-x64-16 2>nul
if %ERRORLEVEL% EQU 0 goto :success

net start postgresql-x64-15 2>nul
if %ERRORLEVEL% EQU 0 goto :success

net start postgresql-x64-14 2>nul
if %ERRORLEVEL% EQU 0 goto :success

net start "postgresql-x64-16" 2>nul
if %ERRORLEVEL% EQU 0 goto :success

REM Buscar psql en rutas comunes
set PSQL_PATH=
if exist "C:\Program Files\PostgreSQL\16\bin\psql.exe" set PSQL_PATH=C:\Program Files\PostgreSQL\16\bin
if exist "C:\Program Files\PostgreSQL\15\bin\psql.exe" set PSQL_PATH=C:\Program Files\PostgreSQL\15\bin
if exist "C:\Program Files\PostgreSQL\14\bin\psql.exe" set PSQL_PATH=C:\Program Files\PostgreSQL\14\bin

if "%PSQL_PATH%"=="" goto :no_pg

echo Encontrado PostgreSQL en: %PSQL_PATH%
echo Iniciando servicio...
sc query | findstr /i "postgresql"
sc start postgresql-x64-16

:success
echo.
echo PostgreSQL corriendo en localhost:5432
echo.
echo Creando base de datos si no existe...
if exist "C:\Program Files\PostgreSQL\16\bin\psql.exe" (
    "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -tc "SELECT 1 FROM pg_database WHERE datname='stilum_citas'" | findstr /c:"1" >nul 2>&1
    if errorlevel 1 (
        "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -c "CREATE USER stilum WITH PASSWORD 'stilum_dev_pass';" 2>nul
        "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -c "CREATE DATABASE stilum_citas OWNER stilum;" 2>nul
        "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE stilum_citas TO stilum;" 2>nul
        echo Base de datos stilum_citas creada.
    ) else (
        echo Base de datos stilum_citas ya existe.
    )
)
goto :end

:no_pg
echo.
echo *** PostgreSQL no encontrado ***
echo Verifica que PostgreSQL este instalado en Program Files
echo o instala Docker Desktop desde https://docker.com
echo.

:end
pause
