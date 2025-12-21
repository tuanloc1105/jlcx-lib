#!/bin/bash

CURRENT_RUNNING_PID=$(ps -ef | grep 'hibernate-reactive-example-1.0.0.jar' | grep -v 'grep' | awk '{print $2}')

if [[ -n "$CURRENT_RUNNING_PID" ]]
then
    for VAR in $CURRENT_RUNNING_PID
    do
        echo "Killing old app $VAR"
        kill $VAR
    done
else
    echo "No running app"
fi
