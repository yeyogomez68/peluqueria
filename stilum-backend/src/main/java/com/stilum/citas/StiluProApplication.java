package com.stilum.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StiluProApplication {

    public static void main(String[] args) {
        SpringApplication.run(StiluProApplication.class, args);
    }
}
