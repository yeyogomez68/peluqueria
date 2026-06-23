# Script para detectar y arrancar PostgreSQL
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Buscando PostgreSQL en el sistema..." -ForegroundColor Cyan
Write-Host "========================================`n"

# 1. Buscar servicios de PostgreSQL
Write-Host ">> Servicios Windows con 'postgres':" -ForegroundColor Yellow
$pgServices = Get-Service | Where-Object { $_.Name -like "*postgres*" -or $_.DisplayName -like "*postgres*" }
if ($pgServices) {
    $pgServices | Format-Table Name, DisplayName, Status -AutoSize
    foreach ($svc in $pgServices) {
        if ($svc.Status -ne "Running") {
            Write-Host "Iniciando servicio: $($svc.Name)..." -ForegroundColor Green
            try {
                Start-Service $svc.Name -ErrorAction Stop
                Write-Host "SERVICIO INICIADO: $($svc.Name)" -ForegroundColor Green
            } catch {
                Write-Host "Error al iniciar (puede requerir admin): $_" -ForegroundColor Red
                Write-Host "Intentando con 'net start'..." -ForegroundColor Yellow
                net start $svc.Name
            }
        } else {
            Write-Host "Servicio $($svc.Name) ya esta CORRIENDO" -ForegroundColor Green
        }
    }
} else {
    Write-Host "No se encontraron servicios de PostgreSQL" -ForegroundColor Red
}

# 2. Buscar psql.exe en todo el sistema
Write-Host "`n>> Buscando psql.exe en todos los discos..." -ForegroundColor Yellow
$drives = Get-PSDrive -PSProvider FileSystem | Select-Object -ExpandProperty Root
foreach ($drive in $drives) {
    try {
        $found = Get-ChildItem -Path $drive -Recurse -Filter "psql.exe" -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) {
            Write-Host "ENCONTRADO: $($found.FullName)" -ForegroundColor Green
        }
    } catch {}
}

# 3. Verificar conexion a PostgreSQL si esta corriendo
Write-Host "`n>> Verificando conexion a localhost:5432..." -ForegroundColor Yellow
try {
    $tcp = New-Object System.Net.Sockets.TcpClient
    $tcp.Connect("localhost", 5432)
    $tcp.Close()
    Write-Host "CONEXION EXITOSA a localhost:5432 - PostgreSQL esta corriendo!" -ForegroundColor Green

    # Intentar crear DB con psql si se encontro
    $psqlPaths = @(
        "C:\Program Files\PostgreSQL\16\bin\psql.exe",
        "C:\Program Files\PostgreSQL\15\bin\psql.exe",
        "C:\Program Files\PostgreSQL\14\bin\psql.exe",
        "C:\Program Files\PostgreSQL\17\bin\psql.exe"
    )
    foreach ($path in $psqlPaths) {
        if (Test-Path $path) {
            Write-Host "`nCreando usuario y DB si no existen..." -ForegroundColor Yellow
            & $path -U postgres -c "DO `$`$ BEGIN IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'stilum') THEN CREATE USER stilum WITH PASSWORD 'stilum_dev_pass'; END IF; END `$`$;"
            & $path -U postgres -c "CREATE DATABASE stilum_citas OWNER stilum;" 2>$null
            & $path -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE stilum_citas TO stilum;" 2>$null
            Write-Host "Base de datos lista." -ForegroundColor Green
            break
        }
    }
} catch {
    Write-Host "SIN CONEXION a localhost:5432 - PostgreSQL no esta corriendo" -ForegroundColor Red
}

Write-Host "`n========================================"
Write-Host " Resultado final:"
Write-Host "========================================"
$pgServices | Format-Table Name, Status -AutoSize
Read-Host "`nPresiona Enter para cerrar"
