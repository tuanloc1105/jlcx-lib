Clear-Host

Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\com\example\lcx" 2> $null ; if (!$?) { Write-Host "Not found m2\repository\com\example\lcx" }
Remove-Item -Recurse -Force "target"                                          2> $null ; if (!$?) { Write-Host "Not found target" }
# Remove-Item -Recurse -Force web\node_modules                                  2> $null ; if (!$?) { Write-Host "Not found web\node_modules" }
