#!/bin/bash

clear

export JAVA_HOME="$HOME/dev-kit/jdk-11"
export MAVEN_HOME="$HOME/dev-kit/maven"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"

printf "\n\n >> Checking java version \n\n\n"

java -version 2>&1

printf "\n\n >> Checking maven version \n\n\n"

mvn --version

mvn \
  deploy \
  -DaltDeploymentRepository=nexus::https://nexus.vtl.name.vn/repository/maven-snapshots/ \
  -DskipTests=true \
  -Dfile.encoding=UTF8 \
  -f \
  pom.xml
