@echo off
title Buscando Maven...
echo Buscando mvn.cmd en rutas comunes...
echo.

for %%D in (C D E) do (
    if exist "%%D:\Program Files\Apache Maven" (
        echo ENCONTRADO en %%D:\Program Files\Apache Maven
        dir "%%D:\Program Files\Apache Maven" /b
    )
    if exist "%%D:\maven" echo ENCONTRADO en %%D:\maven
    if exist "%%D:\tools\maven" echo ENCONTRADO en %%D:\tools\maven
    if exist "%%D:\dev\tools\maven" echo ENCONTRADO en %%D:\dev\tools\maven
)

echo.
echo Buscando en IntelliJ IDEA bundled Maven:
for %%D in (C D E) do (
    if exist "%%D:\Program Files\JetBrains" (
        dir "%%D:\Program Files\JetBrains" /b /s | findstr /i "mvn.cmd"
    )
    if exist "%%~USERPROFILE\.m2\wrapper\dists" (
        echo Maven wrapper cache encontrado en .m2
        dir "%%~USERPROFILE\.m2\wrapper\dists" /b 2>nul
    )
)

echo.
echo Buscando mvn.cmd en todo C:\ (puede tardar)...
where mvn 2>nul
if %ERRORLEVEL% EQU 0 (
    echo mvn encontrado en PATH!
    where mvn
) else (
    echo mvn no esta en PATH
)

echo.
echo Variables JAVA_HOME / MAVEN_HOME:
echo JAVA_HOME=%JAVA_HOME%
echo MAVEN_HOME=%MAVEN_HOME%
echo M2_HOME=%M2_HOME%
echo.
pause
