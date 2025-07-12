Clear-Host

$env:JAVA_HOME  = "D:\dev-kit\jdk-11"
$env:MAVEN_HOME = "D:\dev-kit\maven"
$env:PATH       = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"

$currentLocation = $(Get-Location)

Remove-Item -Recurse src\main\resources\webroot\* *> $null || Write-Host "`n`n`t No webroot folder`n`n`n"

Set-Location web

pnpm run build

Set-Location $currentLocation

Write-Host "`n`n`t Checking java version`n`n`n"

java -version

Write-Host "`n`n`t Checking maven version`n`n`n"

mvn --version

mvn `
  clean `
  install `
  -D"skipTests=true" `
  -D"file.encoding=UTF8" `
  -f `
  pom.xml
