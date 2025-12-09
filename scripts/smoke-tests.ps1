# Smoke Tests para Post-Deployment
param(
    [Parameter(Mandatory=$true)]
    [string]$TargetUrl = "http://localhost:5000",
    [int]$TimeoutSeconds = 30
)

Write-Host "SMOKE TESTS POST-DEPLOYMENT" -ForegroundColor Green
Write-Host "==============================" -ForegroundColor Green
Write-Host "Target: $TargetUrl" -ForegroundColor White
Write-Host ""

$tests = @(
    @{
        Name = "Health Check"
        Url = "$TargetUrl/actuator/health"
        ExpectedStatus = 200
        MaxResponseTime = 1000
    },
    @{
        Name = "Get Similar Products - Product 1"
        Url = "$TargetUrl/product/1/similar"
        ExpectedStatus = 200
        MaxResponseTime = 2000
        RequiredFields = @("id", "name", "price")
    },
    @{
        Name = "Get Similar Products - Product 2"
        Url = "$TargetUrl/product/2/similar"
        ExpectedStatus = 200
        MaxResponseTime = 2000
    },
    @{
        Name = "Invalid Product - 404 Test"
        Url = "$TargetUrl/product/999/similar"
        ExpectedStatus = 404
        MaxResponseTime = 1000
    }
)

$passed = 0
$failed = 0
$results = @()

foreach ($test in $tests) {
    Write-Host "Testing: $($test.Name)" -ForegroundColor Blue

    try {
        $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
        $response = Invoke-RestMethod -Uri $test.Url -TimeoutSec $TimeoutSeconds -ErrorAction Stop
        $stopwatch.Stop()

        $responseTime = $stopwatch.ElapsedMilliseconds
        $statusPassed = $response.StatusCode -eq $test.ExpectedStatus -or $test.ExpectedStatus -eq 200
        $timePassed = $responseTime -le $test.MaxResponseTime
        $fieldsPassed = $true

        if ($test.RequiredFields) {
            foreach ($field in $test.RequiredFields) {
                if (-not ($response | Get-Member -Name $field)) {
                    $fieldsPassed = $false
                    break
                }
            }
        }

        $testPassed = $statusPassed -and $timePassed -and $fieldsPassed

        if ($testPassed) {
            Write-Host "   PASSED ($responseTime ms)" -ForegroundColor Green
            $passed++
        } else {
            Write-Host "   FAILED ($responseTime ms)" -ForegroundColor Red
            $failed++
        }

        $results += @{
            Test = $test.Name
            Passed = $testPassed
            ResponseTime = $responseTime
            Status = "Success"
            Details = "Response time: $responseTime ms"
        }

    } catch {
        Write-Host "   FAILED (Error: $($_.Exception.Message))" -ForegroundColor Red
        $failed++

        $results += @{
            Test = $test.Name
            Passed = $false
            ResponseTime = -1
            Status = "Error"
            Details = $_.Exception.Message
        }
    }
}

Write-Host ""
Write-Host "RESULTADOS SMOKE TESTS:" -ForegroundColor Yellow
Write-Host "=======================" -ForegroundColor Yellow
Write-Host "Passed: $passed" -ForegroundColor Green
Write-Host "Failed: $failed" -ForegroundColor Red
Write-Host "Success Rate: $([math]::Round(($passed / ($passed + $failed)) * 100, 1))%" -ForegroundColor White

# Generar reporte JSON
$smokeReport = @{
    Timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    Target = $TargetUrl
    TotalTests = $passed + $failed
    PassedTests = $passed
    FailedTests = $failed
    SuccessRate = [math]::Round(($passed / ($passed + $failed)) * 100, 1)
    Tests = $results
    Overall = if ($failed -eq 0) {"PASSED"} else {"FAILED"}
}

$reportFile = "smoke-test-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
$smokeReport | ConvertTo-Json -Depth 3 | Out-File $reportFile

Write-Host ""
Write-Host "Reporte generado: $reportFile" -ForegroundColor Blue

if ($failed -gt 0) {
    Write-Host ""
    Write-Host "SMOKE TESTS FAILED - Deployment may have issues" -ForegroundColor Red
    Write-Host "Revisa los logs y considera un rollback" -ForegroundColor Yellow
    exit 1
} else {
    Write-Host ""
    Write-Host "ALL SMOKE TESTS PASSED - Deployment OK" -ForegroundColor Green
    exit 0
}
