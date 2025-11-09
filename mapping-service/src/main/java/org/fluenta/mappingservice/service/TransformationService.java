package org.fluenta.mappingservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fluenta.mappingservice.dto.MappingDtos;
import org.fluenta.mappingservice.dto.MappingDtos.FieldMapping;
import org.fluenta.mappingservice.dto.MappingDtos.MappingRules;
import org.fluenta.mappingservice.dto.MappingDtos.TransformationResponse;
import org.fluenta.mappingservice.dto.MappingDtos.TransformationRule;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Felelős a tényleges adattranszformáció végrehajtásáért a szabályok alapján.
 * Ez egy komplex logikai réteg (pl. JSON Path vagy reflexió alapú).
 * Ez egy NAGYON leegyszerűsített PÉLDA implementáció.
 */
@Service
public class TransformationService {

    private final ObjectMapper objectMapper;

    public TransformationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TransformationResponse applyTransformations(Object sourceData, MappingRules rules) {
        List<String> errors = new ArrayList<>();

        // A kimeneti JSON-t építjük
        ObjectNode targetRoot = objectMapper.createObjectNode();

        // A bejövő Object-et JsonNode-dá alakítjuk a könnyebb kezelésért
        JsonNode sourceRoot = objectMapper.valueToTree(sourceData);

        // 1. Egyszerű mező-hozzárendelések
        for (FieldMapping mapping : rules.fieldMappings()) {
            try {
                // Egyszerűsített "dot-notation" feldolgozó
                JsonNode sourceValue = getNodeByPath(sourceRoot, mapping.sourceField());
                if (sourceValue != null && !sourceValue.isNull()) {
                    setNodeByPath(targetRoot, mapping.targetField(), sourceValue);
                }
            } catch (Exception e) {
                errors.add("Failed to map field " + mapping.sourceField() + ": " + e.getMessage());
            }
        }

        // 2. Transzformációs szabályok
        for (TransformationRule rule : rules.transformationRules()) {
            try {
                Object value = applyRule(sourceRoot, rule);
                if (value != null) {
                    setNodeByPath(targetRoot, rule.targetField(), objectMapper.valueToTree(value));
                }
            } catch (Exception e) {
                errors.add("Failed to apply transform rule for " + rule.targetField() + ": " + e.getMessage());
            }
        }

        // 3. TODO: Validációs szabályok futtatása a 'targetRoot'-on

        return new TransformationResponse(targetRoot, errors);
    }

    /**
     * Leegyszerűsített szabály-alkalmazó.
     * Éles rendszerben ez egy sokkal robusztusabb "rule engine" lenne.
     */
    private Object applyRule(JsonNode sourceRoot, TransformationRule rule) {
        String sourceField = rule.parameters().get("sourceField");
        JsonNode sourceValueNode = getNodeByPath(sourceRoot, sourceField);
        if (sourceValueNode == null || sourceValueNode.isNull()) return null;
        String sourceValue = sourceValueNode.asText();

        switch (rule.ruleType()) {
            case "SPLIT_NAME":
                String[] parts = sourceValue.split(" ", 2);
                if ("FIRST".equals(rule.parameters().get("part")) && parts.length > 0) return parts[0];
                if ("LAST".equals(rule.parameters().get("part")) && parts.length > 1) return parts[1];
                if ("LAST".equals(rule.parameters().get("part")) && parts.length == 1) return parts[0]; // Ha nincs vezetéknév
                return null;

            case "FORMAT_DATE":
                try {
                    LocalDate date = LocalDate.parse(sourceValue); // Feltételezi az ISO (yyyy-MM-dd) bemenetet
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(rule.parameters().get("format"));
                    return date.format(formatter); // pl. "15/03/1985"
                } catch (Exception e) {
                    System.err.println("Date format error: " + e.getMessage());
                    return null;
                }

                // ... TÖBBI SZABÁLY (REGEX_EXTRACT, FORMAT_PHONE, stb.)
            default:
                return null;
        }
    }

    /**
     * Segédfüggvény: Érték lekérése "dot-notation" (pl. "Customer.PersonalData.FullName") alapján.
     * Leegyszerűsített, nem kezeli a tömböket.
     */
    private JsonNode getNodeByPath(JsonNode root, String path) {
        if (path == null) return null;
        String[] parts = path.split("\\.");
        JsonNode currentNode = root;
        for (String part : parts) {
            if (currentNode == null || !currentNode.has(part)) return null;
            currentNode = currentNode.get(part);
        }
        return currentNode;
    }

    /**
     * Segédfüggvény: Érték beállítása "dot-notation" (pl. "client.personal.firstName") alapján.
     * Leegyszerűsített, nem kezeli a tömböket.
     */
    private void setNodeByPath(ObjectNode root, String path, JsonNode value) {
        String[] parts = path.split("\\.");
        ObjectNode currentNode = root;
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!currentNode.has(part) || !currentNode.get(part).isObject()) {
                currentNode.set(part, objectMapper.createObjectNode());
            }
            currentNode = (ObjectNode) currentNode.get(part);
        }
        currentNode.set(parts[parts.length - 1], value);
    }
}