package com.collectorhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CollectorHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(CollectorHubApplication.class, args);
    }
}