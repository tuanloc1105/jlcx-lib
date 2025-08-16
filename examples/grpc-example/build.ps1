Clear-Host

$env:JAVA_HOME  = "D:\dev-kit\jdk-11"
$env:MAVEN_HOME = "D:\dev-kit\maven"
$env:PATH       = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"

protoc -I=proto `
--plugin=protoc-gen-grpc-java=$env:DEV_KIT_LOCATION\tool\protoc-gen-grpc-java.exe `
--plugin=protoc-gen-vertx=$env:DEV_KIT_LOCATION\tool\protoc-gen-vertx.exe `
--java_out=.\grpc-server\src\main\java `
--grpc-java_out=.\grpc-server\src\main\java `
--vertx_out=.\grpc-server\src\main\java `
.\proto\*.proto

protoc -I=proto `
--plugin=protoc-gen-grpc-java=$env:DEV_KIT_LOCATION\tool\protoc-gen-grpc-java.exe `
--plugin=protoc-gen-vertx=$env:DEV_KIT_LOCATION\tool\protoc-gen-vertx.exe `
--java_out=.\grpc-client\src\main\java `
--grpc-java_out=.\grpc-client\src\main\java `
--vertx_out=.\grpc-client\src\main\java `
.\proto\*.proto

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
