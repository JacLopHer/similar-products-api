# GitFlow Maven Plugin - Quick Reference
# This file shows how to use the GitFlow Maven Plugin for release management

Write-Host "🚀 GitFlow Maven Plugin - Quick Reference" -ForegroundColor Green
Write-Host ""

Write-Host "📋 Available Commands:" -ForegroundColor Cyan
Write-Host ""

Write-Host "🎯 Release Management:" -ForegroundColor Yellow
Write-Host "  mvn gitflow:release-start -DreleaseVersion=1.0.0" -ForegroundColor White
Write-Host "  mvn gitflow:release-finish -DreleaseVersion=1.0.0" -ForegroundColor White
Write-Host "  mvn gitflow:release-list" -ForegroundColor Gray
Write-Host ""

Write-Host "🔧 Feature Management:" -ForegroundColor Yellow
Write-Host "  mvn gitflow:feature-start -DfeatureName=my-feature" -ForegroundColor White
Write-Host "  mvn gitflow:feature-finish -DfeatureName=my-feature" -ForegroundColor White
Write-Host "  mvn gitflow:feature-list" -ForegroundColor Gray
Write-Host ""

Write-Host "🔥 Hotfix Management:" -ForegroundColor Yellow
Write-Host "  mvn gitflow:hotfix-start -DreleaseVersion=1.0.1" -ForegroundColor White
Write-Host "  mvn gitflow:hotfix-finish -DreleaseVersion=1.0.1" -ForegroundColor White
Write-Host "  mvn gitflow:hotfix-list" -ForegroundColor Gray
Write-Host ""

Write-Host "📖 Complete Release Process:" -ForegroundColor Cyan
Write-Host "  1. git checkout develop" -ForegroundColor White
Write-Host "  2. git add . && git commit -m 'feat: my changes'" -ForegroundColor White
Write-Host "  3. git push origin develop" -ForegroundColor White
Write-Host "  4. mvn gitflow:release-start -DreleaseVersion=1.0.0" -ForegroundColor White
Write-Host "  5. mvn gitflow:release-finish -DreleaseVersion=1.0.0" -ForegroundColor White
Write-Host "  6. git push origin master develop v1.0.0" -ForegroundColor White
Write-Host ""

Write-Host "💡 Benefits over custom scripts:" -ForegroundColor Blue
Write-Host "  ✅ Industry standard and well-tested" -ForegroundColor Green
Write-Host "  ✅ Automatic version management across all POMs" -ForegroundColor Green
Write-Host "  ✅ Complete GitFlow workflow support" -ForegroundColor Green
Write-Host "  ✅ IntelliJ Maven tool window integration" -ForegroundColor Green
Write-Host "  ✅ Rollback support and error handling" -ForegroundColor Green
Write-Host ""
