FROM openjdk:17

WORKDIR /usr/springbootapp/

COPY target/AwsS3-0.0.1-SNAPSHOT.jar aws3.jar

LABEL maintainer="jaisai"

EXPOSE 8080

CMD ["echo", "hello from docker container"]

ENTRYPOINT ["java", "-jar", "aws3.jar"]
