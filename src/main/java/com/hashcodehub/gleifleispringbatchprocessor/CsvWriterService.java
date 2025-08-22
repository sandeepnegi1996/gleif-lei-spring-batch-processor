package com.hashcodehub.gleifleispringbatchprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Service to handle all CSV writing logic, with file paths externalized to properties.
 */
@Service
public class CsvWriterService {

    @Value("${gleif.output.lei-records}")
    private String leiRecordsPath;
    @Value("${gleif.output.relationship-records}")
    private String relationshipRecordsPath;

    /**
     * Writes the main LEI record data to a CSV file.
     *
     * @param data The GleifResponse object to write.
     */
    public void writeLeiRecordToCsv(GleifResponse data) {
        System.out.println("Writing main LEI record to " + leiRecordsPath);
//        String[] headers = {
//                "id", "lei", "legalName", "registeredAs", "jurisdiction", "status",
//                "initialRegistrationDate", "lastUpdateDate", "nextRenewalDate",
//                "managingLou", "legalAddress", "headquartersAddress", "bic"
//        };

        String[] headers = {
                "id", "lei", "legalName", "registeredAs", "jurisdiction", "status",
                "initialRegistrationDate", "lastUpdateDate", "nextRenewalDate",
                "managingLou", "bic"
        };

        createParentDirectory(leiRecordsPath);
        boolean fileExists = new java.io.File(leiRecordsPath).exists();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(leiRecordsPath, true)); // append=true
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers))) {

            csvPrinter.printRecord(
                    data.getData().getId(),
                    data.getData().getAttributes().getLei(),
                    data.getData().getAttributes().getEntity().getLegalName().getName(),
                    data.getData().getAttributes().getEntity().getRegisteredAs(),
                    data.getData().getAttributes().getEntity().getJurisdiction(),
                    data.getData().getAttributes().getEntity().getStatus(),
                    data.getData().getAttributes().getRegistration().getInitialRegistrationDate(),
                    data.getData().getAttributes().getRegistration().getLastUpdateDate(),
                    data.getData().getAttributes().getRegistration().getNextRenewalDate(),
                    data.getData().getAttributes().getRegistration().getManagingLou(),
//                    formatAddress(data.getData().getAttributes().getEntity().getLegalAddress()),
//                    formatAddress(data.getData().getAttributes().getEntity().getHeadquartersAddress()),
                    data.getData().getAttributes().getBic() != null ? String.join("|", data.getData().getAttributes().getBic()) : ""
            );

            System.out.println("Main LEI record written successfully.");

        } catch (IOException e) {
            System.err.println("Error writing LEI record to CSV file.");
            e.printStackTrace();
        }
    }

    private String formatAddress(GleifResponse.Address address) {
        if (address == null) {
            return "";
        }
        return (address.getAddressLines() != null ? String.join(", ", address.getAddressLines()) : "") +
                ", " + (address.getCity() != null ? address.getCity() : "") +
                ", " + (address.getRegion() != null ? address.getRegion() : "") +
                ", " + (address.getPostalCode() != null ? address.getPostalCode() : "") +
                ", " + (address.getCountry() != null ? address.getCountry() : "");
    }

    /**
     * Writes relationship data to a separate CSV file.
     *
     * @param relationshipsData The Map containing the relationship data.
     */
    public void writeRelationshipToCsv(Map<String, JsonNode> relationshipsData) {
        System.out.println("Writing relationship data to " + relationshipRecordsPath);
        String[] headers = {"relationshipType", "id", "type", "attributes"};

        createParentDirectory(relationshipRecordsPath);
        boolean fileExists = new java.io.File(relationshipRecordsPath).exists();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(relationshipRecordsPath, true));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers))) {

            for (Map.Entry<String, JsonNode> entry : relationshipsData.entrySet()) {
                String relationshipType = entry.getKey();
                JsonNode data = entry.getValue();

                if (data.has("data") && data.get("data").isArray()) {
                    for (JsonNode record : data.get("data")) {
                        printRelationshipRecord(record, relationshipType, csvPrinter);
                    }
                } else if (data.has("data")) {
                    printRelationshipRecord(data.get("data"), relationshipType, csvPrinter);
                }
            }

            System.out.println("Relationship data written successfully.");

        } catch (IOException e) {
            System.err.println("Error writing relationship data to CSV file.");
            e.printStackTrace();
        }
    }

    private void printRelationshipRecord(JsonNode record, String relationshipType, CSVPrinter csvPrinter) throws IOException {
        String id = record.has("id") ? record.get("id").asText() : "";
        String type = record.has("type") ? record.get("type").asText() : "";
        String attributes = "";

        if (record.has("attributes")) {
            attributes = record.get("attributes").toString().replace("\n", "").replace("\r", "");
        }

        csvPrinter.printRecord(relationshipType, id, type, attributes);
    }

    private void createParentDirectory(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            System.err.println("Failed to create parent directories for file: " + filePath);
        }
    }
}