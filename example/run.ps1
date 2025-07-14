Clear-Host

$env:JAVA_HOME = "D:\dev-kit\jdk-11"
$env:PATH      = "$env:JAVA_HOME\bin;$env:PATH"

java `
  "-Xms1024m" `
  "-Xmx2048m" `
  -D"user.timezone=Asia/Ho_Chi_Minh" `
  -D"file.encoding=UTF8" `
  "-Xlog:gc*:file=gc.log:time,uptime,level,tags:filecount=5,filesize=10m" `
  "-Xlog:gc*:stdout:time,uptime,level,tags" `
  "-XX:+UnlockExperimentalVMOptions" `
  "-XX:+UseZGC" `
  -jar `
  "target\todo-app-example-1.0.0-jar-with-dependencies.jar"
