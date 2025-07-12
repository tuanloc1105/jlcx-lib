Clear-Host

Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\com\example\lcx" 2> $null
Remove-Item -Recurse -Force "target"                                          2> $null
