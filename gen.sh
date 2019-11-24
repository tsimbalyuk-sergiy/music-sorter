#!/usr/bin/env bash
mvn clean package
java -agentlib:native-image-agent=config-output-dir=META-INF/native-image -jar target/music-sorter.jar
