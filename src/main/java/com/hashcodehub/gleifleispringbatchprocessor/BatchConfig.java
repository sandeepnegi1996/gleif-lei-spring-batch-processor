package com.hashcodehub.gleifleispringbatchprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

/**
 * Main Spring Batch configuration class.
 * Defines the job, step, reader, processor, and writer beans.
 */
@Configuration
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GleifApiService gleifApiService;

    // Inject file paths from application.properties for production-ready configuration
    @Value("${gleif.input.file-path}")
    private Resource inputResource;

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager, GleifApiService gleifApiService) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.gleifApiService = gleifApiService;
    }

    /**
     * Defines the ItemReader to read LEI IDs from the input CSV file.
     * The file path is now managed via application.properties.
     *
     * @return a FlatFileItemReader for reading LEI IDs.
     */
    @Bean
    public ItemReader<String> leiIdReader() {
        return new FlatFileItemReaderBuilder<String>()
                .name("leiIdReader")
                .resource(inputResource)
                .delimited().names("lei_id") // Assumes a single column with header "lei_id"
                .linesToSkip(1) // Skip the header row
                .fieldSetMapper(fieldSet -> fieldSet.readString("lei_id"))
                .build();
    }

//    /**
//     * Defines the ItemProcessor to fetch and process LEI data.
//     * This processor returns a Map containing both the main LEI record and its relationships,
//     * or null if any part of the data could not be fetched.
//     *
//     * @return an ItemProcessor for LEI data.
//     */
//    @Bean
//    public ItemProcessor<String, Map<String, Object>> leiDataProcessor() {
//        return leiId -> {
//            System.out.println("Processing LEI ID: " + leiId);
//            try {
//                // Fetch the main LEI record with retry logic
//                GleifResponse leiData = gleifApiService.fetchLeiRecord(leiId);
//                if (leiData == null || leiData.getData() == null) {
//                    System.err.println("Skipping LEI " + leiId + " due to main record fetch failure.");
//                    return null; // Signals to Spring Batch to skip this item
//                }
//
//                // Fetch all relationships with retry logic and check for any failures
//                Map<String, JsonNode> fetchedRelationships = gleifApiService.fetchAllRelationships(leiData.getData().getRelationships());
//                if (fetchedRelationships == null) {
//                    System.err.println("Skipping LEI " + leiId + " due to relationship data fetch failure.");
//                    return null; // Signals to Spring Batch to skip this item
//                }
//
//                // If everything is successful, return a map with all the data
//                return Map.of("leiData", leiData, "relationships", fetchedRelationships);
//
//            } catch (Exception e) {
//                System.err.println("An unexpected error occurred while processing LEI " + leiId + ": " + e.getMessage());
//                return null;
//            }
//        };
//    }

//    /**
//     * Defines the ItemWriter to write the complete LEI data to CSV files.
//     *
//     * @return an ItemWriter for LEI and relationship data.
//     */
//    @Bean
//    public ItemWriter<Map<String, Object>> leiDataWriter(CsvWriterService csvWriterService) {
//        return new LeiDataWriter(csvWriterService);
//    }

    @Bean
    public CsvWriterService csvWriterService() {
        return new CsvWriterService();
    }

    /**
     * Defines a single step in the batch job with a more robust error handling configuration.
     * It now takes the LeiDataProcessor as a dependency, resolving the bean conflict.
     *
     * @param leiDataProcessor The processor component.
     * @param leiDataWriter The writer component.
     * @return the Step bean.
     */
    @Bean
    public Step processLeiRecordsStep(LeiDataProcessor leiDataProcessor, ItemWriter<Map<String, Object>> leiDataWriter) {
        return new StepBuilder("processLeiRecordsStep", jobRepository)
                .<String, Map<String, Object>>chunk(2, transactionManager) // Process in chunks of 10
                .reader(leiIdReader())
                .processor(leiDataProcessor)
                .writer(leiDataWriter)
                .faultTolerant() // Enable fault tolerance
                .skipLimit(100) // Skip up to 100 failed items before the job itself fails
                .skip(Exception.class) // Skip any exception during processing or writing
                .build();
    }


    /**
     * Defines the overall batch job.
     *
     * @return the Job bean.
     */
    @Bean
    public Job leiProcessorJob(Step processLeiRecordsStep) {
        return new JobBuilder("leiProcessorJob", jobRepository)
                .start(processLeiRecordsStep)
                .build();
    }
}

