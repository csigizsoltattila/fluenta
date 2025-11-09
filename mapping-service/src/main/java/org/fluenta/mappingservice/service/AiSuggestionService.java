package org.fluenta.mappingservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fluenta.mappingservice.dto.MappingDtos.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class AiSuggestionService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String geminiApiKey;
    private final String geminiApiUrl;

    public AiSuggestionService(WebClient.Builder webClientBuilder,
                               ObjectMapper objectMapper,
                               @Value("${gemini.api.key}") String geminiApiKey,
                               @Value("${gemini.api.url}") String geminiApiUrl) {
        this.webClient = webClientBuilder.baseUrl(geminiApiUrl).build();
        this.objectMapper = objectMapper;
        this.geminiApiKey = geminiApiKey;
        this.geminiApiUrl = geminiApiUrl;
    }

    /**
     * Meghívja a Gemini API-t, hogy javaslatokat generáljon.
     * Ez a metódus jelenleg MOCK adatokat ad vissza,
     * de a valós API hívás logikája is itt van (kommentelve).
     */
    public MappingSuggestionResponse getMappingSuggestions(Object sourceData, Object targetSchema) {
        // TODO: A valós implementációban a tényleges API hívás kell.
        // A teszteléshez most egy mock választ adunk vissza.
        // A valós hívás előtt érdemes a "callGeminiApi" metódust
        // és a prompt építést implementálni.

        System.out.println("AI Service: Using mocked suggestions for development.");
        return getMockedSuggestions();

        /*
        // --- VALÓS API HÍVÁS (IMPLEMENTÁCIÓS PÉLDA) ---

        // 1. Prompt felépítése
        String prompt = buildPrompt(sourceData, targetSchema);
        String systemPrompt = buildSystemPrompt();

        // 2. Gemini kérés összeállítása
        SystemInstruction systemInstruction = new SystemInstruction(List.of(new Part(systemPrompt)));
        Content content = new Content(List.of(new Part(prompt)));
        GenerationConfig config = new GenerationConfig("application/json");
        GeminiRequest request = new GeminiRequest(List.of(content), config, systemInstruction);

        // 3. API hívás (aszinkron)
        Mono<MappingSuggestionResponse> responseMono = callGeminiApi(request);

        // 4. Válasz blokkolása (vagy aszinkron kezelése a service rétegben)
        // Figyelem: a .block() éles környezetben megfontolandó!
        return responseMono.block();
        */
    }

    // --- PÉLDA A VALÓS API HÍVÁSRA (WebClient) ---

    private Mono<MappingSuggestionResponse> callGeminiApi(GeminiRequest request) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", geminiApiKey).build())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .flatMap(this::parseGeminiResponse);
    }

    private Mono<MappingSuggestionResponse> parseGeminiResponse(GeminiResponse geminiResponse) {
        try {
            // Az AI válasza a 'text' mezőben lesz, ami egy JSON string
            String jsonText = geminiResponse.candidates().get(0).content().parts().get(0).text();

            // Eltávolítjuk a "```json" és "```" jeleket, ha az AI belerakta
            jsonText = jsonText.replace("```json", "").replace("```", "").trim();

            // A kapott JSON stringet deszerializáljuk a DTO-nkra
            MappingSuggestionResponse response = objectMapper.readValue(jsonText, MappingSuggestionResponse.class);
            return Mono.just(response);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Failed to parse AI response", e));
        }
    }

    // --- PROMPT ÉPÍTÉS ---

    private String buildSystemPrompt() {
        return "You are an expert data mapping assistant. Your task is to analyze two JSON structures, a source and a target. " +
                "You must generate a mapping between them in a structured JSON format. " +
                "Your response MUST be a valid JSON object adhering to the requested schema, and nothing else. " +
                "The JSON object must contain three keys: 'fieldMappings', 'transformationRules', and 'validationRules'.\n" +
                "Transformation rules should handle things like date formatting (FORMAT_DATE), name splitting (SPLIT_NAME), or concatenation (CONCAT). " +
                "Validation rules should suggest checks like IS_EMAIL, NOT_NULL, or REGEX.";
    }

    private String buildPrompt(Object sourceData, Object targetSchema) {
        try {
            String sourceJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sourceData);
            String targetJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(targetSchema);

            return "Here is the source data sample (BankCorp):\n" +
                    "```json\n" + sourceJson + "\n```\n\n" +
                    "Here is the target data sample (InsureTech):\n" +
                    "```json\n" + targetJson + "\n```\n\n" +
                    "Please provide the `fieldMappings`, `transformationRules`, and `validationRules` in the specified JSON format. " +
                    "Pay attention to a-sync fields like 'FullName' -> 'firstName'/'lastName' and 'BirthDate' -> 'dateOfBirth' format.";

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON for prompt", e);
        }
    }

    // --- MOCK VÁLASZ A TESZTELÉSHEZ ---

    private MappingSuggestionResponse getMockedSuggestions() {
        List<FieldMapping> mappings = List.of(
                new FieldMapping("Customer.CIF", "client.id"),
                new FieldMapping("Customer.PersonalData.TaxID", "client.personal.nationalId"),
                new FieldMapping("Customer.ContactInfo.EmailAddr", "client.contact.email"),
                new FieldMapping("Customer.Address.PostalCode", "client.location.zip"),
                new FieldMapping("Customer.Address.City", "client.location.city")
        );

        List<TransformationRule> transformations = List.of(
                new TransformationRule("client.personal.firstName", "SPLIT_NAME", Map.of("sourceField", "Customer.PersonalData.FullName", "part", "FIRST")),
                new TransformationRule("client.personal.lastName", "SPLIT_NAME", Map.of("sourceField", "Customer.PersonalData.FullName", "part", "LAST")),
                new TransformationRule("client.personal.dateOfBirth", "FORMAT_DATE", Map.of("sourceField", "Customer.PersonalData.BirthDate", "format", "dd/MM/yyyy")),
                new TransformationRule("client.contact.mobile", "FORMAT_PHONE", Map.of("sourceField", "Customer.ContactInfo.PrimaryPhone", "format", "national")),
                new TransformationRule("client.location.street", "REGEX_EXTRACT", Map.of("sourceField", "Customer.Address.StreetAddress", "pattern", "^(.*?)\\s*\\d+$")),
                new TransformationRule("client.location.number", "REGEX_EXTRACT", Map.of("sourceField", "Customer.Address.StreetAddress", "pattern", "(\\d+)$"))
        );

        List<ValidationRule> validations = List.of(
                new ValidationRule("client.contact.email", "IS_EMAIL", null),
                new ValidationRule("client.personal.nationalId", "NOT_NULL", null),
                new ValidationRule("client.location.zip", "REGEX", Map.of("pattern", "^\\d{4}$"))
        );

        return new MappingSuggestionResponse(mappings, transformations, validations);
    }
}
