#!/usr/bin/env bash
git checkout develop
git branch -D master
./mvnw gitflow:release-start && \
 conventional-changelog -p angular -i CHANGELOG.md -s -r 0 && \
 git add CHANGELOG.md && \
 git commit -m "Changelog" && \
 ./mvnw gitflow:release-finish -DnoDeploy=true && \
 git push origin main && git push --tags && \
 git checkout develop
