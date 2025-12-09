# Script de Performance Testing para Producción
param(
    [Parameter(Mandatory=$true)]
    [string]$Environment = "staging",

    [Parameter(Mandatory=$true)]
    [string]$TargetUrl = "http://localhost:5000",

    [int]$Duration = 30,
    [int]$VirtualUsers = 50,
    [switch]$GenerateReport = $true,
    [string]$SlackWebhook = ""
)

Write-Host "🚀 PRODUCTION PERFORMANCE TESTING" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green
Write-Host "Environment: $Environment" -ForegroundColor White
Write-Host "Target: $TargetUrl" -ForegroundColor White
Write-Host "Duration: $Duration seconds" -ForegroundColor White
Write-Host "Virtual Users: $VirtualUsers" -ForegroundColor White
Write-Host ""

# Crear script K6 dinámico
$k6Script = @"
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '${Duration}s', target: $VirtualUsers },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must complete below 500ms
    http_req_failed: ['rate<0.1'],    // Error rate must be below 10%
  },
};

export default function () {
  const response = http.get('$TargetUrl/product/1/similar');

  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);
}
"@

# Guardar script temporal
$k6Script | Out-File -FilePath "./temp-k6-prod.js" -Encoding UTF8

# Ejecutar tests
Write-Host "📊 Ejecutando tests de performance..." -ForegroundColor Blue

$startTime = Get-Date
$result = docker run --rm -v "${PWD}:/scripts" grafana/k6:0.45.0 run /scripts/temp-k6-prod.js

$endTime = Get-Date
$duration = $endTime - $startTime

# Analizar resultados
$success = $LASTEXITCODE -eq 0
$rps = 0
$p95 = 0
$errorRate = 0

if ($result -match "http_reqs.*?(\d+\.?\d*)/s") {
    $rps = [math]::Round([double]$matches[1], 2)
}

if ($result -match "p\(95\)=(\d+\.?\d*)ms") {
    $p95 = [math]::Round([double]$matches[1], 2)
}

if ($result -match "http_req_failed.*?(\d+\.?\d*)%") {
    $errorRate = [math]::Round([double]$matches[1], 2)
}

# Generar reporte
$report = @{
    Environment = $Environment
    TargetUrl = $TargetUrl
    Timestamp = $startTime.ToString("yyyy-MM-dd HH:mm:ss")
    Duration = $duration.TotalSeconds
    Success = $success
    RPS = $rps
    P95_ResponseTime = $p95
    ErrorRate = $errorRate
    Passed = $success -and $rps -gt 10 -and $p95 -lt 500 -and $errorRate -lt 10
}

# Mostrar resultados
Write-Host ""
Write-Host "📊 RESULTADOS:" -ForegroundColor Yellow
Write-Host "===============" -ForegroundColor Yellow
Write-Host "✓ RPS: $($report.RPS)" -ForegroundColor $(if($report.RPS -gt 50) {"Green"} else {"Red"})
Write-Host "✓ P95 Response Time: $($report.P95_ResponseTime)ms" -ForegroundColor $(if($report.P95_ResponseTime -lt 200) {"Green"} else {"Red"})
Write-Host "✓ Error Rate: $($report.ErrorRate)%" -ForegroundColor $(if($report.ErrorRate -lt 5) {"Green"} else {"Red"})
Write-Host "✓ Overall: $(if($report.Passed) {"✅ PASSED"} else {"❌ FAILED"})" -ForegroundColor $(if($report.Passed) {"Green"} else {"Red"})

# Generar archivo de reporte
if ($GenerateReport) {
    $reportFile = "performance-report-$Environment-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
    $report | ConvertTo-Json | Out-File $reportFile
    Write-Host ""
    Write-Host "📄 Reporte generado: $reportFile" -ForegroundColor Blue
}

# Enviar notificación a Slack (opcional)
if ($SlackWebhook -and $SlackWebhook -ne "") {
    $slackMessage = @{
        text = "Performance Test Results - $Environment"
        attachments = @(
            @{
                color = if($report.Passed) {"good"} else {"danger"}
                fields = @(
                    @{ title = "Environment"; value = $Environment; short = $true }
                    @{ title = "RPS"; value = $report.RPS; short = $true }
                    @{ title = "P95"; value = "$($report.P95_ResponseTime)ms"; short = $true }
                    @{ title = "Error Rate"; value = "$($report.ErrorRate)%"; short = $true }
                )
            }
        )
    } | ConvertTo-Json -Depth 3

    try {
        Invoke-RestMethod -Uri $SlackWebhook -Method POST -Body $slackMessage -ContentType "application/json"
        Write-Host "📢 Notificación enviada a Slack" -ForegroundColor Green
    } catch {
        Write-Host "⚠️ Error enviando notificación a Slack: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

# Limpiar archivos temporales
Remove-Item "./temp-k6-prod.js" -ErrorAction SilentlyContinue

# Exit code para CI/CD
if (-not $report.Passed) {
    Write-Host ""
    Write-Host "❌ Tests fallaron - revisa los thresholds" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "✅ Tests completados exitosamente" -ForegroundColor Green
exit 0
