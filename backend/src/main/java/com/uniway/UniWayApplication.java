package com.uniway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class UniWayApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniWayApplication.class, args);
    }
}


