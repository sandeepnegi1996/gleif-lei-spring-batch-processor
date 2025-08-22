package com.hashcodehub.gleifleispringbatchprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to handle all GLEIF API interactions, with built-in retry and recovery logic.
 */
@Service
@Slf4j
public class GleifApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final FailedLeiTrackerService failedLeiTrackerService;
    // Use a RateLimiter to ensure no more than 60 requests per minute (1 per second)
    private final RateLimiter rateLimiter = RateLimiter.create(1.0); // 1.0 permits per second


    // Inject the base URL from application.properties
    @Value("${gleif.api.base-url}")
    private String baseUrl;

    public GleifApiService(RestTemplate restTemplate, ObjectMapper objectMapper, FailedLeiTrackerService failedLeiTrackerService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.failedLeiTrackerService = failedLeiTrackerService;
    }

    /**
     * Fetches a single LEI record by its ID with retry functionality.
     * Now also handles `HttpClientErrorException` (e.g., 4xx errors) with a `no-retry` strategy.
     *
     * @param leiId The LEI ID to fetch.
     * @return A GleifResponse object representing the record.
     */
    @Retryable(
            value = {HttpServerErrorException.class, ResourceAccessException.class},
            notRecoverable = {HttpClientErrorException.class}, // Do not retry on 4xx client errors
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public GleifResponse fetchLeiRecord(String leiId) {
        // Wait for a token from the rate limiter before proceeding
        rateLimiter.acquire();
        log.info("limit acquired .. ");
        String url = baseUrl + "/lei-records/" + leiId;
        System.out.println("Attempting to fetch LEI record for ID: " + leiId);
        try {
            String response = restTemplate.getForObject(url, String.class);
            return objectMapper.readValue(response, GleifResponse.class);
        } catch (Exception e) {
            // Log the exception for better tracing
            System.err.println("Error fetching or deserializing LEI ID " + leiId + ": " + e.getMessage());
            throw new RuntimeException("Fetch or deserialization failed for LEI: " + leiId, e);
        }
    }

    /**
     * Recover method for fetchLeiRecord when all retries fail.
     * This method is called automatically by Spring Retry.
     *
     * @param e     The exception that caused the failure.
     * @param leiId The LEI ID that failed.
     * @return A null GleifResponse object to signal failure.
     */
    @Recover
    public GleifResponse recoverFetchLeiRecord(RuntimeException e, String leiId) {
        String reason = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        System.err.println("All retry attempts failed for LEI ID: " + leiId + ". Reason: " + reason);
        // Log the failure to the dedicated error file
        failedLeiTrackerService.logFailedLei(leiId, reason);
        return null;
    }

    /**
     * Fetches data from a relationships endpoint with retry functionality.
     *
     * @param url The URL of the relationship endpoint.
     * @return A JsonNode representing the data from the relationship.
     */
    @Retryable(
            value = {HttpServerErrorException.class, ResourceAccessException.class},
            notRecoverable = {HttpClientErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public JsonNode fetchRelationshipData(String url) {
        // Wait for a token from the rate limiter before proceeding
        rateLimiter.acquire();
        log.info("limit acquired .. ");

        System.out.println("Attempting to fetch relationship data from URL: " + url);
        try {
            String response = restTemplate.getForObject(url, String.class);
            return objectMapper.readTree(response);
        } catch (Exception e) {
            System.err.println("Error fetching or deserializing relationship data from URL: " + url + ". Reason: " + e.getMessage());
            throw new RuntimeException("Fetch or deserialization failed for URL: " + url, e);
        }
    }

    /**
     * Recover method for fetchRelationshipData when all retries fail.
     *
     * @param e   The exception that caused the failure.
     * @param url The URL that failed.
     * @return A null JsonNode to signal failure.
     */
    @Recover
    public JsonNode recoverFetchRelationshipData(RuntimeException e, String url) {
        String reason = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        System.err.println("All retry attempts failed for relationship URL: " + url + ". Reason: " + reason);
        failedLeiTrackerService.logFailedUrl(url, reason);
        return null;
    }

    /**
     * Fetches all relationship data for a given LEI.
     *
     * @param relationships The relationships object from the LEI record.
     * @return A map of relationship types to their JSON data, or null if any fetch fails.
     */
    public Map<String, JsonNode> fetchAllRelationships(GleifResponse.Relationships relationships) {
        Map<String, JsonNode> fetchedRelationships = new HashMap<>();
        boolean allFetched = true;

        if (relationships != null) {
            Map<String, GleifResponse.RelationshipLink> relationshipLinks = new HashMap<>();
            relationshipLinks.put("managing-lou", relationships.getManagingLou());
            relationshipLinks.put("lei-issuer", relationships.getLeiIssuer());
            relationshipLinks.put("direct-parent", relationships.getDirectParent());
            relationshipLinks.put("ultimate-parent", relationships.getUltimateParent());
            relationshipLinks.put("field-modifications", relationships.getFieldModifications());

            for (Map.Entry<String, GleifResponse.RelationshipLink> entry : relationshipLinks.entrySet()) {
                String type = entry.getKey();
                GleifResponse.RelationshipLink link = entry.getValue();

                if (link != null && link.getLinks() != null && link.getLinks().getRelated() != null) {
                    String relatedUrl = link.getLinks().getRelated();
                    JsonNode relationshipData = fetchRelationshipData(relatedUrl);
                    if (relationshipData == null) {
                        allFetched = false;
                        break;
                    }
                    fetchedRelationships.put(type, relationshipData);
                }
            }
        }
        return allFetched ? fetchedRelationships : null;
    }
}