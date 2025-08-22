package com.hashcodehub.gleifleispringbatchprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * An ItemProcessor to handle the data fetching and processing logic for each LEI ID.
 * This component ensures that only complete data is passed to the writer.
 */
@Component
public class LeiDataProcessor implements ItemProcessor<String, Map<String, Object>> {

    private final GleifApiService gleifApiService;

    public LeiDataProcessor(GleifApiService gleifApiService) {
        this.gleifApiService = gleifApiService;
    }

    /**
     * Processes a single LEI ID, fetches its data and relationships,
     * and returns a map of the data. Returns null if any part of the data
     * could not be fetched.
     *
     * @param leiId The LEI ID to process.
     * @return a Map containing the complete LEI data and relationships, or null if there was a failure.
     * @throws Exception
     */
    @Override
    public Map<String, Object> process(String leiId) throws Exception {
        System.out.println("Processing LEI ID: " + leiId);

        // Step 1: Fetch the main LEI record
        // Simple throttle: 60 requests/min = 1 request/sec
        // since we are using the guave rate limiter token bucket algorithm thread sleep is not required
//        Thread.sleep(1000);
        GleifResponse leiData = gleifApiService.fetchLeiRecord(leiId);
        if (leiData == null || leiData.getData() == null) {
            System.err.println("Skipping LEI " + leiId + " due to main record fetch failure.");
            return null; // Signals to Spring Batch to skip this item
        }

        // Step 2: Fetch all relationships and check for any failures
        System.out.println("Processing relationship links for LEI: " + leiId);
        Map<String, JsonNode> fetchedRelationships = new HashMap<>();
        boolean allRelationshipsFetched = true;

        if (leiData.getData().getRelationships() != null) {
            Map<String, GleifResponse.RelationshipLink> relationshipLinks = new HashMap<>();
            relationshipLinks.put("managing-lou", leiData.getData().getRelationships().getManagingLou());
            relationshipLinks.put("lei-issuer", leiData.getData().getRelationships().getLeiIssuer());
            relationshipLinks.put("direct-parent", leiData.getData().getRelationships().getDirectParent());
            relationshipLinks.put("ultimate-parent", leiData.getData().getRelationships().getUltimateParent());
            relationshipLinks.put("field-modifications", leiData.getData().getRelationships().getFieldModifications());

            for (Map.Entry<String, GleifResponse.RelationshipLink> entry : relationshipLinks.entrySet()) {
                String type = entry.getKey();
                GleifResponse.RelationshipLink link = entry.getValue();

                if (link != null && link.getLinks() != null && link.getLinks().getRelated() != null) {
                    String relatedUrl = link.getLinks().getRelated();
// since we are using the guave rate limiter token bucket algorithm thread sleep is not required
            //        Thread.sleep(1000);
                    JsonNode relationshipData = gleifApiService.fetchRelationshipData(relatedUrl);
                    if (relationshipData == null) {
                        // If any relationship call fails, the entire record is invalid.
                        allRelationshipsFetched = false;
                        break;
                    }
                    fetchedRelationships.put(type, relationshipData);
                }
            }
        }

        // Step 3: Return data only if all relationships were successfully fetched
        if (allRelationshipsFetched) {
            System.out.println("All data for LEI " + leiId + " fetched successfully.");
            Map<String, Object> result = new HashMap<>();
            result.put("leiData", leiData);
            result.put("relationships", fetchedRelationships);
            return result;
        } else {
            System.err.println("Skipping writing LEI " + leiId + " due to partial data failure.");
            return null;
        }
    }
}
