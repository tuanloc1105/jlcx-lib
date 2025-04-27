#!/bin/bash

rm -R src/main/resources/webroot/* 1> /dev/null 2> /dev/null || printf "\n\n\t No webroot folder\n\n\n"

cd web

pnpm run build
