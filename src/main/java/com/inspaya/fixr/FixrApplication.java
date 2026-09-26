package com.inspaya.fixr;

import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
		exclude = PgVectorStoreAutoConfiguration.class
)
public class FixrApplication {

	public static void main(String[] args) {
		SpringApplication.run(FixrApplication.class, args);
	}

}
