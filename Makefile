# Định nghĩa biến
JAVA_HOME := /home/loc/dev-kit/jdk-11
MAVEN_HOME := /home/loc/dev-kit/maven
PATH := /usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin:$(JAVA_HOME)/bin:$(MAVEN_HOME)/bin

export JAVA_HOME
export MAVEN_HOME
export PATH

.PHONY: all check-java check-maven build clean

all: check-java check-maven build

check-java:
	@echo ""
	@echo ">> Checking java version"
	@echo ""
	java -version

check-maven:
	@echo ""
	@echo ">> Checking maven version"
	@echo ""
	mvn --version

build:
	@echo ""
	@echo ">> Building project with Maven"
	@echo ""
	mvn \
	 dependency:resolve \
	 clean \
	 install \
	 -DskipTests=true \
	 -Dfile.encoding=UTF8 \
	 -f pom.xml

nexus-deploy:
	@echo ""
	@echo ">> Deploy to Nexus"
	@echo ""
	mvn \
	 deploy \
	 -DaltDeploymentRepository=nexus::default::http://192.168.1.196:8081/repository/maven-releases/ \
	 -DskipTests=true \
	 -Dfile.encoding=UTF8 \
	 -f pom.xml

clean:
	mvn clean
