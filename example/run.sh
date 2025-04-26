#!/bin/bash

clear

export JAVA_HOME="/home/loc/dev-kit/jdk-11"
export PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin:$JAVA_HOME/bin"

printf "\n\n >> Checking java version \n\n\n"

java -version

java \
 -server \
 -Duser.timezone=Asia/Ho_Chi_Minh \
 -Dfile.encoding=UTF8 \
 -jar \
 target/main-app-jar-with-dependencies.jar
