# Script to setup Grafana dashboard for K6 performance tests
Write-Host "Setting up Grafana dashboard for K6 performance tests..." -ForegroundColor Blue

# Create a simple dashboard JSON for K6
$dashboardJson = @{
    "dashboard" = @{
        "id" = $null
        "title" = "K6 Performance Test Dashboard"
        "description" = "Performance metrics from K6 load tests"
        "tags" = @("k6", "performance")
        "timezone" = "browser"
        "panels" = @(
            @{
                "id" = 1
                "title" = "HTTP Requests per Second"
                "type" = "stat"
                "targets" = @(
                    @{
                        "datasource" = "myinfluxdb"
                        "query" = "SELECT mean(value) FROM http_reqs WHERE time > now() - 5m GROUP BY time(10s)"
                        "rawQuery" = $true
                    }
                )
                "gridPos" = @{ "h" = 8; "w" = 12; "x" = 0; "y" = 0 }
            },
            @{
                "id" = 2
                "title" = "Response Time (95th percentile)"
                "type" = "stat"
                "targets" = @(
                    @{
                        "datasource" = "myinfluxdb"
                        "query" = "SELECT mean(value) FROM http_req_duration WHERE time > now() - 5m AND type='trend' GROUP BY time(10s)"
                        "rawQuery" = $true
                    }
                )
                "gridPos" = @{ "h" = 8; "w" = 12; "x" = 12; "y" = 0 }
            }
        )
        "time" = @{
            "from" = "now-5m"
            "to" = "now"
        }
        "refresh" = "5s"
    }
    "overwrite" = $true
} | ConvertTo-Json -Depth 10

# Instructions for manual setup
Write-Host ""
Write-Host "GRAFANA DASHBOARD SETUP INSTRUCTIONS:" -ForegroundColor Yellow
Write-Host "=================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. OPEN GRAFANA:" -ForegroundColor Green
Write-Host "   http://localhost:3000" -ForegroundColor Cyan
Write-Host "   (Login: admin/admin, no password needed)" -ForegroundColor Gray
Write-Host ""
Write-Host "2. VERIFY DATA SOURCE:" -ForegroundColor Green
Write-Host "   - Go to Configuration > Data Sources" -ForegroundColor White
Write-Host "   - Should see 'myinfluxdb' (default)" -ForegroundColor White
Write-Host "   - Click 'Test' to verify connection" -ForegroundColor White
Write-Host ""
Write-Host "3. CREATE DASHBOARD:" -ForegroundColor Green
Write-Host "   Option A - Import existing K6 dashboard:" -ForegroundColor White
Write-Host "   - Click '+' > Import" -ForegroundColor White
Write-Host "   - Use ID: 2587 (official K6 dashboard)" -ForegroundColor Cyan
Write-Host "   - Select 'myinfluxdb' as datasource" -ForegroundColor White
Write-Host ""
Write-Host "   Option B - Create simple panel:" -ForegroundColor White
Write-Host "   - Click '+' > Dashboard > Add new panel" -ForegroundColor White
Write-Host "   - Query: SELECT * FROM http_reqs" -ForegroundColor Gray
Write-Host "   - Or: SHOW MEASUREMENTS to see all available data" -ForegroundColor Gray
Write-Host ""
Write-Host "4. TROUBLESHOOTING:" -ForegroundColor Red
Write-Host "   If no data appears:" -ForegroundColor White
Write-Host "   - Run tests again: docker-compose run --rm k6 run /scripts/test.js" -ForegroundColor Gray
Write-Host "   - Check time range (last 1 hour)" -ForegroundColor Gray
Write-Host "   - Verify InfluxDB: http://localhost:8086" -ForegroundColor Gray
Write-Host ""
Write-Host "PERFORMANCE TESTS STATUS:" -ForegroundColor Blue
Write-Host "========================" -ForegroundColor Blue

# Check if tests were run recently
try {
    $measurements = docker exec similar-products-api-influxdb-1 influx -execute "SHOW MEASUREMENTS" -database "k6"
    if ($measurements -match "http_reqs") {
        Write-Host "✓ Datos K6 encontrados en InfluxDB" -ForegroundColor Green
        Write-Host "  Mediciones disponibles:" -ForegroundColor White
        Write-Host "  - http_reqs (Requests per second)" -ForegroundColor Cyan
        Write-Host "  - http_req_duration (Response time)" -ForegroundColor Cyan
        Write-Host "  - data_received/data_sent (Data throughput)" -ForegroundColor Cyan
        Write-Host "  - http_req_failed (Error rate)" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "📊 QUERIES RECOMENDADAS PARA GRAFANA:" -ForegroundColor Yellow
        Write-Host "   Para RPS:" -ForegroundColor Cyan
        Write-Host "   SELECT mean(value) FROM http_reqs WHERE time > now() - 1h GROUP BY time(10s)" -ForegroundColor Gray
        Write-Host "   Para Response Time:" -ForegroundColor Cyan
        Write-Host "   SELECT mean(value) FROM http_req_duration WHERE time > now() - 1h GROUP BY time(10s)" -ForegroundColor Gray
        Write-Host "   Para Error Rate:" -ForegroundColor Cyan
        Write-Host "   SELECT mean(value) FROM http_req_failed WHERE time > now() - 1h GROUP BY time(10s)" -ForegroundColor Gray
    } else {
        Write-Host "⚠ No hay datos de K6 recientes" -ForegroundColor Yellow
        Write-Host "  Ejecuta los tests:" -ForegroundColor White
        Write-Host "  docker-compose run --rm k6 run --out influxdb=http://influxdb:8086/k6 /scripts/test.js" -ForegroundColor Cyan
    }
} catch {
    Write-Host "⚠ Error conectando a InfluxDB" -ForegroundColor Yellow
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  Asegúrate de que los servicios estén corriendo:" -ForegroundColor White
    Write-Host "  docker-compose up -d influxdb grafana" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "Expected Results After Tests:" -ForegroundColor Green
Write-Host "   - ~300+ requests/second" -ForegroundColor White
Write-Host "   - P95 response time less than 100ms" -ForegroundColor White
Write-Host "   - 0% error rate for valid endpoints" -ForegroundColor White
Write-Host "   - Mix of 200, 404, and 500 responses (expected)" -ForegroundColor White
