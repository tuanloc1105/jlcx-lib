$env:JAVA_HOME  = "$env:DEV_KIT_LOCATION\jdk-17"
$env:MAVEN_HOME = "$env:DEV_KIT_LOCATION\maven"
$env:PATH       = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"

Write-Host "`n`n`t Checking java version`n`n`n"

java -version

Write-Host "`n`n`t Checking maven version`n`n`n"

mvn --version

mvn `
  verify `
  sonar:sonar `
  -D"sonar.projectKey=$env:SONAR_PROJECT" `
  -D"sonar.projectName=\"$env:SONAR_PROJECT\"" `
  -D"sonar.host.url=https://sonar.vtl.name.vn" `
  -D"sonar.token=$env:SONAR_TOKEN"
