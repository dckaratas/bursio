package com.falcon.bursio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableRetry
public class BursioApplication {

	public static void main(String[] args) {
		SpringApplication.run(BursioApplication.class, args);
	}

}
