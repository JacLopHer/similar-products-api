#!/bin/bash
# Performance and Resilience Test Suite
# Based on the original backend technical test

echo "🚀 Starting Performance and Resilience Test Suite..."

# Clean up any existing containers
echo "🧹 Cleaning up existing containers..."
docker-compose down --remove-orphans || true

# Start the infrastructure
echo "📊 Starting test infrastructure (InfluxDB, Grafana, Simulado)..."
docker-compose up -d simulado influxdb grafana

# Wait for services to be ready
echo "⏳ Waiting for services to initialize..."
sleep 15

# Check that simulado is working
echo "🔍 Checking simulado mock service..."
curl -f http://localhost:3001/product/1/similarids || {
    echo "❌ Simulado is not responding. Check the service."
    exit 1
}

echo "✅ Simulado is working correctly."

# Make sure the application is running
echo "🏃 Checking if your application is running on port 5000..."
curl -f http://localhost:5000/product/1/similar > /dev/null 2>&1 || {
    echo "⚠️  Your application is not running on port 5000."
    echo "   Please start your Spring Boot application with: mvn spring-boot:run -pl bootstrap"
    echo "   Or run: java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar"
    exit 1
}

echo "✅ Your application is responding on port 5000."

# Run the performance tests
echo "🎯 Running K6 Performance Tests..."
echo "   - Normal load test (200 users, 10s)"
echo "   - Not found scenarios (200 users, 10s)"
echo "   - Error scenarios (200 users, 10s)"
echo "   - Slow responses (200 users, 10s)"
echo "   - Very slow responses (200 users, 10s)"
echo "   Total duration: ~70 seconds"

docker-compose run --rm k6 run /scripts/test.js

# Show results
echo ""
echo "🎉 Performance tests completed!"
echo ""
echo "📈 View detailed results at: http://localhost:3000/d/Le2Ku9NMk/k6-performance-test"
echo ""
echo "🔍 Key metrics to check:"
echo "   - Response time percentiles (p95 should be < 2s for normal cases)"
echo "   - Error rate (should be low for valid requests)"
echo "   - Throughput (requests per second)"
echo "   - Resource usage during high load"
echo ""
echo "💡 Performance Tips:"
echo "   - Use connection pooling for HTTP clients"
echo "   - Implement caching for frequently accessed data"
echo "   - Add timeouts and circuit breakers for external calls"
echo "   - Monitor memory usage under load"
echo ""
echo "🛡️  Resilience Features Tested:"
echo "   - ✅ 404 Not Found handling"
echo "   - ✅ 500 Server Error handling"
echo "   - ✅ Slow response handling (1s delay)"
echo "   - ✅ Very slow response handling (5s+ delay)"
echo "   - ✅ Timeout handling (50s delay)"
echo ""

# Keep infrastructure running for analysis
echo "🔧 Infrastructure will keep running for analysis."
echo "   To stop: docker-compose down"
echo "   To view logs: docker-compose logs"
