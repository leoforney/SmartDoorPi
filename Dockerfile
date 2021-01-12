FROM arm32v7/adoptopenjdk:15-jre-hotspot
RUN ./gradlew bootJar
RUN cp build/libs/SmartDoorPi-1.0.jar .
RUN java -jar SmartDoorPi-1.0.jar