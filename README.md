# Rest api monitoring application

Application makes raw HTTP requests from requests.config and tracks info of them 
in api-monitoring.log file.

Log file flushes after exceeding 10 kB.

Reequsts should be divided by line, starting with "#".

Additionally logs can be view through ELK stack by following scheme:

api-monitoring -> filebeat -> logstash -> elasticsearch <- kibana

## Usage

```
mvn clean package
mv -f target/api-monitoring-0.0.1-SNAPSHOT.jar docker/
cd docker
sh make.sh
sh api-monitoring_start.sh
docker-compose up

```

### Used technologies and libraries

- Java 15
- Spring Boot
- Docker
- Filebeat/Logstash/Elasticsearch  ( ghokoheat package: https://github.com/gnokoheat/elk-with-filebeat-by-docker-compose )
- RawHTTP library ( https://github.com/renatoathaydes/rawhttp )

### Options

Can be customized in application.prorties file as given here:
  - request.path - path to requests file
  - requsts.interval - time interval in minuets betweem reqests sessions
  - log.path - path to log file