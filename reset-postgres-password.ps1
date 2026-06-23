# Script para restablecer contrasena de postgres en PostgreSQL 18
# REQUIERE ejecutarse como Administrador
# Usa [System.IO.File]::WriteAllText para escribir sin BOM

$PG_DATA = "C:\Program Files\PostgreSQL\18\data"
$PG_BIN  = "C:\Program Files\PostgreSQL\18\bin"
$SERVICE = "postgresql-x64-18"
$PG_HBA  = "$PG_DATA\pg_hba.conf"
$NEW_PASS = "stilum2024"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Reset contrasena PostgreSQL 18 (v2)" -ForegroundColor Cyan
Write-Host "========================================`n"

# Verificar admin
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]"Administrator")
if (-not $isAdmin) {
    Write-Host "ERROR: Se requiere Administrador." -ForegroundColor Red
    Read-Host "Presiona Enter para cerrar"
    exit 1
}

Write-Host "Ejecutando como Administrador - OK" -ForegroundColor Green

# 1. Leer pg_hba.conf original
Write-Host "`n1. Leyendo pg_hba.conf..." -ForegroundColor Yellow
$originalContent = [System.IO.File]::ReadAllText($PG_HBA)
$backup = "$PG_HBA.bak2"
[System.IO.File]::WriteAllText($backup, $originalContent, [System.Text.Encoding]::ASCII)
Write-Host "   Backup: $backup" -ForegroundColor Gray

# 2. Crear version con trust (sin BOM, ASCII)
Write-Host "`n2. Aplicando autenticacion 'trust' (sin BOM)..." -ForegroundColor Yellow
$trustContent = $originalContent -replace '(host\s+all\s+all\s+\S+\s+)(scram-sha-256|md5)', '$1trust'
$trustContent = $trustContent -replace '(local\s+all\s+all\s+)(scram-sha-256|md5)', '$1trust'
[System.IO.File]::WriteAllText($PG_HBA, $trustContent, [System.Text.Encoding]::ASCII)
Write-Host "   pg_hba.conf actualizado a 'trust' (ASCII, sin BOM)" -ForegroundColor Green

# Verificar que el archivo es legible
Write-Host "   Primeras 3 lineas del pg_hba.conf modificado:" -ForegroundColor Gray
(Get-Content $PG_HBA | Select-Object -First 3) | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }

# 3. Reiniciar servicio
Write-Host "`n3. Reiniciando PostgreSQL..." -ForegroundColor Yellow
net stop $SERVICE 2>$null
Start-Sleep -Seconds 4
net start $SERVICE
Start-Sleep -Seconds 4

$svc = Get-Service $SERVICE
if ($svc.Status -ne "Running") {
    Write-Host "   ERROR: Servicio no pudo iniciar. Revirtiendo..." -ForegroundColor Red
    [System.IO.File]::WriteAllText($PG_HBA, $originalContent, [System.Text.Encoding]::ASCII)
    net start $SERVICE
    Read-Host "Presiona Enter para cerrar"
    exit 1
}
Write-Host "   Servicio corriendo!" -ForegroundColor Green

# 4. Cambiar contrasena de postgres
Write-Host "`n4. Cambiando contrasena de postgres..." -ForegroundColor Yellow
$env:PGPASSWORD = ""
$result = & "$PG_BIN\psql.exe" -U postgres -p 5433 -c "ALTER USER postgres PASSWORD '$NEW_PASS';" 2>&1
Write-Host "   $result"

# 5. Crear usuario y BD stilum
Write-Host "`n5. Creando usuario stilum y BD stilum_citas..." -ForegroundColor Yellow
& "$PG_BIN\psql.exe" -U postgres -p 5433 -c "DO `$`$ BEGIN IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'stilum') THEN CREATE USER stilum WITH PASSWORD 'stilum_dev_pass'; END IF; END `$`$;" 2>&1
& "$PG_BIN\psql.exe" -U postgres -p 5433 -c "CREATE DATABASE stilum_citas OWNER stilum;" 2>&1
& "$PG_BIN\psql.exe" -U postgres -p 5433 -c "GRANT ALL PRIVILEGES ON DATABASE stilum_citas TO stilum;" 2>&1
Write-Host "   Usuario y BD configurados." -ForegroundColor Green

# 6. Revertir pg_hba.conf al original
Write-Host "`n6. Revirtiendo pg_hba.conf al original..." -ForegroundColor Yellow
[System.IO.File]::WriteAllText($PG_HBA, $originalContent, [System.Text.Encoding]::ASCII)
Write-Host "   pg_hba.conf restaurado." -ForegroundColor Green

# 7. Reiniciar servicio con config original
Write-Host "`n7. Reiniciando con config original..." -ForegroundColor Yellow
net stop $SERVICE 2>$null
Start-Sleep -Seconds 4
net start $SERVICE
Start-Sleep -Seconds 4
Write-Host "   Listo." -ForegroundColor Green

# 8. Verificar con nueva contrasena
Write-Host "`n8. Verificando conexion como stilum..." -ForegroundColor Yellow
$env:PGPASSWORD = "stilum_dev_pass"
$verify = & "$PG_BIN\psql.exe" -U stilum -p 5433 -d stilum_citas -c "SELECT 'OK' AS estado;" -w 2>&1
Write-Host "   $verify"

if ($verify -like "*OK*") {
    Write-Host "`n[OK] BASE DE DATOS LISTA!" -ForegroundColor Green
    Write-Host "   postgres password: $NEW_PASS" -ForegroundColor Green
    Write-Host "   stilum password: stilum_dev_pass" -ForegroundColor Green
    Write-Host "   Puerto: 5433 / DB: stilum_citas" -ForegroundColor Green
} else {
    Write-Host "`n[WARN] Verificacion: $verify" -ForegroundColor Yellow
}

Write-Host "`n========================================"
Read-Host "Presiona Enter para cerrar"
