package com.hashcodehub.gleifleispringbatchprocessor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * A scheduler to automatically trigger the batch job at regular intervals.
 */
@Component
public class Scheduler {

    private final JobLauncher jobLauncher;
    private final Job leiProcessorJob;

    // Inject the cron expression from application.properties
    @Value("${gleif.job.cron}")
    private String jobCronExpression;

    public Scheduler(JobLauncher jobLauncher, Job leiProcessorJob) {
        this.jobLauncher = jobLauncher;
        this.leiProcessorJob = leiProcessorJob;
    }

    /**
     * Triggers the batch job to run automatically based on a cron expression from configuration.
     */
    @Scheduled(cron = "${gleif.job.cron}")
    public void runJobAutomatically() {
        System.out.println("Scheduler triggered: Starting LEI processing job.");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        try {
            jobLauncher.run(leiProcessorJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException |
                 JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            System.err.println("Error running scheduled job: " + e.getMessage());
        }
    }
}