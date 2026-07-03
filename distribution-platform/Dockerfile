FROM docker.m.daocloud.io/library/maven:3.9.9-amazoncorretto-21

ARG MODULE
ENV TZ=Asia/Shanghai
ENV JAVA_OPTS=""

WORKDIR /app

COPY distribution-platform/docker-build-jars/${MODULE}.jar /app/app.jar

EXPOSE 8001 8002 8003 8005 8006 8007 9000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
