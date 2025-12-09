# Complete Testing Suite Runner
# Executes all types of tests: Unit -> Integration -> Resilience -> Performance

param(
    [Parameter()]
    [ValidateSet("all", "unit", "integration", "resilience", "performance")]
    [string]$TestType = "all"
)

function Write-Header {
    param($Message)
    Write-Host ""
    Write-Host "=" * 60 -ForegroundColor Blue
    Write-Host $Message -ForegroundColor Yellow
    Write-Host "=" * 60 -ForegroundColor Blue
}

function Write-Success {
    param($Message)
    Write-Host "SUCCESS: $Message" -ForegroundColor Green
}

function Write-Error {
    param($Message)
    Write-Host "ERROR: $Message" -ForegroundColor Red
}

function Run-UnitTests {
    Write-Header "RUNNING UNIT TESTS"
    mvn clean test
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Unit tests passed"
        return $true
    } else {
        Write-Error "Unit tests failed"
        return $false
    }
}

function Run-IntegrationTests {
    Write-Header "RUNNING INTEGRATION TESTS"
    mvn verify
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Integration tests passed"
        return $true
    } else {
        Write-Error "Integration tests failed"
        return $false
    }
}

function Run-ResilienceTests {
    Write-Header "RUNNING RESILIENCE TESTS"
    mvn test -Dtest=SimilarProductsResilienceTest -pl bootstrap
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Resilience tests passed"
        return $true
    } else {
        Write-Error "Resilience tests failed"
        return $false
    }
}

function Run-PerformanceTests {
    Write-Header "RUNNING PERFORMANCE TESTS"
    .\scripts\run-performance-tests.ps1
    Write-Success "Performance tests completed (check Grafana for results)"
    return $true
}

# Main execution
$startTime = Get-Date
Write-Header "SIMILAR PRODUCTS API - COMPLETE TEST SUITE"

switch ($TestType) {
    "unit" {
        $success = Run-UnitTests
    }
    "integration" {
        $success = Run-IntegrationTests
    }
    "resilience" {
        $success = Run-ResilienceTests
    }
    "performance" {
        $success = Run-PerformanceTests
    }
    "all" {
        $unitSuccess = Run-UnitTests
        if ($unitSuccess) {
            $integrationSuccess = Run-IntegrationTests
            if ($integrationSuccess) {
                $resilienceSuccess = Run-ResilienceTests
                if ($resilienceSuccess) {
                    $performanceSuccess = Run-PerformanceTests
                    $success = $performanceSuccess
                } else {
                    $success = $false
                }
            } else {
                $success = $false
            }
        } else {
            $success = $false
        }
    }
}

$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds

Write-Header "TEST EXECUTION SUMMARY"
Write-Host "Type: $TestType" -ForegroundColor White
Write-Host "Duration: $([math]::Round($duration, 2)) seconds" -ForegroundColor White

if ($success) {
    Write-Success "All tests completed successfully!"
    Write-Host ""
    Write-Host "Next Steps:" -ForegroundColor Yellow
    Write-Host "   - Check performance results: http://localhost:3000" -ForegroundColor White
    Write-Host "   - Review test reports: bootstrap/target/surefire-reports/" -ForegroundColor White
    Write-Host "   - Verify application metrics and logs" -ForegroundColor White
} else {
    Write-Error "Some tests failed. Check the output above."
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Yellow
    Write-Host "   - Check application logs for errors" -ForegroundColor White
    Write-Host "   - Verify external service mocks are running" -ForegroundColor White
    Write-Host "   - Review test reports for detailed failure information" -ForegroundColor White
}

if ($TestType -eq "all" -or $TestType -eq "performance") {
    Write-Host ""
    Write-Host "Performance Dashboard: http://localhost:3000/d/Le2Ku9NMk/k6-performance-test" -ForegroundColor Cyan
    Write-Host "To stop infrastructure: docker-compose down" -ForegroundColor Blue
}
