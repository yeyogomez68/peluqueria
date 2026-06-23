@echo off
title Configurar BD - Stilum (Puerto 5433)
echo ========================================
echo  Configurando base de datos Stilum...
echo  PostgreSQL 18 en localhost:5433
echo ========================================

set PSQL="C:\Program Files\PostgreSQL\18\bin\psql.exe"
set PGPASSWORD=postgres

echo.
echo >> Creando usuario stilum (contrasena: stilum_dev_pass)...
%PSQL% -U postgres -p 5433 -c "DO $$ BEGIN IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'stilum') THEN CREATE USER stilum WITH PASSWORD 'stilum_dev_pass'; END IF; END $$;"

echo.
echo >> Creando base de datos stilum_citas...
%PSQL% -U postgres -p 5433 -c "CREATE DATABASE stilum_citas OWNER stilum;" 2>nul
if %ERRORLEVEL% NEQ 0 echo (La BD puede ya existir - continuando...)

echo.
echo >> Otorgando permisos...
%PSQL% -U postgres -p 5433 -c "GRANT ALL PRIVILEGES ON DATABASE stilum_citas TO stilum;"

echo.
echo >> Verificando conexion como stilum...
set PGPASSWORD=stilum_dev_pass
%PSQL% -U stilum -p 5433 -d stilum_citas -c "SELECT version();" -w

echo.
echo ========================================
if %ERRORLEVEL% EQU 0 (
    echo  [OK] BASE DE DATOS LISTA - puedes arrancar el backend
) else (
    echo  [ERROR] Revisa la salida arriba
    echo  Si la contrasena de postgres no es 'postgres',
    echo  edita este archivo y cambia PGPASSWORD=TU_PASSWORD
)
echo ========================================
pause
