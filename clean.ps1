Clear-Host

Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\vn\com\lcx" 2> $null ; if (!$?) { Write-Host "Not found m2\repository\vn\com\lcx" }
Remove-Item -Recurse -Force "common-lib\target"                          2> $null ; if (!$?) { Write-Host "Not found common-lib\target" }
Remove-Item -Recurse -Force "grpc-proto-plugin\target"                   2> $null ; if (!$?) { Write-Host "Not found grpc-proto-plugin\target" }
Remove-Item -Recurse -Force "processor\target"                           2> $null ; if (!$?) { Write-Host "Not found processor\target" }
Remove-Item -Recurse -Force "target"                                     2> $null ; if (!$?) { Write-Host "Not found target" }
