package pe.edu.vallegrande.vgmsorganizations.application.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.application.services.StreetService;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.StreetRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.StreetResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.CustomException;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.mapper.StreetMapper;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.repository.StreetRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
public class StreetServiceImpl implements StreetService {

    private final StreetRepository streetRepository;
    private final StreetMapper streetMapper;

    // Constructor explícito (PRS1 Standard - No usar @RequiredArgsConstructor)
    public StreetServiceImpl(StreetRepository streetRepository, StreetMapper streetMapper) {
        this.streetRepository = streetRepository;
        this.streetMapper = streetMapper;
    }

    @Override
    public Mono<StreetResponse> create(StreetRequest request) {
        return streetRepository.findFirstByOrderByStreetCodeDesc()
                .map(street -> generateStreetCode(street.getStreetCode()))
                .defaultIfEmpty("CAL001")
                .flatMap(streetCode -> {
                    Street street = Street.builder()
                            .zoneId(request.getZoneId())
                            .streetCode(streetCode)
                            .streetName(request.getStreetName())
                            .streetType(request.getStreetType())
                            .status("ACTIVE")
                            .createdAt(Instant.now())
                            .build();

                    return streetRepository.save(streetMapper.toEntity(street))
                            .map(streetMapper::toDomain)
                            .map(this::mapToResponse);
                });
    }

    @Override
    public Flux<StreetResponse> findAll() {
        return streetRepository.findAll()
                .map(streetMapper::toDomain)
                .map(this::mapToResponse);
    }

    @Override
    public Mono<StreetResponse> findById(String id) {
        return streetRepository.findById(id)
                .map(streetMapper::toDomain)
                .map(this::mapToResponse)
                .switchIfEmpty(Mono.error(new CustomException(404, "Calle no encontrada", "La calle con ID " + id + " no existe")));
    }

    @Override
    public Mono<StreetResponse> update(String id, StreetRequest request) {
        return streetRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(404, "Calle no encontrada", "La calle con ID " + id + " no existe")))
                .map(streetMapper::toDomain)
                .flatMap(existingStreet -> {
                    // Mantener el código existente, solo actualizar los otros campos
                    existingStreet.setZoneId(request.getZoneId());
                    existingStreet.setStreetName(request.getStreetName());
                    existingStreet.setStreetType(request.getStreetType());
                    // NO actualizar el streetCode, mantener el original
                    return streetRepository.save(streetMapper.toEntity(existingStreet));
                })
                .map(streetMapper::toDomain)
                .map(this::mapToResponse);
    }

    @Override
    public Mono<Void> delete(String id) {
        return streetRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(404, "Calle no encontrada", "La calle con ID " + id + " no existe")))
                .map(streetMapper::toDomain)
                .flatMap(street -> {
                    street.setStatus("INACTIVE");
                    return streetRepository.save(streetMapper.toEntity(street));
                })
                .then();
    }

    @Override
    public Mono<Void> restore(String id) {
        return streetRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(404, "Calle no encontrada", "La calle con ID " + id + " no existe")))
                .map(streetMapper::toDomain)
                .flatMap(street -> {
                    street.setStatus("ACTIVE");
                    return streetRepository.save(streetMapper.toEntity(street));
                })
                .then();
    }

    @Override
    public Flux<StreetResponse> findByZoneId(String zoneId) {
        return streetRepository.findAllByZoneId(zoneId)
                .map(streetMapper::toDomain)
                .map(this::mapToResponse);
    }

    private String generateStreetCode(String lastCode) {
        if (lastCode == null || lastCode.isEmpty()) {
            return "CAL001";
        }
        
        // Extraer el número del código anterior
        String numberPart = lastCode.substring(3); // Quitar "CAL"
        int nextNumber = Integer.parseInt(numberPart) + 1;
        
        // Formatear con ceros a la izquierda
        return String.format("CAL%03d", nextNumber);
    }

    private StreetResponse mapToResponse(Street street) {
        return StreetResponse.builder()
                .streetId(street.getStreetId())
                .zoneId(street.getZoneId())
                .streetCode(street.getStreetCode())
                .streetName(street.getStreetName())
                .streetType(street.getStreetType())
                .status(street.getStatus())
                .createdAt(street.getCreatedAt())
                .build();
    }

    @Override
    public Flux<StreetResponse> findByZoneIdIn(java.util.List<String> zoneIds) {
        return streetRepository.findAllByZoneIdIn(zoneIds)
                .map(streetMapper::toDomain)
                .map(this::mapToResponse);
    }
}
