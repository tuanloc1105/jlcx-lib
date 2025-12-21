#!/bin/bash

export JAVA_HOME="$HOME/dev-kit/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"

printf "\n\n >> Checking java version \n\n\n"

java -version 2>&1

java \
 -Xms1024m \
 -Xmx2048m \
 -Duser.timezone=Asia/Ho_Chi_Minh \
 -Dfile.encoding=UTF8 \
 -Xlog:gc*:file=gc.log:time,uptime,level,tags:filecount=5,filesize=10m \
 -Xlog:gc*:stdout:time,uptime,level,tags \
 -XX:+UnlockExperimentalVMOptions \
 -XX:+UseZGC \
 -jar \
 hibernate-reactive-example-1.0.0.jar \
 1> /dev/null 2> error.txt & echo $! > .pid

cat error.txt
