Clear-Host

$env:JAVA_HOME  = "$env:DEV_KIT_LOCATION\jdk-11"
$env:MAVEN_HOME = "$env:DEV_KIT_LOCATION\maven"
$env:PATH       = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"

Write-Host "`n`n`t Checking java version`n`n`n"

java -version

Write-Host "`n`n`t Checking maven version`n`n`n"

mvn --version

mvn `
  deploy `
  -D"altDeploymentRepository=nexus::https://nexus.vtl.name.vn/repository/maven-releases/" `
  -D"skipTests=true" `
  -D"file.encoding=UTF8" `
  -f `
  pom.xml
