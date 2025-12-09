# Performance and Resilience Test Suite for Windows
# Based on the original backend technical test

Write-Host "Starting Performance and Resilience Test Suite..." -ForegroundColor Green

# Clean up any existing containers
Write-Host "Cleaning up existing containers..." -ForegroundColor Yellow
docker-compose down --remove-orphans 2>$null

# Start the infrastructure
Write-Host "Starting test infrastructure (InfluxDB, Grafana, Simulado)..." -ForegroundColor Blue
docker-compose up -d simulado influxdb grafana

# Wait for services to be ready
Write-Host "Waiting for services to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Check that simulado is working
Write-Host "Checking simulado mock service..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "http://localhost:3001/product/1/similarids" -TimeoutSec 10
    Write-Host "Simulado is working correctly." -ForegroundColor Green
} catch {
    Write-Host "Simulado is not responding. Check the service." -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Make sure the application is running
Write-Host "Checking if your application is running on port 5000..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "http://localhost:5000/product/1/similar" -TimeoutSec 5
    Write-Host "Your application is responding on port 5000." -ForegroundColor Green
} catch {
    Write-Host "Your application is not running on port 5000." -ForegroundColor Yellow
    Write-Host "   Please start your Spring Boot application with: mvn spring-boot:run -pl bootstrap" -ForegroundColor White
    Write-Host "   Or run: java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar" -ForegroundColor White
    exit 1
}

# Run the performance tests
Write-Host ""
Write-Host "Running K6 Performance Tests..." -ForegroundColor Magenta
Write-Host "   - Normal load test (200 users, 10s)" -ForegroundColor White
Write-Host "   - Not found scenarios (200 users, 10s)" -ForegroundColor White
Write-Host "   - Error scenarios (200 users, 10s)" -ForegroundColor White
Write-Host "   - Slow responses (200 users, 10s)" -ForegroundColor White
Write-Host "   - Very slow responses (200 users, 10s)" -ForegroundColor White
Write-Host "   Total duration: ~70 seconds" -ForegroundColor Yellow

docker-compose run --rm k6 run /scripts/test.js

# Show results
Write-Host ""
Write-Host "Performance tests completed!" -ForegroundColor Green
Write-Host ""
Write-Host "GRAFANA DASHBOARD ACCESS:" -ForegroundColor Blue
Write-Host "   URL: http://localhost:3000" -ForegroundColor Cyan
Write-Host "   Login: admin/admin (no password required)" -ForegroundColor White
Write-Host ""
Write-Host "MANUAL DASHBOARD SETUP (if dashboard doesn't show data):" -ForegroundColor Yellow
Write-Host "   1. Go to http://localhost:3000" -ForegroundColor White
Write-Host "   2. Click '+' -> Import" -ForegroundColor White
Write-Host "   3. Use Grafana dashboard ID: 2587 (K6 Load Testing Results)" -ForegroundColor White
Write-Host "   4. Select 'myinfluxdb' as datasource" -ForegroundColor White
Write-Host "   5. If no data shows, create a simple panel:" -ForegroundColor White
Write-Host "      - Query: SELECT * FROM http_reqs WHERE time > now() - 1h" -ForegroundColor Gray
Write-Host ""
Write-Host "INFLUXDB DATA VERIFICATION:" -ForegroundColor Blue
Write-Host "   Database: k6 at http://localhost:8086" -ForegroundColor White
Write-Host "   Test query: SHOW MEASUREMENTS" -ForegroundColor White
Write-Host ""
Write-Host "Key metrics to check:" -ForegroundColor Yellow
Write-Host "   - Response time percentiles (p95 should be < 2s for normal cases)" -ForegroundColor White
Write-Host "   - Error rate (should be low for valid requests)" -ForegroundColor White
Write-Host "   - Throughput (requests per second)" -ForegroundColor White
Write-Host "   - Resource usage during high load" -ForegroundColor White
Write-Host ""
Write-Host "Performance Tips:" -ForegroundColor Green
Write-Host "   - Use connection pooling for HTTP clients" -ForegroundColor White
Write-Host "   - Implement caching for frequently accessed data" -ForegroundColor White
Write-Host "   - Add timeouts and circuit breakers for external calls" -ForegroundColor White
Write-Host "   - Monitor memory usage under load" -ForegroundColor White
Write-Host ""
Write-Host "Resilience Features Tested:" -ForegroundColor Magenta
Write-Host "   - 404 Not Found handling" -ForegroundColor Green
Write-Host "   - 500 Server Error handling" -ForegroundColor Green
Write-Host "   - Slow response handling (1s delay)" -ForegroundColor Green
Write-Host "   - Very slow response handling (5s+ delay)" -ForegroundColor Green
Write-Host "   - Timeout handling (50s delay)" -ForegroundColor Green
Write-Host ""

# Keep infrastructure running for analysis
Write-Host "Infrastructure will keep running for analysis." -ForegroundColor Blue
Write-Host "   To stop: docker-compose down" -ForegroundColor White
Write-Host "   To view logs: docker-compose logs" -ForegroundColor White
