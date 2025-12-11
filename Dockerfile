FROM amazoncorretto:17-alpine-jdk
WORKDIR /DDS
COPY DDS-all.jar .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "DDS-all.jar"]