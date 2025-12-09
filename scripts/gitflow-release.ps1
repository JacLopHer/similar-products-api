# GitFlow Release Finalization Script - SIMPLIFIED
# Usage: .\gitflow-release.ps1 -Version "1.0.0"
# NOTE: This script should be run AFTER the release PR has been merged to master

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

Write-Header "GitFlow Release Finalization v$Version"
Write-Warning "This script should be run AFTER the release PR has been merged to master"

# Validate version format
if ($Version -notmatch '^\d+\.\d+\.\d+$') {
    Write-Error "❌ Invalid version format. Use format: X.Y.Z (e.g., 1.0.0)"
    exit 1
}

if ($DryRun) {
    Write-Warning "DRY RUN MODE - No actual operations will be performed"
    Write-Host ""
}

try {
    # Step 1: Check current branch and status
    Write-Step "Checking Git repository status..."

    $currentBranch = & git rev-parse --abbrev-ref HEAD
    $status = & git status --porcelain

    if ($status) {
        Write-Error "❌ Working directory is not clean. Please commit or stash changes."
        Write-Host "Uncommitted changes:"
        & git status --short
        exit 1
    }

    Write-Success "Repository is clean"
    Write-Host "Current branch: $currentBranch" -ForegroundColor White

    # Step 2: Ensure we're on master and up to date
    Write-Step "Ensuring we're on master branch and up to date..."

    if (-not $DryRun) {
        # Switch to master if not already there
        if ($currentBranch -ne "master") {
            Write-Host "Switching from $currentBranch to master..." -ForegroundColor Yellow
            & git checkout master
            if ($LASTEXITCODE -ne 0) {
                Write-Error "❌ Failed to checkout master"
                exit 1
            }
        }

        # Pull latest changes from remote
        & git pull origin master
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to pull latest master"
            exit 1
        }
    }

    Write-Success "On master branch and up to date"

    # Step 3: Create Git tag for the release
    Write-Step "Creating release tag v$Version on master..."

    if (-not $DryRun) {
        # Check if tag already exists
        $tagExists = & git tag -l "v$Version"
        if ($tagExists) {
            Write-Error "❌ Tag v$Version already exists"
            exit 1
        }

        & git tag -a "v$Version" -m "Release version $Version"
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to create tag"
            exit 1
        }
    }

    Write-Success "Git tag v$Version created on master"

    # Step 4: Verify the release build works
    Write-Step "Verifying release build..."

    if (-not $DryRun) {
        & mvn clean package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Release build verification failed"
            exit 1
        }
    }

    Write-Success "Release build verification passed"

    # Final success message
    Write-Header "Release v$Version Finalized Successfully!"

    Write-Host ""
    Write-Host "📋 Next steps:" -ForegroundColor Cyan
    Write-Host "  1. Push the tag: git push origin v$Version" -ForegroundColor White
    Write-Host "  2. GitHub Actions will create the release automatically" -ForegroundColor White
    Write-Host "  3. Delete release branch: git branch -d release/v$Version" -ForegroundColor White
    Write-Host ""
    Write-Host "📊 Final JAR: bootstrap/target/bootstrap-$Version.jar" -ForegroundColor White
    Write-Host ""
    Write-Host "🎉 Release process completed!" -ForegroundColor Green

    if ($DryRun) {
        Write-Warning "This was a DRY RUN - no actual changes were made"
        Write-Host "Run without -DryRun flag to finalize the release" -ForegroundColor White
    }

} catch {
    Write-Error "❌ Error during release finalization: $_"
    Write-Host ""
    Write-Host "🔧 Recovery steps:" -ForegroundColor Yellow
    Write-Host "  1. Check current status: git status" -ForegroundColor White
    Write-Host "  2. Ensure you're on master: git checkout master" -ForegroundColor White
    Write-Host "  3. Verify release PR was merged properly" -ForegroundColor White
    exit 1
}
