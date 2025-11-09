package org.fluenta.mappingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fluenta.mappingservice.Util.MockDataHelper;
import org.fluenta.mappingservice.dto.MappingDtos;
import org.fluenta.mappingservice.dto.MappingDtos.*;
import org.fluenta.mappingservice.service.MappingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MappingController.class)
public class MappingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MappingService mappingService;

    @Test
    void testSuggestMapping() throws Exception {
        // Arrange
        MappingSuggestionRequest request = new MappingSuggestionRequest(
                MockDataHelper.getBankCorpAsObject(),
                MockDataHelper.getInsureTechAsObject()
        );

        MappingSuggestionResponse mockResponse = new MappingSuggestionResponse(
                List.of(new FieldMapping("Customer.CIF", "client.id")),
                List.of(new TransformationRule("client.personal.firstName", "SPLIT_NAME", Map.of("sourceField", "Customer.PersonalData.FullName", "part", "FIRST"))),
                List.of(new ValidationRule("client.contact.email", "IS_EMAIL", null))
        );

        when(mappingService.generateSuggestions(any(), any())).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/mapping/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fieldMappings[0].sourceField").value("Customer.CIF"))
                .andExpect(jsonPath("$.transformationRules[0].ruleType").value("SPLIT_NAME"))
                .andExpect(jsonPath("$.validationRules[0].ruleType").value("IS_EMAIL"));
    }

    @Test
    void testTransformData() throws Exception {
        // Arrange
        String templateId = UUID.randomUUID().toString();
        TransformationRequest request = new MappingDtos.TransformationRequest(
                MockDataHelper.getBankCorpAsObject(),
                templateId,
                null
        );

        // A "transformedData" egy Object, ami itt most egy egyszerű Map
        Map<String, Object> transformedData = Map.of("client", Map.of("id", "12345"));
        TransformationResponse mockResponse = new TransformationResponse(transformedData, List.of());

        when(mappingService.transform(any(), any(String.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/mapping/transform")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transformedData.client.id").value("12345"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void testGetTemplates() throws Exception {
        // Arrange
        MappingTemplate mockTemplate = new MappingTemplate(
                UUID.randomUUID().toString(),
                "Test Template",
                new MappingRules(List.of(), List.of(), List.of())
        );
        when(mappingService.getAllTemplates()).thenReturn(List.of(mockTemplate));

        // Act & Assert
        mockMvc.perform(get("/mapping/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Template"));
    }
}
