# Script para ejecutar tests K6 y abrir dashboard automáticamente
param(
    [switch]$SkipTests = $false
)

Write-Host "🚀 K6 Performance Tests & Dashboard Viewer" -ForegroundColor Green
Write-Host "===========================================" -ForegroundColor Green
Write-Host ""

if (-not $SkipTests) {
    Write-Host "📊 Ejecutando tests de performance..." -ForegroundColor Blue
    docker-compose run --rm k6 run --out influxdb=http://influxdb:8086/k6 /scripts/test.js

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Tests completados exitosamente!" -ForegroundColor Green
    } else {
        Write-Host "❌ Error en la ejecución de tests" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "⏭️ Omitiendo ejecución de tests..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🎯 Abriendo dashboard de resultados..." -ForegroundColor Blue
Start-Process "http://localhost:3000/d/0MGZJGGvk"

Write-Host ""
Write-Host "📈 Dashboard URLs:" -ForegroundColor Yellow
Write-Host "   NEW Working Dashboard: http://localhost:3000/d/0MGZJGGvk" -ForegroundColor Cyan
Write-Host "   Old Dashboard: http://localhost:3000/d/c0mmAMGDz/k6-performance-test-results-working" -ForegroundColor Gray
Write-Host "   Grafana Home: http://localhost:3000" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 Tip: Usa -SkipTests para abrir solo el dashboard sin ejecutar tests" -ForegroundColor Gray
