package org.fluenta.mappingservice.controller;

import org.fluenta.mappingservice.dto.MappingDtos;
import org.fluenta.mappingservice.dto.MappingDtos.MappingSuggestionRequest;
import org.fluenta.mappingservice.dto.MappingDtos.MappingSuggestionResponse;
import org.fluenta.mappingservice.dto.MappingDtos.MappingTemplate;
import org.fluenta.mappingservice.dto.MappingDtos.TransformationResponse;
import org.fluenta.mappingservice.service.MappingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping("/mapping")
public class MappingController {

    private final MappingService mappingService;

    public MappingController(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    /**
     * Fogad két adatstruktúrát (forrás és cél) és AI segítségével
     * javaslatot tesz a mapping-re, transzformációs és validációs szabályokra.
     */
    @PostMapping("/suggest")
    public ResponseEntity<MappingSuggestionResponse> suggestMapping(@RequestBody MappingSuggestionRequest request) {
        // A service réteg hívja az AI-t és visszaadja a javaslatokat
        MappingSuggestionResponse suggestion = mappingService.generateSuggestions(request.sourceData(), request.targetSchema());
        return ResponseEntity.ok(suggestion);
    }

    /**
     * Végrehajt egy adattranszformációt a megadott forrás adaton
     * egy mentett sablon (templateId) alapján.
     */
    @PostMapping("/transform")
    public ResponseEntity<TransformationResponse> transformData(@RequestBody MappingDtos.TransformationRequest request) {
        // A service réteg betölti a sablont és végrehajtja a transzformációt
        TransformationResponse response = mappingService.transform(request.sourceData(), request.templateId());
        return ResponseEntity.ok(response);
    }

    /**
     * Listázza az összes mentett (in-memory) mapping sablont.
     */
    @GetMapping("/templates")
    public ResponseEntity<List<MappingTemplate>> getTemplates() {
        return ResponseEntity.ok(mappingService.getAllTemplates());
    }

    /**
     * (Opcionális) Új sablon mentése a javaslatok alapján.
     */
    @PostMapping("/templates")
    public ResponseEntity<MappingTemplate> saveTemplate(@RequestBody MappingTemplate template) {
        MappingTemplate savedTemplate = mappingService.saveTemplate(template);
        return ResponseEntity.ok(savedTemplate);
    }
}
