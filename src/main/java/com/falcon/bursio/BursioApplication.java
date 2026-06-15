package com.falcon.bursio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BursioApplication {

	public static void main(String[] args) {
		SpringApplication.run(BursioApplication.class, args);
	}

}
