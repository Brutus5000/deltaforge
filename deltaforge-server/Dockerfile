FROM openjdk:11-jdk-slim

LABEL maintainer="Brutus5000@gmx.net"

VOLUME /tmp
VOLUME /deltaforge
COPY build/libs/deltaforge-server-*.jar app.jar
ENTRYPOINT ["java", "-server", "-Djava.security.egd=file:/dev/./urandom", "-XX:+CompactStrings", "-XX:MaxMetaspaceSize=196m", "-XX:MinHeapFreeRatio=25", "-XX:MaxHeapFreeRatio=40", "-jar", "app.jar"]
