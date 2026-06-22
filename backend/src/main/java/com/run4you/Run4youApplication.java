package com.run4you;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Run4youApplication {

    public static void main(String[] args) {
        SpringApplication.run(Run4youApplication.class, args);
    }

}
