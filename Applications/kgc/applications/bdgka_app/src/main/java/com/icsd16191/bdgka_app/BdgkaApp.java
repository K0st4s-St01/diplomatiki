package com.icsd16191.bdgka_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class BdgkaApp {
	public static void main(String[] args) {
		SpringApplication.run(BdgkaApp.class, args);
	}

}
