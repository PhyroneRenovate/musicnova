FROM gradle:jdk14 as build
WORKDIR /build/
RUN git clone https://github.com/Phyrone/musicnova.git .
RUN gradle -no-daemon clean bootJar
FROM azul/zulu-openjdk:14
COPY --from=build /build/build/libs/*.jar /opt/bot/musicnova.jar
CMD ["java","-Xmx200m","-Xms200m","-jar","/opt/bot/musicnova.jar"]