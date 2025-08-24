#!/bin/bash

clear

rm -R $HOME/.m2/repository/vn/com/lcx 2> /dev/null || echo "Not found m2/repository/vn/com/lcx"
rm -R common-lib/target               2> /dev/null || echo "Not found common-lib/target"
rm -R target                          2> /dev/null || echo "Not found target"
rm -R processor/target                2> /dev/null || echo "Not found processor/target"
rm -R index-merge-plugin/target       2> /dev/null || echo "Not found index-merge-plugin/target"
