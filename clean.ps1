Clear-Host

Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\vn\com\lcx" *> $null ; if (!$?) { Write-Host "Not found m2\repository\vn\com\lcx" }
Remove-Item -Recurse -Force "common-lib\target"                          *> $null ; if (!$?) { Write-Host "Not found common-lib\target" }
Remove-Item -Recurse -Force "processor\target"                           *> $null ; if (!$?) { Write-Host "Not found processor\target" }
Remove-Item -Recurse -Force "target"                                     *> $null ; if (!$?) { Write-Host "Not found target" }
