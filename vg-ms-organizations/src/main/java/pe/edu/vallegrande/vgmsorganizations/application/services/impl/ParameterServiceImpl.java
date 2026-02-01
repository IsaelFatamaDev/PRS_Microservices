package pe.edu.vallegrande.vgmsorganizations.application.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.application.services.ParameterService;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameters;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.ParameterRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.ParameterResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.mapper.ParameterMapper;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.repository.ParameterRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
public class ParameterServiceImpl implements ParameterService {

    private final ParameterRepository parameterRepository;
    private final ParameterMapper parameterMapper;

    // Constructor explícito (PRS1 Standard - No usar @RequiredArgsConstructor)
    public ParameterServiceImpl(ParameterRepository parameterRepository, ParameterMapper parameterMapper) {
        this.parameterRepository = parameterRepository;
        this.parameterMapper = parameterMapper;
    }

    @Override
    public Mono<ParameterResponse> create(ParameterRequest request) {
        return generateNextCode()
                .flatMap(code -> {
                    Parameters parameter = Parameters.builder()
                            .parameterCode(code)
                            .organizationId(request.getOrganizationId())
                            .parameterName(request.getParameterName())
                            .parameterValue(request.getParameterValue())
                            .parameterDescription(request.getParameterDescription()) // CORRECCIÓN
                            .status("ACTIVE")
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    return parameterRepository.save(parameterMapper.toEntity(parameter));
                })
                .map(parameterMapper::toDomain)
                .map(this::toResponse)  // Convertimos a Response
                .onErrorResume(e -> {
                    return Mono.error(new RuntimeException("Error al crear el parámetro: " + e.getMessage()));
                });
    }


    @Override
    public Flux<ParameterResponse> findAll() {
        return parameterRepository.findAll()
                .map(parameterMapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    public Mono<ParameterResponse> findById(String id) {
        return parameterRepository.findById(id)
                .map(parameterMapper::toDomain)
                .map(this::toResponse)
                .switchIfEmpty(Mono.error(new RuntimeException("Parameter not found")));
    }

    @Override
    public Mono<ParameterResponse> update(String id, ParameterRequest request) {
        return parameterRepository.findById(id)
                .map(parameterMapper::toDomain)
                .flatMap(existing -> {
                    existing.setParameterName(request.getParameterName());
                    existing.setParameterValue(request.getParameterValue());
                    existing.setParameterDescription(request.getParameterDescription());
                    existing.setUpdatedAt(Instant.now());

                    return parameterRepository.save(parameterMapper.toEntity(existing));
                })
                .map(parameterMapper::toDomain)
                .map(this::toResponse)
                .switchIfEmpty(Mono.error(new RuntimeException("Parameter not found")));
    }

    @Override
    public Mono<Void> delete(String id) {
        return parameterRepository.findById(id)
                .map(parameterMapper::toDomain)
                .flatMap(existing -> {
                    existing.setStatus("INACTIVE");
                    existing.setUpdatedAt(Instant.now());
                    return parameterRepository.save(parameterMapper.toEntity(existing));
                })
                .then();
    }

    @Override
    public Mono<Void> restore(String id) {
        return parameterRepository.findById(id)
                .map(parameterMapper::toDomain)
                .flatMap(existing -> {
                    existing.setStatus("ACTIVE");
                    existing.setUpdatedAt(Instant.now());
                    return parameterRepository.save(parameterMapper.toEntity(existing));
                })
                .then();
    }

    // ======================
    // 🔢 GENERADOR DE CÓDIGO
    // ======================
    private Mono<String> generateNextCode() {
        return parameterRepository.findAll()
                .filter(p -> p.getParameterCode() != null)
                .sort((a, b) -> b.getParameterCode().compareTo(a.getParameterCode()))
                .next()
                .map(last -> {
                    String lastCode = last.getParameterCode();
                    int number = Integer.parseInt(lastCode.replace("PM", ""));
                    return String.format("PM%03d", number + 1);
                })
                .defaultIfEmpty("PM001");
    }

    // ======================
    // 🔁 MAPPER
    // ======================
    private ParameterResponse toResponse(Parameters p) {
        return ParameterResponse.builder()
                .id(p.getId())
                .organizationId(p.getOrganizationId())
                .parameterCode(p.getParameterCode())
                .parameterName(p.getParameterName())
                .parameterValue(p.getParameterValue())
                .parameterDescription(p.getParameterDescription())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    @Override
    public Flux<ParameterResponse> findByOrganizationId(String organizationId) {
        return parameterRepository.findAllByOrganizationId(organizationId)
                .map(parameterMapper::toDomain)
                .map(this::toResponse);
    }

}
