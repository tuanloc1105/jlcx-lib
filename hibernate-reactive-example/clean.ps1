Clear-Host

Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\com\example\hibernate-reactive-example" 2> $null ; if (!$?) { Write-Host "Not found m2\repository\com\example\hibernate-reactive-example" }
Remove-Item -Recurse -Force "target"                                                                 2> $null ; if (!$?) { Write-Host "Not found target" }
