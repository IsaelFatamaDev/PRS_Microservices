package pe.edu.vallegrande.vg_ms_claims_incidents.application.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vg_ms_claims_incidents.application.services.ComplaintCategoryService;
import pe.edu.vallegrande.vg_ms_claims_incidents.domain.models.ComplaintCategory;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document.ComplaintCategoryDocument;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.ComplaintCategoryDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.mapper.ComplaintCategoryMapper;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.repository.ComplaintCategoryRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ComplaintCategoryServiceImpl implements ComplaintCategoryService {

    private final ComplaintCategoryRepository complaintCategoryRepository;
    private final ComplaintCategoryMapper complaintCategoryMapper;

    @Override
    public Flux<ComplaintCategoryDTO> findAll() {
        return complaintCategoryRepository.findAll()
                .map(complaintCategoryMapper::toDomain)  // Document → Domain
                .map(this::convertToDTO);                // Domain → DTO
    }

    @Override
    public Mono<ComplaintCategoryDTO> findById(String id) {
        return complaintCategoryRepository.findById(id)
                .map(complaintCategoryMapper::toDomain)  // Document → Domain
                .map(this::convertToDTO);                // Domain → DTO
    }

    @Override
    public Mono<ComplaintCategoryDTO> save(ComplaintCategoryDTO complaintCategoryDTO) {
        if (complaintCategoryDTO.getStatus() == null || complaintCategoryDTO.getStatus().isEmpty()) {
            complaintCategoryDTO.setStatus("ACTIVE");
        }
        
        // 1. DTO → Domain
        ComplaintCategory complaintCategory = convertToEntity(complaintCategoryDTO);
        
        // 2. Domain → Document
        ComplaintCategoryDocument document = complaintCategoryMapper.toDocument(complaintCategory);
        document.prePersist();  // Aplicar auditoría
        
        // 3. Guardar y convertir de vuelta
        return complaintCategoryRepository.save(document)
                .map(complaintCategoryMapper::toDomain)  // Document → Domain
                .map(this::convertToDTO);                // Domain → DTO
    }

    @Override
    public Mono<ComplaintCategoryDTO> update(String id, ComplaintCategoryDTO complaintCategoryDTO) {
        return complaintCategoryRepository.findById(id)
                .flatMap(existingDoc -> {
                    // 1. Document → Domain
                    ComplaintCategory existingDomain = complaintCategoryMapper.toDomain(existingDoc);
                    
                    // 2. Actualizar campos del dominio
                    if (complaintCategoryDTO.getCategoryCode() != null) {
                        existingDomain.setCategoryCode(complaintCategoryDTO.getCategoryCode());
                    }
                    if (complaintCategoryDTO.getCategoryName() != null) {
                        existingDomain.setCategoryName(complaintCategoryDTO.getCategoryName());
                    }
                    if (complaintCategoryDTO.getDescription() != null) {
                        existingDomain.setDescription(complaintCategoryDTO.getDescription());
                    }
                    if (complaintCategoryDTO.getPriorityLevel() != null) {
                        existingDomain.setPriorityLevel(complaintCategoryDTO.getPriorityLevel());
                    }
                    if (complaintCategoryDTO.getMaxResponseTime() != null) {
                        existingDomain.setMaxResponseTime(complaintCategoryDTO.getMaxResponseTime());
                    }
                    if (complaintCategoryDTO.getStatus() != null) {
                        existingDomain.setStatus(complaintCategoryDTO.getStatus());
                    }
                    
                    // 3. Domain → Document
                    ComplaintCategoryDocument updatedDoc = complaintCategoryMapper.toDocument(existingDomain);
                    updatedDoc.setId(existingDoc.getId());  // Mantener ID original
                    updatedDoc.setCreatedAt(existingDoc.getCreatedAt());  // Mantener fecha de creación
                    updatedDoc.preUpdate();  // Aplicar auditoría de actualización
                    
                    // 4. Guardar
                    return complaintCategoryRepository.save(updatedDoc);
                })
                .map(complaintCategoryMapper::toDomain)  // Document → Domain
                .map(this::convertToDTO);                // Domain → DTO
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return complaintCategoryRepository.findById(id)
                .flatMap(document -> {
                    document.setStatus("INACTIVE");
                    document.preUpdate();
                    return complaintCategoryRepository.save(document);
                })
                .then();
    }

    @Override
    public Mono<ComplaintCategoryDTO> restoreById(String id) {
        return complaintCategoryRepository.findById(id)
                .flatMap(document -> {
                    document.setStatus("ACTIVE");
                    document.preUpdate();
                    return complaintCategoryRepository.save(document);
                })
                .map(complaintCategoryMapper::toDomain)  // Document → Domain
                .map(this::convertToDTO);                // Domain → DTO
    }

    private ComplaintCategoryDTO convertToDTO(ComplaintCategory complaintCategory) {
        ComplaintCategoryDTO complaintCategoryDTO = new ComplaintCategoryDTO();
        BeanUtils.copyProperties(complaintCategory, complaintCategoryDTO);
        return complaintCategoryDTO;
    }

    private ComplaintCategory convertToEntity(ComplaintCategoryDTO complaintCategoryDTO) {
        ComplaintCategory complaintCategory = new ComplaintCategory();
        BeanUtils.copyProperties(complaintCategoryDTO, complaintCategory);
        return complaintCategory;
    }
}