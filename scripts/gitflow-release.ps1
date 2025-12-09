# GitFlow Release Script
# Usage: .\gitflow-release.ps1 -Version "1.1.0"

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

Write-Header "GitFlow Release Process v$Version"

# Validate version format
if ($Version -notmatch '^\d+\.\d+\.\d+$') {
    Write-Error "❌ Invalid version format. Use format: X.Y.Z (e.g., 1.1.0)"
    exit 1
}

if ($DryRun) {
    Write-Warning "DRY RUN MODE - No actual Git operations will be performed"
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

    # Step 2: Ensure we're on develop and it's up to date
    Write-Step "Switching to develop branch and updating..."

    if (-not $DryRun) {
        & git checkout develop
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to checkout develop branch"
            exit 1
        }

        & git pull origin develop
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to pull latest develop"
            exit 1
        }
    }

    Write-Success "On develop branch and up to date"

    # Step 3: Create release branch
    $releaseBranch = "release/v$Version"
    Write-Step "Creating release branch: $releaseBranch"

    if (-not $DryRun) {
        & git checkout -b $releaseBranch
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to create release branch"
            exit 1
        }
    }

    Write-Success "Release branch created: $releaseBranch"

    # Step 4: Update version in project
    Write-Step "Updating project version to $Version..."

    if (-not $DryRun) {
        & mvn versions:set -DnewVersion=$Version -DgenerateBackupPoms=false
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to update version"
            exit 1
        }
    }

    Write-Success "Version updated to $Version"

    # Step 5: Run tests
    Write-Step "Running complete test suite..."

    if (-not $DryRun) {
        & mvn clean test
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Tests failed"
            exit 1
        }
    }

    Write-Success "All tests passed"

    # Step 6: Build package
    Write-Step "Building release package..."

    if (-not $DryRun) {
        & mvn clean package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Build failed"
            exit 1
        }
    }

    Write-Success "Release package built successfully"

    # Step 7: Commit release changes
    Write-Step "Committing release changes..."

    if (-not $DryRun) {
        & git add .
        & git commit -m "chore: prepare release v$Version"
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to commit release changes"
            exit 1
        }
    }

    Write-Success "Release changes committed"

    # Step 8: Merge to master
    Write-Step "Merging release to master branch..."

    if (-not $DryRun) {
        & git checkout master
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to checkout master"
            exit 1
        }

        & git pull origin master
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to pull latest master"
            exit 1
        }

        & git merge --no-ff $releaseBranch -m "release: merge release v$Version to master"
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to merge release to master"
            exit 1
        }
    }

    Write-Success "Release merged to master"

    # Step 9: Create Git tag
    Write-Step "Creating Git tag v$Version..."

    if (-not $DryRun) {
        & git tag -a "v$Version" -m "Release version $Version"
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to create tag"
            exit 1
        }
    }

    Write-Success "Git tag v$Version created"

    # Step 10: Merge back to develop
    Write-Step "Merging release back to develop..."

    if (-not $DryRun) {
        & git checkout develop
        & git merge --no-ff $releaseBranch -m "release: merge release v$Version back to develop"
        if ($LASTEXITCODE -ne 0) {
            Write-Error "❌ Failed to merge back to develop"
            exit 1
        }
    }

    Write-Success "Release merged back to develop"

    # Step 11: Clean up release branch
    Write-Step "Cleaning up release branch..."

    if (-not $DryRun) {
        & git branch -d $releaseBranch
        if ($LASTEXITCODE -ne 0) {
            Write-Warning "Could not delete release branch (might have remote tracking)"
        }
    }

    Write-Success "Release branch cleaned up"

    # Final success message
    Write-Header "GitFlow Release v$Version Completed Successfully!"

    Write-Host ""
    Write-Host "📋 Next steps:" -ForegroundColor Cyan
    Write-Host "  1. Push master: git push origin master" -ForegroundColor White
    Write-Host "  2. Push tag: git push origin v$Version" -ForegroundColor White
    Write-Host "  3. Push develop: git push origin develop" -ForegroundColor White
    Write-Host "  4. GitHub Actions will create the release automatically" -ForegroundColor White
    Write-Host ""
    Write-Host "📊 Generated JAR: bootstrap/target/bootstrap-$Version.jar" -ForegroundColor White
    Write-Host ""

    if ($DryRun) {
        Write-Warning "This was a DRY RUN - no actual changes were made"
        Write-Host "Run without -DryRun flag to execute the release" -ForegroundColor White
    }

} catch {
    Write-Error "❌ Error during GitFlow release process: $_"
    Write-Host ""
    Write-Host "🔧 Recovery steps:" -ForegroundColor Yellow
    Write-Host "  1. Check current branch: git branch" -ForegroundColor White
    Write-Host "  2. If on release branch, you can:" -ForegroundColor White
    Write-Host "     - Fix issues and continue manually" -ForegroundColor White
    Write-Host "     - Delete release branch: git branch -D $releaseBranch" -ForegroundColor White
    Write-Host "     - Return to develop: git checkout develop" -ForegroundColor White
    exit 1
}
