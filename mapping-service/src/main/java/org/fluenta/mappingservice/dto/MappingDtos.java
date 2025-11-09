package org.fluenta.mappingservice.dto;

import java.util.List;
import java.util.Map;

public class MappingDtos {

    // --- AI javaslat kérés és válasz ---

    /**
     * AI javaslatkérő request body-ja.
     * Az Object típus lehetővé teszi tetszőleges JSON struktúra fogadását.
     * @param sourceData A forrás adat (pl. BankCorp JSON)
     * @param targetSchema A cél séma vagy példa adat (pl. InsureTech JSON)
     */
    public record MappingSuggestionRequest(Object sourceData, Object targetSchema) {}

    /**
     * Az AI által generált javaslatokat tartalmazó válasz.
     * Ezt a struktúrát kell az AI-nak visszaadnia JSON formátumban.
     * @param fieldMappings Javasolt mező-hozzárendelések.
     * @param transformationRules Javasolt átalakítási szabályok (pl. dátumformázás).
     * @param validationRules Javasolt validációs szabályok a cél mezőkre.
     */
    public record MappingSuggestionResponse(
            List<FieldMapping> fieldMappings,
            List<TransformationRule> transformationRules,
            List<ValidationRule> validationRules
    ) {}


    // --- Transzformációs kérés és válasz ---

    /**
     * Adattranszformációs kérés.
     * @param sourceData A forrás adat, amit át kell alakítani.
     * @param templateId Egy korábban mentett sablon azonosítója.
     * @param rules (Opcionális) Ad-hoc szabályok, ha nincs templateId.
     */
    public record TransformationRequest(
            Object sourceData,
            String templateId,
            MappingRules rules
    ) {}

    /**
     * Adattranszformációs válasz.
     * @param transformedData Az átalakított adat (cél formátumban).
     * @param errors A transzformáció során felmerült hibák listája.
     */
    public record TransformationResponse(Object transformedData, List<String> errors) {}


    // --- Sablon és szabály modellek ---

    /**
     * Egy teljes mapping sablont reprezentál.
     * @param id A sablon egyedi azonosítója.
     * @param name A sablon neve.
     * @param rules A sablonhoz tartozó szabálykészlet.
     */
    public record MappingTemplate(String id, String name, MappingRules rules) {}

    /**
     * Egy sablon teljes szabálykészlete.
     */
    public record MappingRules(
            List<FieldMapping> fieldMappings,
            List<TransformationRule> transformationRules,
            List<ValidationRule> validationRules
    ) {}

    /**
     * Egy egyszerű mező-hozzárendelés.
     * @param sourceField Forrás mező (pl. "Customer.PersonalData.FullName")
     * @param targetField Cél mező (pl. "client.personal.firstName")
     */
    public record FieldMapping(String sourceField, String targetField) {}

    /**
     * Egy átalakítási szabály.
     * @param targetField A cél mező, amire a szabály vonatkozik.
     * @param ruleType A szabály típusa (pl. "SPLIT_NAME", "FORMAT_DATE", "CONCAT").
     * @param parameters A szabály paraméterei (pl. "sourceField", "format", "part").
     */
    public record TransformationRule(String targetField, String ruleType, Map<String, String> parameters) {}

    /**
     * Egy validációs szabály.
     * @param targetField A cél mező, amire a szabály vonatkozik.
     * @param ruleType A validáció típusa (pl. "IS_EMAIL", "NOT_NULL", "REGEX").
     * @param parameters (Opcionális) pl. a REGEX minta.
     */
    public record ValidationRule(String targetField, String ruleType, Map<String, String> parameters) {}

    // --- AI API DTO-k ---
    // Ezek a Gemini API-val való kommunikációhoz kellenek

    public record GeminiRequest(List<Content> contents, GenerationConfig generationConfig, SystemInstruction systemInstruction) {}
    public record Content(List<Part> parts) {}
    public record Part(String text) {}
    public record SystemInstruction(List<Part> parts) {}
    public record GenerationConfig(String responseMimeType) {}

    public record GeminiResponse(List<Candidate> candidates) {}
    public record Candidate(Content content) {}
}