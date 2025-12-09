# Release Management Script
# Usage: .\release.ps1 -Version "1.1.0"

param(
    [Parameter(Mandatory=$true)]
    [string]$Version
)

Write-Host "🚀 Preparing release v$Version..." -ForegroundColor Green

# Validate version format
if ($Version -notmatch '^\d+\.\d+\.\d+$') {
    Write-Error "❌ Invalid version format. Use format: X.Y.Z (e.g., 1.1.0)"
    exit 1
}

try {
    # 1. Update versions in all pom.xml files
    Write-Host "📝 Updating versions in pom.xml files..." -ForegroundColor Yellow

    # Use Maven Release Plugin to update versions
    & mvn versions:set -DnewVersion=$Version -DgenerateBackupPoms=false

    if ($LASTEXITCODE -ne 0) {
        Write-Error "❌ Error updating versions"
        exit 1
    }

    # 2. Run complete test suite
    Write-Host "🧪 Running complete test suite..." -ForegroundColor Yellow
    & mvn clean test

    if ($LASTEXITCODE -ne 0) {
        Write-Error "❌ Tests failed"
        exit 1
    }

    # 3. Build and package application
    Write-Host "📦 Building and packaging application..." -ForegroundColor Yellow
    & mvn clean package -DskipTests

    if ($LASTEXITCODE -ne 0) {
        Write-Error "❌ Error packaging application"
        exit 1
    }

    # 4. Create Git tag
    Write-Host "🏷️ Creating Git tag..." -ForegroundColor Yellow
    & git add .
    & git commit -m "chore: release v$Version"
    & git tag -a "v$Version" -m "Release version $Version"

    if ($LASTEXITCODE -ne 0) {
        Write-Error "❌ Error creating tag"
        exit 1
    }

    Write-Host "✅ Release v$Version prepared successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 Next steps:" -ForegroundColor Cyan
    Write-Host "  1. Push commits: git push origin master" -ForegroundColor White
    Write-Host "  2. Push tag: git push origin v$Version" -ForegroundColor White
    Write-Host "  3. GitHub Actions workflow will automatically create the release" -ForegroundColor White
    Write-Host ""
    Write-Host "📊 Generated JAR: bootstrap/target/bootstrap-$Version.jar" -ForegroundColor White

} catch {
    Write-Error "❌ Error during release process: $_"
    exit 1
}
