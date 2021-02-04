#!/bin/sh

CONTAINER_NAME=api-monitoring

IMAGE_NAME=api-monitoring

CURRENT_DIR=$(pwd)

if [ $(docker ps -a --filter name=$CONTAINER_NAME --filter status=running | wc -l) = 2 ]
then
    echo "Container already have been run"
    exit 0
fi

if [ $(docker ps -a --filter name=$CONTAINER_NAME --filter status=exited | wc -l) = 2 ]
then
    echo "Container stopped, try start..."
    docker start $CONTAINER_NAME
    exit 0
fi

docker run -d -m 512m --mount type=bind,source="$CURRENT_DIR/config",target="/opt/$CONTAINER_NAME/config" \
    --mount type=bind,source="$CURRENT_DIR/logs",target="/opt/$CONTAINER_NAME/logs" \
    --name=$CONTAINER_NAME $IMAGE_NAME

docker ps -a --filter name=$CONTAINER_NAME