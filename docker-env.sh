#!/usr/bin/env bash

export MONGO_PORT=27017
export RC_PORT=44443
export RC_VERSION=${RC_VERSION:-"develop"}

# Rocket

export MONGO_URL=mongodb://db:${MONGO_PORT}/rocketchat
export MONGO_OPLOG_URL=mongodb://db:${MONGO_PORT}/local
export HOME=/tmp
export PORT=${RC_PORT}
export ROOT_URL=http://localhost:${RC_PORT}
export Accounts_AvatarStorePath=/app/uploads
export ADMIN_USERNAME=admin
export ADMIN_PASS=supersecret
export ADMIN_EMAIL=admin@example.com
