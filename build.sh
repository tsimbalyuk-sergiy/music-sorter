#!/usr/bin/env bash
set -e
GRAAL=${JAVA_HOME}
UPX=/usr/bin/upx

mvn clean package

rm -rf ./build/

[[ -d build ]] || mkdir -p build
cp -f ./target/music-sorter.jar ./build/
cd ./build/

echo GRAAL native-image
"${GRAAL}"/bin/native-image -H:+ReportExceptionStackTraces \
  --no-server \
  --no-fallback \
  --language:llvm \
  --allow-incomplete-classpath \
  --class-path music-sorter.jar \
  --report-unsupported-elements-at-runtime \
  -H:+TraceClassInitialization \
  -H:ReflectionConfigurationFiles=../META-INF/native-image/reflect-config.json \
  -H:IncludeResources=../META-INF/native-image/resource-config.json \
  -H:Log=registerResource \
  -H:+ReportExceptionStackTraces \
  -H:-AllowVMInspection \
  -jar music-sorter.jar

echo UPX
${UPX} ./music-sorter -omusic-sorter-compressed
ls -lh ./music-sorter
ls -lh ./music-sorter-compressed
