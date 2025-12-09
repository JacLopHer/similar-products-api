# Script SIMPLE para developers que NO son DevOps
# Solo verifica que tu API funcione después de un deployment

param(
    [string]$ApiUrl = "http://localhost:5000"
)

Write-Host "Verificando que tu API funciona..." -ForegroundColor Blue

try {
    $response = Invoke-RestMethod "$ApiUrl/product/1/similar" -TimeoutSec 10

    if ($response -and $response.Count -gt 0) {
        Write-Host "✅ API FUNCIONANDO CORRECTAMENTE" -ForegroundColor Green
        Write-Host "   Productos devueltos: $($response.Count)" -ForegroundColor White
        Write-Host "   Primer producto: $($response[0].name)" -ForegroundColor White
        Write-Host ""
        Write-Host "🎉 Todo OK - puedes relajarte" -ForegroundColor Green
        exit 0
    } else {
        Write-Host "❌ API devuelve respuesta vacía" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ API no responde: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Verifica que esté ejecutándose en $ApiUrl" -ForegroundColor Yellow
    exit 1
}
