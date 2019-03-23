FROM openjdk:11.0.1-jdk-slim-stretch
EXPOSE 8080
ADD /target/condo-manager-0.0.1-SNAPSHOT.jar condo-manager.jar
ENTRYPOINT ["java", "-jar", "condo-manager.jar"]