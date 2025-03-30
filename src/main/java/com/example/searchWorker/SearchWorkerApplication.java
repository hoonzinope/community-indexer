package com.example.searchWorker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SearchWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SearchWorkerApplication.class, args);
	}

}
