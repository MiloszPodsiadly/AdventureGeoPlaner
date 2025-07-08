package com.milosz.podsiadly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EnableJpaAuditing
@EntityScan("com.milosz.podsiadly.model")
@SpringBootApplication
public class AdventureGeoPlanerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdventureGeoPlanerApplication.class, args);
    }

}
