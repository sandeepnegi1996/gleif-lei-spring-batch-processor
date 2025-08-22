package com.hashcodehub.gleifleispringbatchprocessor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Top-level POJO for the GLEIF API response, using Lombok for boilerplate.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GleifResponse {
    private Meta meta;
    private Data data;
    private Links links;

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        private GoldenCopy goldenCopy;
    }


    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GoldenCopy {
        private String publishDate;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private String type;
        private String id;
        private Attributes attributes;
        private Relationships relationships;
        private Links links;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attributes {
        private String lei;
        private Entity entity;
        private Registration registration;
        private List<String> bic;
        private List<String> mic;
        private String conformityFlag;
        private String ocid;
        private String qcc;
        private String spglobal;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entity {
        private LegalName legalName;
        private List<OtherNames> otherNames;
        private List<Object> transliteratedOtherNames;
        private Address legalAddress;
        private Address headquartersAddress;
        private RegisteredAt registeredAt;
        private String registeredAs;
        private String jurisdiction;
        private String category;
        private LegalForm legalForm;
        private AssociatedEntity associatedEntity;
        private String status;
        private Expiration expiration;
        private SuccessorEntity successorEntity;
        private List<Object> successorEntities;
        private String creationDate;
        private Object subCategory;
        private List<Object> otherAddresses;
        private List<Object> eventGroups;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LegalName {
        private String name;
        private String language;
    }


    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OtherNames {
        private String name;
        private String language;
        private String type;

    }



    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        private String language;
        private List<String> addressLines;
        private String addressNumber;
        private String addressNumberWithinBuilding;
        private String mailRouting;
        private String city;
        private String region;
        private String country;
        private String postalCode;


    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegisteredAt {
        private String id;
        private String other;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LegalForm {
        private String id;
        private String other;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssociatedEntity {
        private String lei;
        private String name;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Expiration {
        private String date;
        private String reason;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SuccessorEntity {
        private String lei;
        private String name;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Registration {
        private String initialRegistrationDate;
        private String lastUpdateDate;
        private String status;
        private String nextRenewalDate;
        private String managingLou;
        private String corroborationLevel;
        private RegisteredAt validatedAt;
        private String validatedAs;
        public List<OtherValidationAuthority> otherValidationAuthorities;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Relationships {
        @JsonProperty("managing-lou")
        private RelationshipLink managingLou;
        @JsonProperty("lei-issuer")
        private RelationshipLink leiIssuer;
        @JsonProperty("field-modifications")
        private RelationshipLink fieldModifications;
        @JsonProperty("direct-parent")
        private RelationshipLink directParent;
        @JsonProperty("ultimate-parent")
        private RelationshipLink ultimateParent;


    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OtherValidationAuthority{
        public ValidatedAt validatedAt;
        public String validatedAs;
    }


    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValidatedAt{
        public String id;
        public Object other;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RelationshipLink  {
        private Links links;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Links {
        private String related;
        @JsonProperty("reporting-exception")
        private String reportingException;
        private String self;
    }
}
