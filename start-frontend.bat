@echo off
title Frontend - Stilum Angular (Puerto 4200)
echo ========================================
echo  Iniciando Frontend Angular...
echo ========================================
cd /d "%~dp0stilum-frontend"
npx ng serve --open
pause
