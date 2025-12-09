# Current Situation Handler for GitFlow
# This script helps you handle the current changes and prepare for GitFlow release

param(
    [Parameter()]
    [string]$CommitMessage = "feat: add version management and CI/CD improvements"
)

Write-Host "🔄 Handling current changes for GitFlow process..." -ForegroundColor Green

try {
    # Check current status
    Write-Host "📋 Checking current Git status..." -ForegroundColor Yellow

    $currentBranch = & git rev-parse --abbrev-ref HEAD
    $status = & git status --porcelain

    Write-Host "Current branch: $currentBranch" -ForegroundColor White

    if ($status) {
        Write-Host "📝 Uncommitted changes found:" -ForegroundColor Yellow
        & git status --short
        Write-Host ""

        # Add and commit current changes
        Write-Host "💾 Committing current changes..." -ForegroundColor Cyan
        & git add .
        & git commit -m $CommitMessage

        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Changes committed successfully" -ForegroundColor Green
        } else {
            Write-Error "❌ Failed to commit changes"
            exit 1
        }
    } else {
        Write-Host "✅ No uncommitted changes found" -ForegroundColor Green
    }

    # Check if we need to switch to develop
    if ($currentBranch -ne "develop") {
        Write-Host "🔄 Switching to develop branch..." -ForegroundColor Yellow

        # Check if develop exists
        $branchExists = & git show-ref --verify --quiet refs/heads/develop
        if ($LASTEXITCODE -ne 0) {
            Write-Host "📝 Creating develop branch from current branch..." -ForegroundColor Cyan
            & git checkout -b develop
        } else {
            & git checkout develop
        }

        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Now on develop branch" -ForegroundColor Green
        } else {
            Write-Error "❌ Failed to switch to develop"
            exit 1
        }
    }

    # If we had committed changes and we're now on develop, we might need to merge
    if ($currentBranch -ne "develop" -and $status) {
        Write-Host "🔄 Merging changes from $currentBranch to develop..." -ForegroundColor Yellow
        & git merge $currentBranch --no-ff -m "feat: merge version management improvements to develop"

        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Changes merged to develop" -ForegroundColor Green
        } else {
            Write-Error "❌ Failed to merge to develop"
            exit 1
        }
    }

    Write-Host ""
    Write-Host "🎉 Ready for GitFlow release process!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 Next steps:" -ForegroundColor Cyan
    Write-Host "  1. Push develop: git push origin develop" -ForegroundColor White
    Write-Host "  2. Run GitFlow release: .\scripts\gitflow-release.ps1 -Version `"1.0.0`"" -ForegroundColor White
    Write-Host "  3. Or test first: .\scripts\gitflow-release.ps1 -Version `"1.0.0`" -DryRun" -ForegroundColor White
    Write-Host ""
    Write-Host "🏗️ Current project structure:" -ForegroundColor Blue
    Write-Host "  • master  - Production branch (releases only)" -ForegroundColor White
    Write-Host "  • develop - Integration branch (your changes are here)" -ForegroundColor White
    Write-Host "  • release/v1.0.0 - Will be created by gitflow-release script" -ForegroundColor White

} catch {
    Write-Error "❌ Error handling current situation: $_"
    Write-Host ""
    Write-Host "🔧 Manual recovery:" -ForegroundColor Yellow
    Write-Host "  1. Check status: git status" -ForegroundColor White
    Write-Host "  2. Commit manually: git add . && git commit -m 'your message'" -ForegroundColor White
    Write-Host "  3. Switch to develop: git checkout develop" -ForegroundColor White
    exit 1
}
