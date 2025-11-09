package org.fluenta.mappingservice.service;
import org.fluenta.mappingservice.dto.MappingDtos.MappingSuggestionResponse;
import org.fluenta.mappingservice.dto.MappingDtos.MappingTemplate;
import org.fluenta.mappingservice.dto.MappingDtos.TransformationResponse;
import org.fluenta.mappingservice.repository.MappingTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MappingServiceImpl implements MappingService {

    private final AiSuggestionService aiSuggestionService;
    private final TransformationService transformationService;
    private final MappingTemplateRepository templateRepository;

    public MappingServiceImpl(AiSuggestionService aiSuggestionService,
                              TransformationService transformationService,
                              MappingTemplateRepository templateRepository) {
        this.aiSuggestionService = aiSuggestionService;
        this.transformationService = transformationService;
        this.templateRepository = templateRepository;
    }

    @Override
    public MappingSuggestionResponse generateSuggestions(Object sourceData, Object targetSchema) {
        // Ez a metódus delegálja a tényleges AI hívást
        System.out.println("Generating suggestions...");
        return aiSuggestionService.getMappingSuggestions(sourceData, targetSchema);
    }

    @Override
    public TransformationResponse transform(Object sourceData, String templateId) {
        // 1. Sablon lekérése
        MappingTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        System.out.println("Transforming data with template: " + templateId);

        // 2. Transzformáció végrehajtása
        // A TransformationService-nek kell implementálnia a logikát,
        // ami a sourceData-t a template.rules() alapján átalakítja.
        return transformationService.applyTransformations(sourceData, template.rules());
    }

    @Override
    public MappingTemplate saveTemplate(MappingTemplate template) {
        // Generáljunk egy ID-t, ha nincs
        String id = template.id() == null ? UUID.randomUUID().toString() : template.id();
        MappingTemplate templateToSave = new MappingTemplate(id, template.name(), template.rules());

        System.out.println("Saving template: " + templateToSave.name());
        return templateRepository.save(templateToSave);
    }

    @Override
    public List<MappingTemplate> getAllTemplates() {
        System.out.println("Fetching all templates...");
        return templateRepository.findAll();
    }
}