#!/bin/bash

# Exit on error
set -e

mvn clean compile -DskipTests
# Running the agent
#mvn -Pnative -Dagent exec:exec@java-agent
mvn -Pnative -Dagent=true -DskipTests -DskipNativeBuild=true package exec:exec@java-agent
# Building the native executable
mvn -Pnative -Dagent package -DskipTests
# Running the application with Maven and as a native executable
#mvn -Pnative exec:exec@native
