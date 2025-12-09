# GitFlow Release Preparation Script - FIXED
param(
    [Parameter(Mandatory=$true)]
    [string]$Version,
    [Parameter()]
    [switch]$DryRun
)

function Write-Header {
    param($Message)
    Write-Host ""
    Write-Host "=" * 60 -ForegroundColor Blue
    Write-Host $Message -ForegroundColor Yellow
    Write-Host "=" * 60 -ForegroundColor Blue
}

function Write-Step {
    param($Message)
    Write-Host "🔄 $Message" -ForegroundColor Cyan
}

function Write-Success {
    param($Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Warning {
    param($Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
}

Write-Header "GitFlow Release Preparation v$Version"

# Validate version format
if ($Version -notmatch '^\d+\.\d+\.\d+$') {
    Write-Error "❌ Invalid version format. Use format: X.Y.Z (e.g., 1.0.0)"
    exit 1
}

if ($DryRun) {
    Write-Warning "DRY RUN MODE - No actual Git operations will be performed"
}

try {
    Write-Step "Checking Git repository status..."

    $currentBranch = & git rev-parse --abbrev-ref HEAD
    $status = & git status --porcelain

    if ($status) {
        Write-Error "❌ Working directory is not clean. Please commit or stash changes."
        exit 1
    }

    Write-Success "Repository is clean. Current branch: $currentBranch"

    # Switch to master
    Write-Step "Switching to master branch..."
    if (-not $DryRun) {
        if ($currentBranch -ne "master") {
            & git checkout master
            if ($LASTEXITCODE -ne 0) {
                Write-Error "❌ Failed to checkout master"
                exit 1
            }
        }
        & git pull origin master
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to pull master"
            exit 1
        }
    }
    Write-Success "On master and up to date"

    # Create release branch
    $releaseBranch = "release/v$Version"
    Write-Step "Creating release branch: $releaseBranch"
    if (-not $DryRun) {
        & git checkout -b $releaseBranch
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to create release branch"
            exit 1
        }
    }
    Write-Success "Release branch created"

    # Merge develop
    Write-Step "Merging develop into release branch..."
    if (-not $DryRun) {
        & git merge develop --no-ff -m "merge: integrate develop into release v$Version"
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to merge develop"
            exit 1
        }
    }
    Write-Success "Develop merged into release"

    # Update version
    Write-Step "Updating version to $Version..."
    if (-not $DryRun) {
        & mvn versions:set -DnewVersion=$Version -DgenerateBackupPoms=false
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to update version"
            exit 1
        }
    }
    Write-Success "Version updated"

    # Run tests
    Write-Step "Running tests..."
    if (-not $DryRun) {
        & mvn clean test
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Tests failed"
            exit 1
        }
    }
    Write-Success "Tests passed"

    # Build package
    Write-Step "Building package..."
    if (-not $DryRun) {
        & mvn clean package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Build failed"
            exit 1
        }
    }
    Write-Success "Package built"

    # Commit changes
    Write-Step "Committing changes..."
    if (-not $DryRun) {
        & git add .
        & git commit -m "release: prepare v$Version"
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to commit"
            exit 1
        }
    }
    Write-Success "Changes committed"

    Write-Header "Release Branch v$Version Ready!"
    Write-Host "Next steps:"
    Write-Host "1. git push origin $releaseBranch"
    Write-Host "2. Create PR: $releaseBranch → master"
    Write-Host "3. Merge PR"
    Write-Host "4. Run: .\scripts\gitflow-release.ps1 -Version '$Version'"

} catch {
    Write-Error "❌ Error: $_"
    exit 1
}
