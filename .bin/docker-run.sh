#!/usr/bin/env bash

source ./.bin/docker-env.sh

# Starts all applications. Run 'mvn docker:build' in platform and webapp before this script to create docker images.

# Remove existing containers
docker compose stop
docker compose rm -f

## Start new containers

docker compose up -d

while ! echo exit | nc localhost ${MONGO_PORT}; do sleep 30; done
echo "MongoDB up and running"

while ! echo exit | nc localhost ${RC_PORT}; do sleep 30; done
echo "RocketChat up and running"

sleep 30
