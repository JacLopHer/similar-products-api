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

**Server Configuration (from application.yml):**
- **Port**: 5000
- **Connection Timeout**: 20s  
- **Idle Timeout**: 60s
- **External API Base URL**: http://localhost:3001
- **API Timeout**: 2000ms

```bash
# From root
cd bootstrap
mvn spring-boot:run

# Or run JAR directly
java -jar bootstrap/target/bootstrap-1.0.0.jar
```

**Available Endpoints:**
- **Main API**: http://localhost:5000/product/{id}/similar
- **Version Info**: http://localhost:5000/api/v1/version
- **Health Check**: http://localhost:5000/actuator/health
- **Build Info**: http://localhost:5000/actuator/info

## Testing with Mocks

Start mock services (from `backendDevTest` directory):

```bash
docker-compose up -d simulado influxdb grafana
```

Test the endpoints:

```bash
# Test main API
curl http://localhost:5000/product/1/similar

# Test version endpoint
curl http://localhost:5000/api/v1/version

# Test health check
curl http://localhost:5000/actuator/health
```

## API Contract

### GET /product/{productId}/similar

Returns similar products details.

### GET /api/v1/version

Returns application version and build information.

**Success (200)**:
```json
{
  "application": "Similar Products API",
  "version": "1.0.0",
  "buildTime": "2025-12-09T19:25:44.123Z",
  "gitCommit": "abc123f",
  "gitBranch": "main"
}
```

### GET /api/v1/info

Returns comprehensive build and git information.

### GET /actuator/health

Returns application health status.

### GET /actuator/info

Returns build information via Spring Boot Actuator.

## Product Endpoint Response

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
- **`prepare-release.ps1`** - Create and prepare release branch from develop
- **`gitflow-release.ps1`** - Create official release on master branch
- **`release.ps1`** - Simple release management script (legacy)

## Release Management

### GitFlow Process

This project follows **GitFlow** branching strategy:
- **`master`** - Production-ready code, only releases
- **`develop`** - Integration branch for features
- **`feature/*`** - Feature development branches
- **`release/*`** - Release preparation branches
- **`hotfix/*`** - Emergency fixes for production

### GitFlow Release Process (Correct & Simplified)

This project follows a simplified GitFlow strategy:

#### Complete Release Flow

```bash
# 1. INTEGRATE CHANGES TO DEVELOP
git checkout develop
git add .
git commit -m "feat: add version management and CI/CD improvements"
git push origin develop

# 2. PREPARE RELEASE (scripts handle git checkout automatically)
.\scripts\prepare-release.ps1 -Version "1.0.0"
# This script automatically:
# - Switches to master branch
# - Creates release/v1.0.0 FROM master
# - Merges develop INTO release branch
# - Updates version to 1.0.0
# - Runs tests and builds package

# 3. PUSH RELEASE BRANCH AND CREATE PR
git push origin release/v1.0.0
# Create PR: release/v1.0.0 → master

# 4. MERGE PR AND FINALIZE (scripts handle git checkout automatically)
# (After PR is merged through GitHub)
.\scripts\gitflow-release.ps1 -Version "1.0.0"
# This script automatically:
# - Switches to master branch 
# - Creates git tag v1.0.0

# 5. PUSH TAG
git push origin v1.0.0

# 6. CLEANUP
git branch -d release/v1.0.0
```

#### Why This Process Works

- ✅ **Stable base**: Release branch starts from stable master
- ✅ **Feature integration**: Develop is merged into release branch
- ✅ **PR review**: Release → master goes through PR process
- ✅ **Clean finish**: No complex merge-back logic
- ✅ **Automated release**: GitHub Actions triggers on tag push

#### Available Scripts

- **`prepare-release.ps1`** - Creates release branch (master→release←develop)
- **`gitflow-release.ps1`** - Finalizes release (creates tag after PR merge)
- **`release.ps1`** - Legacy simple release script

#### Script Usage

```powershell
# Step 1: Prepare release branch
.\scripts\prepare-release.ps1 -Version "1.0.0" -DryRun  # Test first
.\scripts\prepare-release.ps1 -Version "1.0.0"         # Real execution

# Step 2: After PR merge, finalize release  
.\scripts\gitflow-release.ps1 -Version "1.0.0" -DryRun  # Test first
.\scripts\gitflow-release.ps1 -Version "1.0.0"         # Real execution
```

#### GitHub Actions Workflows

- **Pull Request Workflow** (`.github/workflows/performance.yml`):
  - Triggers only on Pull Requests to `master` or `develop`
  - Runs tests and performance validation
  - Manual trigger available via `workflow_dispatch`

- **Release Workflow** (`.github/workflows/release.yml`):
  - Triggers on version tags (`v*.*.*`)
  - Creates GitHub Release with JAR artifact
  - Runs quality checks and performance tests
  - Manual trigger available with version input

#### GitFlow Release Process

**For feature development:**
```bash
# 1. Create feature branch from develop
git checkout develop
git pull origin develop
git checkout -b feature/your-feature-name

# 2. Develop and test
# ... make your changes ...

# 3. Create PR to develop branch
git push origin feature/your-feature-name
```

**For releases (from develop to master):**
```bash
# 1. Create release branch from develop
git checkout develop
git pull origin develop
git checkout -b release/v1.1.0

# 2. Prepare release (run release script)
.\scripts\release.ps1 -Version "1.1.0"

# 3. Merge to master and tag
git checkout master
git merge release/v1.1.0
git push origin master
git push origin v1.1.0

# 4. Merge back to develop
git checkout develop
git merge release/v1.1.0
git push origin develop
```

#### Version Information

The application exposes version information through multiple endpoints:

- `GET /api/v1/version` - Clean JSON version info
- `GET /api/v1/info` - Comprehensive build and git information  
- `GET /actuator/info` - Spring Boot Actuator build info
- `GET /actuator/health` - Health status

#### Versioning Strategy

- **Semantic Versioning**: `MAJOR.MINOR.PATCH`
- **Git Tags**: Automatically created with `v` prefix (`v1.0.0`)
- **Build Information**: Includes Git commit, branch, and build timestamp
- **JAR Naming**: `similar-products-api-{version}.jar`

## License

Technical test project.
