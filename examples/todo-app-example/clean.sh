#!/bin/bash

clear

rm -R $HOME/.m2/repository/com/example/lcx 2> /dev/null || echo "Not found m2/repository/com/example/lcx"
rm -R target                               2> /dev/null || echo "Not found target"
#rm -R web/node_modules                     2> /dev/null || echo "Not found web/node_modules"
