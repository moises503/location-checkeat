#!/usr/bin/env bash 

echo "cleaning and assembling"
./gradlew clean assemble
echo "publishing"
./gradlew publishToMavenLocal
