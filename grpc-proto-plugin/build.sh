#!/bin/bash

clear

export JAVA_HOME="/home/loc/dev-kit/jdk-11"
export MAVEN_HOME="/home/loc/dev-kit/maven"
export PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin:$JAVA_HOME/bin:$MAVEN_HOME/bin"

printf "\n\n >> Checking java version \n\n\n"

java -version

printf "\n\n >> Checking maven version \n\n\n"

mvn --version

mvn \
 -Dmaven.wagon.http.ssl.insecure=true \
 -Dmaven.wagon.http.ssl.allowall=true \
 -Dmaven.wagon.http.ssl.ignore.validity.dates=true \
 -Dmaven.resolver.transport=wagon \
 dependency:resolve \
 clean \
 install \
 -DskipTests=true \
 -Dfile.encoding=UTF8 \
 -f pom.xml
