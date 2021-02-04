package com.superhorsy.apimonitoring;

import com.superhorsy.apimonitoring.services.FileService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpOptions;

@SpringBootApplication
public class ApiMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiMonitoringApplication.class, args);
    }

    @Bean
    public FileService fileManager() {
        return new FileService();
    }

    @Bean
    public RawHttp rawHttp() {
        RawHttpOptions options = RawHttpOptions.newBuilder().allowIllegalStartLineCharacters().build();
        return new RawHttp(options);
    }
}
