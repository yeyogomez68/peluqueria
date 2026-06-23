@echo off
title Actualizar contrasena admin
set PGPASSWORD=stilum_dev_pass
"C:\Program Files\PostgreSQL\18\bin\psql.exe" -U stilum -p 5433 -d stilum_citas -c "UPDATE users SET password_hash = '$2b$12$Pw39e2NbeLOhCwrWJIkkGuWsR1VjAAJPATkdu7Cqxw67oAT4quBIe' WHERE email = 'admin@stilum.com';"
echo.
echo Listo. Contrasena actualizada a: Admin@Stilum2024!
pause
