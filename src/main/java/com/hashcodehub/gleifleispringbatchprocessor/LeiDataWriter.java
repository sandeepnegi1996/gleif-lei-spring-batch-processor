package com.hashcodehub.gleifleispringbatchprocessor;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * An ItemWriter to handle writing the processed data to CSV files.
 * This component writes to two different CSVs based on the data provided.
 */
@Component
public class LeiDataWriter implements ItemWriter<Map<String, Object>> {

    private final CsvWriterService csvWriterService;

    @Autowired
    public LeiDataWriter(CsvWriterService csvWriterService) {
        this.csvWriterService = csvWriterService;
    }

    /**
     * Writes the processed items (chunks) to the CSV files.
     *
     * @param chunk The chunk of items to write.
     * @throws Exception
     */
    @Override
    public void write(Chunk<? extends Map<String, Object>> chunk) throws Exception {
        for (Map<String, Object> item : chunk) {
            GleifResponse leiData = (GleifResponse) item.get("leiData");
            Map<String, JsonNode> relationships = (Map<String, JsonNode>) item.get("relationships");

            System.out.println("Writing complete LEI record and its relationships to CSVs.");

            // Correctly call the write methods, passing only the data.
            // The file paths are managed internally by CsvWriterService.
            csvWriterService.writeLeiRecordToCsv(leiData);
            csvWriterService.writeRelationshipToCsv(relationships);
        }
    }
}
