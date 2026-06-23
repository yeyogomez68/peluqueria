# Script: descarga Maven si no esta, luego arranca Spring Boot
$MAVEN_VERSION = "3.9.9"
$MAVEN_ZIP_URL = "https://dlcdn.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip"
$MAVEN_MIRROR  = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip"
$MAVEN_DIR     = "$env:USERPROFILE\.m2\maven-$MAVEN_VERSION"
$MAVEN_CMD     = "$MAVEN_DIR\bin\mvn.cmd"
$BACKEND_DIR   = "$PSScriptRoot\stilum-backend"
$JAVA_HOME     = "C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Stilum Backend - Iniciador Automatico" -ForegroundColor Cyan
Write-Host "========================================`n"

# 1. Verificar Java
$env:JAVA_HOME = $JAVA_HOME
if (-not (Test-Path "$JAVA_HOME\bin\java.exe")) {
    Write-Host "ERROR: Java no encontrado en $JAVA_HOME" -ForegroundColor Red
    Read-Host "Enter para cerrar"; exit 1
}
Write-Host "[OK] Java 21: $JAVA_HOME" -ForegroundColor Green

# 2. Buscar Maven ya instalado
$mvnCandidates = @(
    "C:\Program Files\JetBrains\IntelliJ IDEA 2024.2.1\plugins\maven\lib\maven3\bin\mvn.cmd",
    "C:\Program Files\JetBrains\IntelliJ IDEA 2024.2\plugins\maven\lib\maven3\bin\mvn.cmd",
    "$MAVEN_CMD"
)
# Buscar tambien en Toolbox
Get-ChildItem "$env:LOCALAPPDATA\JetBrains\Toolbox\apps" -Recurse -Filter "mvn.cmd" -ErrorAction SilentlyContinue |
    Select-Object -First 1 | ForEach-Object { $mvnCandidates += $_.FullName }

$foundMvn = $null
foreach ($c in $mvnCandidates) {
    if (Test-Path $c) { $foundMvn = $c; break }
}

# 3. Si no hay Maven, descargarlo
if (-not $foundMvn) {
    Write-Host "`nMaven no encontrado. Descargando Maven $MAVEN_VERSION..." -ForegroundColor Yellow
    $zipPath = "$env:TEMP\apache-maven-$MAVEN_VERSION-bin.zip"

    if (-not (Test-Path $zipPath)) {
        Write-Host "  Descargando desde $MAVEN_ZIP_URL ..." -ForegroundColor Gray
        try {
            Invoke-WebRequest -Uri $MAVEN_ZIP_URL -OutFile $zipPath -UseBasicParsing -TimeoutSec 120
        } catch {
            Write-Host "  Primer mirror fallo, intentando alternativo..." -ForegroundColor Yellow
            Invoke-WebRequest -Uri $MAVEN_MIRROR -OutFile $zipPath -UseBasicParsing -TimeoutSec 120
        }
    }

    Write-Host "  Extrayendo Maven en $MAVEN_DIR ..." -ForegroundColor Gray
    if (Test-Path $MAVEN_DIR) { Remove-Item $MAVEN_DIR -Recurse -Force }
    New-Item -ItemType Directory -Path $MAVEN_DIR -Force | Out-Null
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [System.IO.Compression.ZipFile]::OpenRead($zipPath)
    foreach ($entry in $zip.Entries) {
        $destPath = Join-Path $MAVEN_DIR ($entry.FullName -replace "^apache-maven-$MAVEN_VERSION/", "")
        if ($entry.FullName.EndsWith("/")) {
            New-Item -ItemType Directory -Path $destPath -Force | Out-Null
        } else {
            $dir = Split-Path $destPath
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
            [System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $destPath, $true)
        }
    }
    $zip.Dispose()
    $foundMvn = $MAVEN_CMD
    Write-Host "  Maven instalado en: $MAVEN_DIR" -ForegroundColor Green
}

Write-Host "[OK] Maven: $foundMvn" -ForegroundColor Green

# 4. Arrancar Spring Boot
Write-Host "`nArrancando Spring Boot backend..." -ForegroundColor Cyan
Write-Host "Listo cuando aparezca: Started ... in X seconds`n" -ForegroundColor Yellow
Set-Location $BACKEND_DIR
$env:PATH = "$JAVA_HOME\bin;$env:PATH"
& $foundMvn spring-boot:run
