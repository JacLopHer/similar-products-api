# Similar Products API

**Hexagonal Architecture** (Ports & Adapters) with explicit multi-module Maven structure.

## Architecture

### Module Structure

```
similar-products-api/
│
├── domain/              ← CORE (Entities, Value Objects, Port Interfaces)
│   └── No dependencies, no implementations
│
├── application/         ← USE CASES (Business Logic Orchestration)
│   └── Depends on: domain
│   └── No Spring, pure Java
│
├── infrastructure/      ← ADAPTERS (Primary REST + Secondary HTTP)
│   ├── adapter/rest/         → Primary adapters (Controllers)
│   ├── adapter/http/         → Secondary adapters (HTTP clients)
│   └── config/               → Technical configuration
│   └── Depends on: domain + application
│
└── bootstrap/           ← SPRING BOOT (Dependency Injection & Main)
    └── Depends on: all modules
    └── Wires everything together
```

### Dependency Flow

```
bootstrap → infrastructure → application → domain

No backwards dependencies allowed!
```

## Technology Stack

- **Java 17** (Records, Sealed types)
- **Spring Boot 3.2.1**
- **Maven Multi-module**
- **WebClient** (Reactive HTTP)
- **Resilience4j** (Circuit Breaker & Retry)
- **Lombok** (Reduce boilerplate)
- **JUnit 5 & Mockito** (Testing)

## Building the Project

```bash
# From root directory
mvn clean install

# Run tests
mvn test

# Package
mvn clean package
```

## Running the Application

```bash
# From root
cd bootstrap
mvn spring-boot:run

# Or run JAR
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar
```

Application runs on **port 5000**.

## Testing with Mocks

Start mock services (from `backendDevTest` directory):

```bash
docker-compose up -d simulado influxdb grafana
```

Test the endpoint:

```bash
curl http://localhost:5000/product/1/similar
```

## API Contract

### GET /product/{productId}/similar

Returns similar products details.

**Success (200)**:
```json
[
  {
    "id": "2",
    "name": "Product Name",
    "price": 99.99,
    "availability": true
  }
]
```

**Not Found (404)**:
```json
{
  "title": "Product Not Found",
  "status": 404,
  "detail": "Product not found: 999"
}
```

## Hexagonal Architecture Principles

### 1. Domain Layer (domain/)
- **Pure business logic**
- **No external dependencies** (not even Spring!)
- Defines **ports** (interfaces):
  - **Input ports** (Use Cases): What the app does
  - **Output ports** (SPIs): What the app needs

### 2. Application Layer (application/)
- **Orchestrates business logic**
- Implements **Input Ports** (Use Cases)
- Uses **Output Ports** to access infrastructure
- **No Spring**, no framework dependencies

### 3. Infrastructure Layer (infrastructure/)
- **Adapters** implement Output Ports
- **Primary adapters** (REST controllers): Call Use Cases
- **Secondary adapters** (HTTP clients, DB): Implement Output Ports
- Contains all **technical concerns**

### 4. Bootstrap Layer (bootstrap/)
- **Spring Boot main** application
- **Dependency injection** configuration
- Wires everything together

## Key Benefits

✅ **Testability**: Domain and Application are pure Java - easy to test  
✅ **Flexibility**: Swap adapters without changing business logic  
✅ **Independence**: Domain doesn't know about REST, HTTP, Spring  
✅ **Maintainability**: Clear separation of concerns  
✅ **Enforcement**: Maven prevents architectural violations

## Performance Features

- **Parallel product fetching** (CompletableFuture)
- **Circuit Breaker** to prevent cascading failures
- **Retry with exponential backoff**
- **Configurable timeouts**

## CI/CD & Performance Testing

### GitHub Actions Workflow

Automated CI/CD pipeline with performance testing:

```bash
# Triggers on push to any branch
Build Job:
├── Unit Tests
├── Maven Build
└── ✅ Success

Performance Job (after build):
├── Independent Maven Build
├── Start Application
├── K6 Performance Tests (320+ RPS)
└── API Verification
```

**Architecture:** Each job builds independently for maximum reliability.

### Performance Testing with K6

- **Load testing** with 200 virtual users
- **Multiple scenarios**: normal, error, slow, timeout
- **Realistic test patterns**
- **Performance metrics**: RPS, latency, error rates

View results in GitHub Actions after each commit.

### Key Metrics Achieved

- ✅ **320+ requests/second**
- ✅ **P95 < 30ms response time**  
- ✅ **<1% error rate**
- ✅ **Circuit breaker protection**
- ✅ **Robust CI/CD pipeline**

## SOLID Principles

- **Single Responsibility**: Each module has one reason to change
- **Open/Closed**: Extend via new adapters, don't modify core
- **Liskov Substitution**: Adapters are interchangeable
- **Interface Segregation**: Specific ports for each responsibility
- **Dependency Inversion**: All depend on abstractions (ports)

## Development Workflow

### Order of Implementation

1. **domain**: Define model and port interfaces
2. **application**: Implement use cases using ports
3. **infrastructure**: Implement adapters for ports
4. **bootstrap**: Wire everything with Spring

### Testing Strategy

- **domain**: No tests needed (just interfaces and models)
- **application**: Unit tests with mocked ports
- **infrastructure**: Integration tests with WireMock
- **bootstrap**: E2E tests with @SpringBootTest

## Configuration

Key settings in `bootstrap/src/main/resources/application.yml`:

```yaml
# Server configuration
server:
  port: 5000

# Product service configuration  
product:
  service:
    base-url: http://localhost:3001

# Similar products service configuration
similar-products:
  service:
    base-url: http://localhost:3002

# Circuit breaker configuration
resilience4j:
  circuit-breaker:
    instances:
      productService:
        register-health-indicator: true
        failure-rate-threshold: 50
        wait-duration-in-open-state: 5s
        automatic-transition-from-open-to-half-open-enabled: true
```

## Available Scripts

Essential development scripts located in `scripts/`:

- **`run-all-tests.ps1`** - Execute complete test suite
- **`run-performance-tests.ps1`** - Run K6 performance tests  
- **`run-production-tests.ps1`** - Production environment testing
- **`setup-grafana-dashboard.ps1`** - Setup monitoring dashboard
- **`smoke-tests.ps1`** - Post-deployment verification tests

## License

Technical test project.
