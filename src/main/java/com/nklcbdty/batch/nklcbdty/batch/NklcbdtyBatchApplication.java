package com.nklcbdty.batch.nklcbdty.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NklcbdtyBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(NklcbdtyBatchApplication.class, args);
	}

}
