package org.fluenta.mappingservice.service;

import org.fluenta.mappingservice.Util.MockDataHelper;
import org.fluenta.mappingservice.dto.MappingDtos.*;
import org.fluenta.mappingservice.repository.MappingTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MappingServiceImplTest {

    @Mock
    private AiSuggestionService aiSuggestionService;

    @Mock
    private TransformationService transformationService;

    @Mock
    private MappingTemplateRepository templateRepository;

    @InjectMocks
    private MappingServiceImpl mappingService;

    private Object bankCorpData;
    private Object insureTechData;

    @BeforeEach
    void setUp() {
        bankCorpData = MockDataHelper.getBankCorpAsObject();
        insureTechData = MockDataHelper.getInsureTechAsObject();
    }

    @Test
    void testGenerateSuggestions() {
        // Arrange
        MappingSuggestionResponse mockSuggestion = new MappingSuggestionResponse(
                List.of(new FieldMapping("Test", "Test")), List.of(), List.of()
        );
        when(aiSuggestionService.getMappingSuggestions(bankCorpData, insureTechData)).thenReturn(mockSuggestion);

        // Act
        MappingSuggestionResponse result = mappingService.generateSuggestions(bankCorpData, insureTechData);

        // Assert
        assertNotNull(result);
        assertEquals("Test", result.fieldMappings().get(0).sourceField());
        verify(aiSuggestionService).getMappingSuggestions(bankCorpData, insureTechData);
    }

    @Test
    void testTransform() {
        // Arrange
        String templateId = UUID.randomUUID().toString();
        MappingRules rules = new MappingRules(
                List.of(new FieldMapping("Customer.CIF", "client.id")),
                List.of(),
                List.of()
        );
        MappingTemplate template = new MappingTemplate(templateId, "Test", rules);

        Map<String, Object> transformedData = Map.of("client", Map.of("id", "12345"));
        TransformationResponse mockResponse = new TransformationResponse(transformedData, List.of());

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(transformationService.applyTransformations(bankCorpData, rules)).thenReturn(mockResponse);

        // Act
        TransformationResponse result = mappingService.transform(bankCorpData, templateId);

        // Assert
        assertNotNull(result);
        assertEquals(transformedData, result.transformedData());
        verify(templateRepository).findById(templateId);
        verify(transformationService).applyTransformations(bankCorpData, rules);
    }

    @Test
    void testSaveTemplate() {
        // Arrange
        MappingTemplate template = new MappingTemplate(null, "New Template", null);
        when(templateRepository.save(any(MappingTemplate.class))).thenAnswer(invocation -> {
            MappingTemplate t = invocation.getArgument(0);
            // Biztosítjuk, hogy a mentett verziónak már van ID-ja
            return new MappingTemplate(t.id() != null ? t.id() : UUID.randomUUID().toString(), t.name(), t.rules());
        });
        // Act
        MappingTemplate savedTemplate = mappingService.saveTemplate(template);

        // Assert
        assertNotNull(savedTemplate);
        assertNotNull(savedTemplate.id());
        assertEquals("New Template", savedTemplate.name());
        verify(templateRepository).save(any(MappingTemplate.class));
    }
}
