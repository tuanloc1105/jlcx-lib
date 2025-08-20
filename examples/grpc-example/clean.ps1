Clear-Host

Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\com\example\grpc-client" *> $null ; if (!$?) { Write-Host "Not found m2\repository\com\example\grpc-client" }
Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\com\example\grpc-server" *> $null ; if (!$?) { Write-Host "Not found m2\repository\com\example\grpc-server" }
Remove-Item -Recurse -Force "grpc-client\target"                                      *> $null ; if (!$?) { Write-Host "Not found grpc-client" }
Remove-Item -Recurse -Force "grpc-server\target"                                      *> $null ; if (!$?) { Write-Host "Not found grpc-server" }

Remove-Item -Recurse -Force grpc-server\src\main\java\examples *> $null
Remove-Item -Recurse -Force grpc-client\src\main\java\examples *> $null
