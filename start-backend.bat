@echo off
title Backend - Stilum Spring Boot (Puerto 8080)
echo ========================================
echo  Iniciando Backend Spring Boot...
echo ========================================

set BACKEND_DIR=%~dp0stilum-backend
set MVN_CMD=

REM 1. Buscar mvn en PATH
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set MVN_CMD=mvn
    goto :found
)

REM 2. Buscar en IntelliJ IDEA 2024 bundled Maven
for /d %%I in ("C:\Program Files\JetBrains\IntelliJ IDEA*") do (
    if exist "%%I\plugins\maven\lib\maven3\bin\mvn.cmd" (
        set MVN_CMD="%%I\plugins\maven\lib\maven3\bin\mvn.cmd"
        goto :found
    )
)

REM 3. Buscar en rutas alternativas de IntelliJ (usuario)
for /d %%I in ("%LOCALAPPDATA%\JetBrains\Toolbox\apps\IDEA-U\*\*") do (
    if exist "%%I\plugins\maven\lib\maven3\bin\mvn.cmd" (
        set MVN_CMD="%%I\plugins\maven\lib\maven3\bin\mvn.cmd"
        goto :found
    )
)
for /d %%I in ("%LOCALAPPDATA%\JetBrains\Toolbox\apps\IDEA-C\*\*") do (
    if exist "%%I\plugins\maven\lib\maven3\bin\mvn.cmd" (
        set MVN_CMD="%%I\plugins\maven\lib\maven3\bin\mvn.cmd"
        goto :found
    )
)

REM 4. Buscar .m2/wrapper
if exist "%USERPROFILE%\.m2\wrapper\dists" (
    for /d %%W in ("%USERPROFILE%\.m2\wrapper\dists\apache-maven*") do (
        for /d %%H in ("%%W\*") do (
            if exist "%%H\apache-maven-*\bin\mvn.cmd" (
                for /d %%M in ("%%H\apache-maven-*") do set MVN_CMD="%%M\bin\mvn.cmd"
                goto :found
            )
        )
    )
)

REM 5. No encontrado
echo.
echo ERROR: Maven no encontrado.
echo Opciones:
echo  a) Instala Maven: https://maven.apache.org/download.cgi
echo     y agrega al PATH
echo  b) Abre el proyecto en IntelliJ IDEA y ejecuta desde ahi
echo     (Run > stilum-backend > Spring Boot)
echo.
pause
exit /b 1

:found
echo Maven encontrado: %MVN_CMD%
echo.
cd /d "%BACKEND_DIR%"
echo Directorio: %CD%
echo.
echo Ejecutando: %MVN_CMD% spring-boot:run
echo (Primera vez puede tardar ~2 min descargando dependencias)
echo Listo cuando aparezca: Started ... in X seconds
echo.
%MVN_CMD% spring-boot:run
pause
