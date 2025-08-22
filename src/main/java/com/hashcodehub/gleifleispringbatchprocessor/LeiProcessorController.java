package com.hashcodehub.gleifleispringbatchprocessor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller to manually trigger the LEI data processing.
 */
@RestController
@RequestMapping("/api/v1/lei")
public class LeiProcessorController {

    private final JobLauncher jobLauncher;
    private final Job leiProcessorJob;

    public LeiProcessorController(JobLauncher jobLauncher, Job leiProcessorJob) {
        this.jobLauncher = jobLauncher;
        this.leiProcessorJob = leiProcessorJob;
    }

    /**
     * Endpoint to manually trigger the LEI data extraction process.
     * @return A response entity with a status message.
     */
    @PostMapping("/process")
    public ResponseEntity<String> processLeiRecords() {
        System.out.println("Endpoint triggered: Starting LEI processing job manually.");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        try {
            jobLauncher.run(leiProcessorJob, jobParameters);
            return ResponseEntity.ok("LEI records processing initiated.");
        } catch (JobExecutionAlreadyRunningException | JobRestartException |
                 JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            System.err.println("Error starting manual job: " + e.getMessage());
            return ResponseEntity.status(500).body("Error starting job: " + e.getMessage());
        }
    }
}
