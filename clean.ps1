Clear-Host

Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\vn\com\lcx" 2> $null
Remove-Item -Recurse -Force "common-lib\target"                          2> $null
Remove-Item -Recurse -Force "grpc-proto-plugin\target"                   2> $null
Remove-Item -Recurse -Force "processor\target"                           2> $null
Remove-Item -Recurse -Force "target"                                     2> $null
