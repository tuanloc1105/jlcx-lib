#!/bin/bash

clear

rm -R $HOME/.m2/repository/com/example/grpc-client 2> /dev/null || echo "Not found m2/repository/com/example/lcx"
rm -R $HOME/.m2/repository/com/example/grpc-server 2> /dev/null || echo "Not found m2/repository/com/example/lcx"
rm -R grpc-client/target                           2> /dev/null || echo "Not found grpc-client/target"
rm -R grpc-server/target                           2> /dev/null || echo "Not found grpc-server/target"

rm -Rf grpc-server/src/main/java/examples grpc-server/src/main/java/examples
