@echo off
title ADMIN - Reset PostgreSQL Password
:: Este archivo se abre solo como administrador
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo Solicitando permisos de administrador...
    powershell -Command "Start-Process '%~f0' -Verb RunAs"
    exit /b
)

powershell -ExecutionPolicy Bypass -File "%~dp0reset-postgres-password.ps1"
