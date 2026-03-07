#!/bin/bash

clear

rm -R $HOME/.m2/repository/vn/io/lcx  2> /dev/null || echo "Not found m2"
rm -R common-lib/target               2> /dev/null || echo "Not found common-lib/target"
rm -R processor/target                2> /dev/null || echo "Not found processor/target"
