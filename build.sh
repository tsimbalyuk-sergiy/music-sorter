#!/usr/bin/env bash
set -e

GRAAL_HOME=${GRAAL_HOME:-${JAVA_HOME}}
UPX_PATH=${UPX_PATH:-$(which upx || echo "/usr/bin/upx")}

STRIP_BINARY=true
while [[ $# -gt 0 ]]; do
    case $1 in
        --no-strip)
            STRIP_BINARY=false
            shift
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [--no-strip]"
            exit 1
            ;;
    esac
done

echo "[*] Building with GraalVM native-image..."
echo "[*] GRAAL_HOME: ${GRAAL_HOME}"
echo "[*] UPX_PATH: ${UPX_PATH}"
echo "[*] Strip binary: ${STRIP_BINARY}"

mvn clean package -DskipTests

rm -rf ./build/
mkdir -p build
cp -f ./target/music-sorter.jar ./build/
cd ./build/

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

if [[ "${STRIP_BINARY}" == "true" ]]; then
    echo "[*] Stripping binary..."
    echo "  Before: $(ls -lh ./music-sorter | awk '{print $5}')"
    strip -x ./music-sorter
    echo "  After:  $(ls -lh ./music-sorter | awk '{print $5}')"
else
    echo "[*] Skipping binary stripping (--no-strip specified)"
fi

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
