#!/bin/bash

clear

rm -R $HOME/.m2/repository/com/example/hibernate-reactive-example 2> /dev/null || echo "Not found m2/repository/com/example/hibernate-reactive-example"
rm -R target                                                      2> /dev/null || echo "Not found grpc-server/target"
