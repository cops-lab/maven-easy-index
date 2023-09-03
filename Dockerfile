FROM openjdk:11.0-jre-slim

WORKDIR /usr/local/runme
COPY reader/target/lib lib
# having multiple .jars (e.g., *-sources.jar) breaks this cmd
COPY reader/target/*.jar server.jar

RUN mkdir -p /cache

ENTRYPOINT exec java $JAVA_OPTS -jar server.jar $0 $@
CMD ["--baseFolder", "/cache"]
