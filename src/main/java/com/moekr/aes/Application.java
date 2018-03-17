package com.moekr.aes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.moekr.aes.data.dao")
public class Application extends SpringApplication {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
