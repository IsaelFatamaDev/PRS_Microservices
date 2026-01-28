package pe.edu.vallegrande.vg_ms_claims_incidents.application.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vg_ms_claims_incidents.application.services.ComplaintResponseService;
import pe.edu.vallegrande.vg_ms_claims_incidents.domain.models.ComplaintResponse;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document.ComplaintResponseDocument;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.ComplaintResponseDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.mapper.ComplaintResponseMapper;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.repository.ComplaintResponseRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ComplaintResponseServiceImpl implements ComplaintResponseService {

    private final ComplaintResponseRepository complaintResponseRepository;
    private final ComplaintResponseMapper complaintResponseMapper;

    @Override
    public Flux<ComplaintResponseDTO> findAll() {
        return complaintResponseRepository.findAll()
                .map(complaintResponseMapper::toDomain)  // Document → Domain
                .map(this::convertToDTO);                // Domain → DTO
    }

    @Override
    public Mono<ComplaintResponseDTO> findById(String id) {
        return complaintResponseRepository.findById(id)
                .map(complaintResponseMapper::toDomain)  // Document → Domain
                .map(this::convertToDTO);                // Domain → DTO
    }

    @Override
    public Mono<ComplaintResponseDTO> save(ComplaintResponseDTO complaintResponseDTO) {
        ComplaintResponse complaintResponse = convertToEntity(complaintResponseDTO);
        ComplaintResponseDocument document = complaintResponseMapper.toDocument(complaintResponse);
        document.prePersist();  // Set audit fields
        
        return complaintResponseRepository.save(document)
                .map(complaintResponseMapper::toDomain)  // Document → Domain
                .map(this::convertToDTO);                // Domain → DTO
    }

    @Override
    public Mono<ComplaintResponseDTO> update(String id, ComplaintResponseDTO complaintResponseDTO) {
        return complaintResponseRepository.findById(id)
                .flatMap(existingDocument -> {
                    ComplaintResponse existingDomain = complaintResponseMapper.toDomain(existingDocument);
                    ComplaintResponse updatedDomain = convertToEntity(complaintResponseDTO);
                    
                    // Update fields
                    BeanUtils.copyProperties(updatedDomain, existingDomain, "id", "createdAt");
                    
                    // Convert back to document
                    ComplaintResponseDocument updatedDocument = complaintResponseMapper.toDocument(existingDomain);
                    updatedDocument.setId(existingDocument.getId());
                    updatedDocument.setCreatedAt(existingDocument.getCreatedAt());
                    updatedDocument.preUpdate();  // Set updatedAt
                    
                    return complaintResponseRepository.save(updatedDocument);
                })
                .map(complaintResponseMapper::toDomain)  // Document → Domain
                .map(this::convertToDTO);                // Domain → DTO
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return complaintResponseRepository.deleteById(id);
    }

    private ComplaintResponseDTO convertToDTO(ComplaintResponse complaintResponse) {
        ComplaintResponseDTO complaintResponseDTO = new ComplaintResponseDTO();
        BeanUtils.copyProperties(complaintResponse, complaintResponseDTO);
        return complaintResponseDTO;
    }

    private ComplaintResponse convertToEntity(ComplaintResponseDTO complaintResponseDTO) {
        ComplaintResponse complaintResponse = new ComplaintResponse();
        BeanUtils.copyProperties(complaintResponseDTO, complaintResponse);
        return complaintResponse;
    }
}