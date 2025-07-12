Clear-Host

$env:JAVA_HOME  = "D:\dev-kit\jdk-11"
$env:MAVEN_HOME = "D:\dev-kit\maven"
$env:PATH       = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"

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
