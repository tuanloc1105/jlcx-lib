Clear-Host

Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\com\example\lcx" *> $null ; if (!$?) { Write-Host "Not found m2\repository\com\example\lcx" }
Remove-Item -Recurse -Force "target"                                          *> $null ; if (!$?) { Write-Host "Not found target" }
# Remove-Item -Recurse -Force web\node_modules                                  *> $null ; if (!$?) { Write-Host "Not found web\node_modules" }
