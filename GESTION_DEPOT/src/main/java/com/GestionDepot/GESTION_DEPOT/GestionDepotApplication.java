package com.GestionDepot.GESTION_DEPOT;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class GestionDepotApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionDepotApplication.class, args);
	}

}	
