#!/usr/bin/env bash
mvn clean package
cp ./music-sorter.properties ./target/
java -agentlib:native-image-agent=config-output-dir=META-INF/native-image -jar target/music-sorter.jar
