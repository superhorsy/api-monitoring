#!/bin/sh

NAME=api-monitoring

if [ $(docker ps -a --filter ancestor=$NAME --filter status=running | wc -l) = 2 ]
then
    echo "There are run containers for images $NAME. Stop and remove it? [y|N]"
    read ANSWER
    if [ "$ANSWER" = "y" ]
    then
        docker stop $(docker ps -q --filter ancestor=$NAME)
        docker rm -v $(docker ps -a -q --filter ancestor=$NAME)
    else
        exit 0
    fi
fi

if [ $(docker ps -a --filter ancestor=$NAME --filter status=exited | wc -l) = 2 ]
then
    echo "There are stopped containers for images $NAME. Remove it? [y|N]"
    read ANSWER
    if [ "$ANSWER" = "y" ]
    then
        docker rm -v $(docker ps -a -q --filter ancestor=$NAME)
    else
        exit 0
    fi
fi

docker rmi $NAME

docker build -t $NAME .