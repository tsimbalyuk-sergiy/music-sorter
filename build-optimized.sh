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

echo "[*] Building OPTIMIZED native binary with maximum performance..."
echo "[*] GRAAL_HOME: ${GRAAL_HOME}"
echo "[*] UPX_PATH: ${UPX_PATH}"
echo "[*] Strip binary: ${STRIP_BINARY}"

mvn clean package -DskipTests -q

rm -rf ./build/
mkdir -p build
cp -f ./target/music-sorter.jar ./build/
cd ./build/

echo "[*] Running optimized native-image compilation..."

# build static executable on Linux, regular on macOS
STATIC_FLAG=""
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "[*] Linux detected - enabling static executable"
    STATIC_FLAG="-H:+StaticExecutable"
else
    echo "[*] Non-Linux OS detected - building regular executable"
fi

${GRAAL_HOME}/bin/native-image \
  --no-fallback \
  --report-unsupported-elements-at-runtime \
  -H:+ReportExceptionStackTraces \
  -H:+UnlockExperimentalVMOptions \
  -H:+UseServiceLoaderFeature \
  -H:+RemoveSaturatedTypeFlows \
  --gc=serial \
  -march=native \
  -O3 \
  ${STATIC_FLAG} \
  -H:ReflectionConfigurationFiles=../META-INF/native-image/reflect-config.json \
  -H:ResourceConfigurationFiles=../META-INF/native-image/resource-config.json \
  -H:JNIConfigurationFiles=../META-INF/native-image/jni-config.json \
  -H:DynamicProxyConfigurationFiles=../META-INF/native-image/proxy-config.json \
  --initialize-at-run-time=org.jaudiotagger,org.tinylog.runtime.ModernJavaRuntime \
  -jar music-sorter.jar \
  music-sorter-optimized

echo "[*] Optimized native binary created successfully!"
ls -lh ./music-sorter-optimized

if [[ "${STRIP_BINARY}" == "true" ]]; then
    echo "[*] Stripping binary to reduce size..."
    echo "  Before: $(ls -lh ./music-sorter-optimized | awk '{print $5}')"
    strip -x ./music-sorter-optimized
    echo "  After:  $(ls -lh ./music-sorter-optimized | awk '{print $5}')"
else
    echo "[*] Skipping binary stripping (--no-strip specified)"
fi

if [[ -f "${UPX_PATH}" ]]; then
    echo "[*] Applying maximum UPX compression..."
    ${UPX_PATH} --ultra-brute ./music-sorter-optimized -o music-sorter-ultra
    echo "[*] Ultra compression complete!"
    ls -lh ./music-sorter-ultra
    
    echo ""
    echo "=== Size Comparison ==="
    echo "Original:  $(ls -lh ./music-sorter-optimized | awk '{print $5}')"
    echo "Ultra:     $(ls -lh ./music-sorter-ultra | awk '{print $5}')"
else
    echo "[WARN] UPX not found at ${UPX_PATH}, skipping compression"
    echo "[INFO] To enable compression, install UPX or set UPX_PATH environment variable"
fi

echo "[SUCCESS] Optimized native binary build complete!"