Clear-Host

Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\com\example\hibernate-reactive-example" *> $null ; if (!$?) { Write-Host "Not found m2\repository\com\example\hibernate-reactive-example" }
Remove-Item -Recurse -Force "target"                                                                 *> $null ; if (!$?) { Write-Host "Not found target" }
