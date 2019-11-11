#!/usr/bin/env bash
set -e
GRAAL=/home/tsv/.sdkman/candidates/java/19.2.1-grl
#UPX=~/soft/upx-3.95-amd64_linux
UPX=/usr/bin/upx

mvn clean package
CP=$(mvn -q exec:exec -Dexec.executable=echo -Dexec.args="%classpath")

[[ -d build ]] || mkdir -p build
cp -f ./target/music-sorter.jar ./build/
cd ./build/

echo
echo Graal
echo
#-H:DynamicProxyConfigurationFiles="dynamic-proxies.json" \
                           #              --class-path music-sorter-*.jar \
#${GRAAL}/bin/native-image -cp "$CP" dev.tsvinc.music.sort.App -H:+ReportExceptionStackTraces \
#-H:Name=server \
#             -H:Class=io.micronaut.function.aws.runtime.MicronautLambdaRuntime \
#--enable-https \
#--rerun-class-initialization-at-runtime='sun.security.jca.JCAUtil$CachedSecureRandomHolder,javax.net.ssl.SSLContext' \
#             --delay-class-initialization-to-runtime=io.netty.handler.codec.http.HttpObjectEncoder,io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder,io.netty.handler.ssl.util.ThreadLocalInsecureRandom,com.sun.jndi.dns.DnsClient,io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler,com.amazonaws.serverless.proxy.internal.LambdaContainerHandler,io.netty.handler.ssl.JdkNpnApplicationProtocolNegotiator,io.netty.handler.ssl.ReferenceCountedOpenSslEngine
#--language:llvm \
${GRAAL}/bin/native-image -H:+ReportExceptionStackTraces \
              --language:llvm \
              --no-server \
              --allow-incomplete-classpath \
              --class-path music-sorter.jar \
              --report-unsupported-elements-at-runtime \
              --initialize-at-run-time=org.jaudiotagger.audio.asf.io.ChunkContainerReader,org.jaudiotagger.audio.asf.io.ContentDescriptionReader \
              -H:ReflectionConfigurationFiles=../reflect-config.json \
              -H:Log=registerResource \
              -H:+ReportUnsupportedElementsAtRuntime \
              -H:+ReportExceptionStackTraces \
              -H:-AllowVMInspection \
              -jar music-sorter.jar

ls -lh ./music-sorter

#echo
#echo Upx
#echo
#
#rm -f ./serv
#$UPX/upx ./com.cmlteam.serv.serv -oserv
#ls -lh ./serv
