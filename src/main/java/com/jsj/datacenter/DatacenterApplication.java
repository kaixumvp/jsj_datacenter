package com.jsj.datacenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DatacenterApplication {

    public static void main(String[] args) {
        System.setProperty("spring.security.enabled", "false");
        SpringApplication.run(DatacenterApplication.class, args);
    }

}