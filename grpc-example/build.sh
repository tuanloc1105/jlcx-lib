#!/bin/bash

export JAVA_HOME="$HOME/dev-kit/jdk-11"
export MAVEN_HOME="$HOME/dev-kit/maven"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"

protoc -I=proto \
--plugin=protoc-gen-grpc-java=$DEV_KIT_LOCATION/tool/protoc-gen-grpc-java \
--plugin=protoc-gen-vertx=$DEV_KIT_LOCATION/tool/protoc-gen-vertx \
--java_out=./grpc-server/src/main/java \
--grpc-java_out=./grpc-server/src/main/java \
--vertx_out=./grpc-server/src/main/java \
./proto/*.proto

protoc -I=proto \
--plugin=protoc-gen-grpc-java=$DEV_KIT_LOCATION/tool/protoc-gen-grpc-java \
--plugin=protoc-gen-vertx=$DEV_KIT_LOCATION/tool/protoc-gen-vertx \
--java_out=./grpc-client/src/main/java \
--grpc-java_out=./grpc-client/src/main/java \
--vertx_out=./grpc-client/src/main/java \
./proto/*.proto

printf "\n\n >> Checking java version \n\n\n"

java -version 2>&1

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
