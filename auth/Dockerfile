FROM  java:8
VOLUME /tmp
ADD  target/auth-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT [ "sh", "-c", "java -Dspring.profiles.active=dev -jar app.jar"]
