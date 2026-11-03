package com.inspaya.fixr;

import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
		exclude = PgVectorStoreAutoConfiguration.class
)
@EnableScheduling
@EnableAsync
public class FixrApplication {

	public static void main(String[] args) {
		SpringApplication.run(FixrApplication.class, args);
	}

}
