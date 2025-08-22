package com.hashcodehub.gleifleispringbatchprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
//@EnableBatchProcessing // Activates Spring Batch features
@EnableScheduling     // Enables the scheduler to run jobs periodically
@EnableRetry          // Enables Spring's retry mechanism
public class GleifLeiApplication {

    public static void main(String[] args) {
        SpringApplication.run(GleifLeiApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
