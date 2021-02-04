FROM adoptopenjdk:15-jre-hotspot

ADD api-monitoring-0.0.1-SNAPSHOT.jar /opt/api-monitoring/
ADD application.properties /opt/api-monitoring/



WORKDIR /opt/api-monitoring/

CMD ["java", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=/opt/api-monitoring/target/dump.hprof", "-Xmx400m", "-jar", "api-monitoring-0.0.1-SNAPSHOT.jar"]
