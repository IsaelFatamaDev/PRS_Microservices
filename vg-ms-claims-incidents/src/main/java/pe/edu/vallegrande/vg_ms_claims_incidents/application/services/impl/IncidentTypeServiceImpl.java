package pe.edu.vallegrande.vg_ms_claims_incidents.application.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vg_ms_claims_incidents.application.services.IncidentTypeService;
import pe.edu.vallegrande.vg_ms_claims_incidents.domain.models.IncidentType;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document.IncidentTypeDocument;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.IncidentTypeDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.mapper.IncidentTypeMapper;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.repository.IncidentTypeRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class IncidentTypeServiceImpl implements IncidentTypeService {

    private final IncidentTypeRepository incidentTypeRepository;
    private final IncidentTypeMapper incidentTypeMapper;

    @Override
    public Flux<IncidentTypeDTO> findAll() {
        return incidentTypeRepository.findAll()
                .map(incidentTypeMapper::toDomain)  // Document → Domain
                .map(this::convertToDTO);           // Domain → DTO
    }

    @Override
    public Mono<IncidentTypeDTO> findById(String id) {
        return incidentTypeRepository.findById(id)
                .map(incidentTypeMapper::toDomain)  // Document → Domain
                .map(this::convertToDTO);           // Domain → DTO
    }

    @Override
    public Mono<IncidentTypeDTO> save(IncidentTypeDTO incidentTypeDTO) {
        if (incidentTypeDTO.getStatus() == null || incidentTypeDTO.getStatus().isEmpty()) {
            incidentTypeDTO.setStatus("ACTIVE");
        }
        IncidentType incidentType = convertToEntity(incidentTypeDTO);
        IncidentTypeDocument document = incidentTypeMapper.toDocument(incidentType);
        document.prePersist();  // Set audit fields
        
        return incidentTypeRepository.save(document)
                .map(incidentTypeMapper::toDomain)  // Document → Domain
                .map(this::convertToDTO);           // Domain → DTO
    }

    @Override
    public Mono<IncidentTypeDTO> update(String id, IncidentTypeDTO incidentTypeDTO) {
        return incidentTypeRepository.findById(id)
                .flatMap(existingDocument -> {
                    IncidentType existingDomain = incidentTypeMapper.toDomain(existingDocument);
                    IncidentType updatedDomain = convertToEntity(incidentTypeDTO);
                    
                    // Update fields
                    BeanUtils.copyProperties(updatedDomain, existingDomain, "id", "createdAt");
                    
                    // Convert back to document
                    IncidentTypeDocument updatedDocument = incidentTypeMapper.toDocument(existingDomain);
                    updatedDocument.setId(existingDocument.getId());
                    updatedDocument.setCreatedAt(existingDocument.getCreatedAt());
                    updatedDocument.preUpdate();  // Set updatedAt
                    
                    return incidentTypeRepository.save(updatedDocument);
                })
                .map(incidentTypeMapper::toDomain)  // Document → Domain
                .map(this::convertToDTO);           // Domain → DTO
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return incidentTypeRepository.findById(id)
                .flatMap(incidentType -> {
                    incidentType.setStatus("INACTIVE");
                    incidentType.preUpdate();  // Update audit fields
                    return incidentTypeRepository.save(incidentType);
                })
                .then();
    }

    @Override
    public Mono<IncidentTypeDTO> restoreById(String id) {
        return incidentTypeRepository.findById(id)
                .flatMap(incidentType -> {
                    incidentType.setStatus("ACTIVE");
                    incidentType.preUpdate();  // Update audit fields
                    return incidentTypeRepository.save(incidentType);
                })
                .map(incidentTypeMapper::toDomain)  // Document → Domain
                .map(this::convertToDTO);           // Domain → DTO
    }

    private IncidentTypeDTO convertToDTO(IncidentType incidentType) {
        IncidentTypeDTO incidentTypeDTO = new IncidentTypeDTO();
        BeanUtils.copyProperties(incidentType, incidentTypeDTO);
        return incidentTypeDTO;
    }

    private IncidentType convertToEntity(IncidentTypeDTO incidentTypeDTO) {
        IncidentType incidentType = new IncidentType();
        BeanUtils.copyProperties(incidentTypeDTO, incidentType);
        return incidentType;
    }
}