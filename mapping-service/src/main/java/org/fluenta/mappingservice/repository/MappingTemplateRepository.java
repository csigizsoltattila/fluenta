package org.fluenta.mappingservice.repository;

import org.fluenta.mappingservice.dto.MappingDtos;
import org.fluenta.mappingservice.dto.MappingDtos.MappingTemplate;

import java.util.List;
import java.util.Optional;

public interface MappingTemplateRepository {
    MappingTemplate save(MappingTemplate template);
    Optional<MappingTemplate> findById(String id);
    List<MappingTemplate> findAll();
}
