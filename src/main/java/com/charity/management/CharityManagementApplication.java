package com.charity.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CharityManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(CharityManagementApplication.class, args);
    }
}
