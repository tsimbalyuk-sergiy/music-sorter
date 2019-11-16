#!/usr/bin/env bash
set -e
GRAAL=${JAVA_HOME}
UPX=/usr/bin/upx

mvn clean package
CP=$(mvn -q exec:exec -Dexec.executable=echo -Dexec.args="%classpath")

rm -rf ./build/

[[ -d build ]] || mkdir -p build
cp -f ./target/music-sorter.jar ./build/
cd ./build/

echo GRAAL native-image
${GRAAL}/bin/native-image -H:+ReportExceptionStackTraces \
              -H:+TraceClassInitialization \
              -H:ReflectionConfigurationFiles=../reflect-config.json \
              --no-server \
              --language:llvm \
              --allow-incomplete-classpath \
              --class-path music-sorter.jar \
              --report-unsupported-elements-at-runtime \
              -H:Log=registerResource \
              -H:+ReportUnsupportedElementsAtRuntime \
              -H:+ReportExceptionStackTraces \
              -H:-AllowVMInspection \
              -jar music-sorter.jar

echo UPX
${UPX} ./music-sorter -omusic-sorter-compressed
ls -lh ./music-sorter
ls -lh ./music-sorter-compressed
