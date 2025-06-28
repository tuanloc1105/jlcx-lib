$currentLocation = $(Get-Location)

Remove-Item -Recurse src\main\resources\webroot\* *> $null || Write-Host "`n`n`t No webroot folder`n`n`n"

Set-Location web

pnpm run build

Set-Location $currentLocation
