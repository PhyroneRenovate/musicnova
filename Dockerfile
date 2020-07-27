FROM registry-lab.phyrone.de:443/phyrone/gradle-with-node-docker:jdk14 as build
WORKDIR /build/
RUN git clone https://github.com/Phyrone/musicnova.git .
RUN gradle -no-daemon clean first-install bootJar
FROM azul/zulu-openjdk:14
COPY --from=build /build/build/libs/*.jar /opt/bot/musicnova.jar
CMD ["java","-Xmx200m","-Xms200m","-jar","/opt/bot/musicnova.jar"]