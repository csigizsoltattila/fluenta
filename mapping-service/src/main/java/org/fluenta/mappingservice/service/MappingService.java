package org.fluenta.mappingservice.service;

import org.fluenta.mappingservice.dto.MappingDtos.MappingSuggestionResponse;
import org.fluenta.mappingservice.dto.MappingDtos.MappingTemplate;
import org.fluenta.mappingservice.dto.MappingDtos.TransformationResponse;

import java.util.List;

public interface MappingService {

    /**
     * AI-alapú javaslatokat generál két adatséma/példa alapján.
     *
     * @param sourceData   A forrás adat (pl. BankCorp).
     * @param targetSchema A cél séma (pl. InsureTech).
     * @return Egy javaslatcsomag (mapping, transform, validation rules).
     */
    MappingSuggestionResponse generateSuggestions(Object sourceData, Object targetSchema);

    /**
     * Végrehajtja az adattranszformációt egy mentett sablon alapján.
     *
     * @param sourceData A forrás adat.
     * @param templateId A használni kívánt sablon azonosítója.
     * @return A transzformált adat és az esetleges hibák.
     */
    TransformationResponse transform(Object sourceData, String templateId);

    /**
     * Elment egy új mapping sablont.
     *
     * @param template A menteni kívánt sablon.
     * @return A mentett sablon (lehet, hogy ID-val kiegészítve).
     */
    MappingTemplate saveTemplate(MappingTemplate template);

    /**
     * Visszaadja az összes mentett sablont.
     *
     * @return A sablonok listája.
     */
    List<MappingTemplate> getAllTemplates();
}