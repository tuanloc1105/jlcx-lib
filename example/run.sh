#!/bin/bash

clear

export JAVA_HOME="/home/loc/dev-kit/jdk-11"
export PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin:$JAVA_HOME/bin"

printf "\n\n >> Checking java version \n\n\n"

java -version

# java \
#  -server \
#  -Duser.timezone=Asia/Ho_Chi_Minh \
#  -Dfile.encoding=UTF8 \
#  -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -Xloggc:data/gc.log \
#  -cp \
#  target/example-1.0.0.jar:target/lib/*:classes \
#  com.example.lcx.App

java \
 -server \
 -Duser.timezone=Asia/Ho_Chi_Minh \
 -Dfile.encoding=UTF8 \
 -Xlog:gc*:file=data/gc.log:time,uptime,level,tags:filecount=5,filesize=10m \
 -cp \
 target/example-1.0.0.jar:target/lib/*:classes \
 com.example.lcx.App
