package org.fluenta.mappingservice.repository;

import org.fluenta.mappingservice.dto.MappingDtos;
import org.fluenta.mappingservice.dto.MappingDtos.MappingTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryMappingTemplateRepository implements MappingTemplateRepository {

    private final Map<String, MappingTemplate> templateStore = new ConcurrentHashMap<>();

    @Override
    public MappingTemplate save(MappingTemplate template) {
        if (template.id() == null) {
            throw new IllegalArgumentException("Template ID cannot be null");
        }
        templateStore.put(template.id(), template);
        return template;
    }

    @Override
    public Optional<MappingTemplate> findById(String id) {
        return Optional.ofNullable(templateStore.get(id));
    }

    @Override
    public List<MappingTemplate> findAll() {
        return List.copyOf(templateStore.values());
    }
}
