#!/usr/bin/env bash
set -e

# Configuration
GRAAL_HOME=${GRAAL_HOME:-${JAVA_HOME}}
UPX_PATH=${UPX_PATH:-$(which upx || echo "/usr/bin/upx")}

echo "[*] Building with GraalVM native-image..."
echo "[*] GRAAL_HOME: ${GRAAL_HOME}"
echo "[*] UPX_PATH: ${UPX_PATH}"

# Clean and build
mvn clean package -DskipTests

# Prepare build directory
rm -rf ./build/
mkdir -p build
cp -f ./target/music-sorter.jar ./build/
cd ./build/

# Modern GraalVM native-image build
echo "[*] Running native-image compilation..."
${GRAAL_HOME}/bin/native-image \
  --no-fallback \
  --report-unsupported-elements-at-runtime \
  -H:+ReportExceptionStackTraces \
  -H:+UnlockExperimentalVMOptions \
  -H:+UseServiceLoaderFeature \
  -H:+RemoveSaturatedTypeFlows \
  --gc=serial \
  -H:ReflectionConfigurationFiles=../META-INF/native-image/reflect-config.json \
  -H:ResourceConfigurationFiles=../META-INF/native-image/resource-config.json \
  -H:JNIConfigurationFiles=../META-INF/native-image/jni-config.json \
  -H:DynamicProxyConfigurationFiles=../META-INF/native-image/proxy-config.json \
  --initialize-at-run-time=org.jaudiotagger,org.tinylog.runtime.ModernJavaRuntime \
  -jar music-sorter.jar \
  music-sorter

echo "[*] Native binary created successfully!"
ls -lh ./music-sorter

# Optional UPX compression
if [[ -f "${UPX_PATH}" ]]; then
    echo "[*] Compressing with UPX..."
    ${UPX_PATH} --best ./music-sorter -o music-sorter-compressed
    echo "[*] Compression complete!"
    ls -lh ./music-sorter-compressed
else
    echo "[WARN] UPX not found at ${UPX_PATH}, skipping compression"
    echo "[INFO] To enable compression, install UPX or set UPX_PATH environment variable"
fi

echo "[SUCCESS] Native binary build complete!"
