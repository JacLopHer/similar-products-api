# GitFlow Release Preparation Script - CORRECTED
# Usage: .\prepare-release.ps1 -Version "1.0.0"
#
# Correct flow:
# 1. Create release branch FROM MASTER
# 2. Merge develop INTO release branch
# 3. Create PR from release -> master

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
Write-Host "Flow: master → release/v$Version ← develop" -ForegroundColor Cyan

# Validate version format
if ($Version -notmatch '^\d+\.\d+\.\d+$') {
    Write-Error "❌ Invalid version format. Use format: X.Y.Z (e.g., 1.0.0)"
    exit 1
}

if ($DryRun) {
    Write-Warning "DRY RUN MODE - No actual Git operations will be performed"
    Write-Host ""
}

try {
    # Step 1: Check repository status
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

    # Step 2: Switch to master and update
    Write-Step "Switching to master branch and updating..."

    if (-not $DryRun) {
        & git checkout master
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to checkout master branch"
            exit 1
        }

        & git pull origin master
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to pull latest master"
            exit 1
        }
    }

    Write-Success "On master branch and up to date"

    # Step 3: Create release branch FROM MASTER
    $releaseBranch = "release/v$Version"
    Write-Step "Creating release branch FROM MASTER: $releaseBranch"

    if (-not $DryRun) {
        # Check if release branch already exists
        $branchExists = & git show-ref --verify --quiet refs/heads/$releaseBranch
        if ($LASTEXITCODE -eq 0) {
            Write-Error "❌ Release branch $releaseBranch already exists"
            exit 1
        }

        & git checkout -b $releaseBranch
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to create release branch from master"
            exit 1
        }
    }

    Write-Success "Release branch created from master: $releaseBranch"

    # Step 4: Merge develop INTO release branch
    Write-Step "Merging develop INTO release branch..."

    if (-not $DryRun) {
        & git merge develop --no-ff -m "merge: integrate develop into release v$Version"
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to merge develop into release branch"
            Write-Host "You may need to resolve conflicts manually" -ForegroundColor Yellow
            exit 1
        }
    }

    Write-Success "Develop merged into release branch"

    # Step 5: Update version in release branch
    Write-Step "Updating project version to $Version..."

    if (-not $DryRun) {
        & mvn versions:set -DnewVersion=$Version -DgenerateBackupPoms=false
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to update version"
            exit 1
        }
    }

    Write-Success "Version updated to $Version"

    # Step 6: Run tests on release branch
    Write-Step "Running tests on release branch..."

    if (-not $DryRun) {
        & mvn clean test
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Tests failed on release branch"
            exit 1
        }
    }

    Write-Success "All tests passed on release branch"

    # Step 7: Build final package
    Write-Step "Building release package..."

    if (-not $DryRun) {
        & mvn clean package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Build failed"
            exit 1
        }
    }

    Write-Success "Release package built successfully"

    # Step 8: Commit all release changes
    Write-Step "Committing release preparation..."

    if (-not $DryRun) {
        & git add .
        & git commit -m "release: prepare v$Version"
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to commit release changes"
            exit 1
        }
    }

    Write-Success "Release preparation committed"

    # Final success message
    Write-Header "Release Branch v$Version Ready for PR!"

    Write-Host ""
    Write-Host "📋 Next steps:" -ForegroundColor Cyan
    Write-Host "  1. Push release branch: git push origin $releaseBranch" -ForegroundColor White
    Write-Host "  2. Create PR: $releaseBranch → master" -ForegroundColor White
    Write-Host "  3. Review and merge PR to complete release" -ForegroundColor White
    Write-Host "  4. GitHub Actions will create release automatically" -ForegroundColor White
    Write-Host ""
    Write-Host "📊 Generated JAR: bootstrap/target/bootstrap-$Version.jar" -ForegroundColor White
    Write-Host ""
    Write-Host "🏗️ Release branch structure:" -ForegroundColor Blue
    Write-Host "  master (stable) → release/v$Version ← develop (features)" -ForegroundColor White

    if ($DryRun) {
        Write-Warning "This was a DRY RUN - no actual changes were made"
        Write-Host "Run without -DryRun flag to create the release branch" -ForegroundColor White
    }

} catch {
    Write-Error "❌ Error during release preparation: $_"
    Write-Host ""
    Write-Host "🔧 Recovery steps:" -ForegroundColor Yellow
    Write-Host "  1. Check current branch: git branch" -ForegroundColor White
    Write-Host "  2. If on release branch with conflicts:" -ForegroundColor White
    Write-Host "     - Resolve conflicts and continue: git add . && git commit" -ForegroundColor White
    Write-Host "     - Or abort: git merge --abort" -ForegroundColor White
    Write-Host "  3. Delete failed release branch: git checkout master && git branch -D $releaseBranch" -ForegroundColor White
    exit 1
}

