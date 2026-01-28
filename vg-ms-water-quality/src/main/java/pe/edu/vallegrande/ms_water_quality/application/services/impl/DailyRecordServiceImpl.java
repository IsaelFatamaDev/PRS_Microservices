package pe.edu.vallegrande.ms_water_quality.application.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.ms_water_quality.application.services.DailyRecordService;
import pe.edu.vallegrande.ms_water_quality.domain.models.DailyRecord;
import pe.edu.vallegrande.ms_water_quality.domain.models.TestingPoint;
import pe.edu.vallegrande.ms_water_quality.infrastructure.client.dto.ExternalUser;
import pe.edu.vallegrande.ms_water_quality.infrastructure.dto.request.DailyRecordCreateRequest;
import pe.edu.vallegrande.ms_water_quality.infrastructure.dto.response.enriched.DailyRecordEnrichedResponse;
import pe.edu.vallegrande.ms_water_quality.infrastructure.exception.CustomException;
import pe.edu.vallegrande.ms_water_quality.infrastructure.repository.DailyRecordRepository;
import pe.edu.vallegrande.ms_water_quality.infrastructure.repository.TestingPointRepository;
import pe.edu.vallegrande.ms_water_quality.infrastructure.service.ExternalServiceClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyRecordServiceImpl implements DailyRecordService {

    private final DailyRecordRepository dailyRecordRepository;
    private final TestingPointRepository testingPointRepository;
    private final ExternalServiceClient externalServiceClient;
    private final pe.edu.vallegrande.ms_water_quality.infrastructure.mapper.DailyRecordMapper dailyRecordMapper;
    private final pe.edu.vallegrande.ms_water_quality.infrastructure.mapper.TestingPointMapper testingPointMapper;
    private final pe.edu.vallegrande.ms_water_quality.infrastructure.security.JwtService jwtService;

    @Override
    public Flux<DailyRecordEnrichedResponse> getAll() {
        return getCurrentUserOrganizationId()
                .flatMapMany(this::getAllByOrganization);
    }

    @Override
    public Mono<DailyRecordEnrichedResponse> getById(String id) {
        return getCurrentUserOrganizationId()
                .flatMap(orgId -> getByIdAndOrganization(id, orgId));
    }

    @Override
    public Mono<DailyRecordEnrichedResponse> save(DailyRecordCreateRequest request) {
        DailyRecord dailyRecord = new DailyRecord();
        dailyRecord.setOrganizationId(request.getOrganizationId());

        // Generate record code if not provided
        Mono<String> recordCodeMono;
        if (request.getRecordCode() != null && !request.getRecordCode().trim().isEmpty()) {
            recordCodeMono = Mono.just(request.getRecordCode());
        } else {
            recordCodeMono = generateNextRecordCode(request.getRecordType());
        }

        return recordCodeMono.flatMap(recordCode -> {
            dailyRecord.setRecordCode(recordCode);

            // Handle null testingPoints
            List<String> testingPointIds = request.getTestingPointIds() != null ? request.getTestingPointIds()
                    : List.of();
            dailyRecord.setTestingPointIds(testingPointIds);

            dailyRecord.setRecordDate(request.getRecordDate());
            dailyRecord.setLevel(request.getLevel());
            dailyRecord.setAcceptable(request.isAcceptable());
            dailyRecord.setActionRequired(request.isActionRequired());
            dailyRecord.setRecordedByUserId(request.getRecordedByUserId());
            dailyRecord.setObservations(request.getObservations());
            dailyRecord.setAmount(request.getAmount());
            dailyRecord.setRecordType(request.getRecordType());
            dailyRecord.setCreatedAt(LocalDateTime.now());

            return dailyRecordRepository.save(dailyRecordMapper.toDocument(dailyRecord))
                .map(dailyRecordMapper::toDomain)
                .flatMap(this::enrichDailyRecord);
        });
    }

    @Override
    public Mono<DailyRecordEnrichedResponse> update(String id, DailyRecordCreateRequest request) {
        return dailyRecordRepository.findById(id)
                .map(dailyRecordMapper::toDomain)
                .switchIfEmpty(Mono.error(CustomException.notFound("DailyRecord", id)))
                .flatMap(record -> {
                    record.setOrganizationId(request.getOrganizationId());

                    // Handle null testingPoints
                    List<String> testingPointIds = request.getTestingPointIds() != null ? request.getTestingPointIds()
                            : List.of();
                    record.setTestingPointIds(testingPointIds);

                    // Keep the existing record code - don't update it
                    // record.setRecordCode(request.getRecordCode()); // Removed this line
                    record.setRecordDate(request.getRecordDate());
                    record.setLevel(request.getLevel());
                    record.setAcceptable(request.isAcceptable());
                    record.setActionRequired(request.isActionRequired());
                    record.setRecordedByUserId(request.getRecordedByUserId());
                    record.setObservations(request.getObservations());
                    record.setAmount(request.getAmount());
                    record.setRecordType(request.getRecordType());
                    return dailyRecordRepository.save(dailyRecordMapper.toDocument(record))
                        .map(dailyRecordMapper::toDomain);
                })
                .flatMap(this::enrichDailyRecord);
    }

    @Override
    public Mono<Void> delete(String id) {
        return dailyRecordRepository.findById(id)
                .map(dailyRecordMapper::toDomain)
                .switchIfEmpty(Mono.error(CustomException.notFound("DailyRecord", id)))
                .flatMap(record -> {
                    record.setDeletedAt(LocalDateTime.now());
                    return dailyRecordRepository.save(dailyRecordMapper.toDocument(record));
                })
                .then();
    }

    @Override
    public Mono<Void> deletePhysically(String id) {
        return dailyRecordRepository.deleteById(id);
    }

    @Override
    public Mono<DailyRecordEnrichedResponse> restore(String id) {
        return dailyRecordRepository.findById(id)
                .map(dailyRecordMapper::toDomain)
                .switchIfEmpty(Mono.error(CustomException.notFound("DailyRecord", id)))
                .flatMap(record -> {
                    record.setDeletedAt(null);
                    return dailyRecordRepository.save(dailyRecordMapper.toDocument(record))
                        .map(dailyRecordMapper::toDomain);
                })
                .flatMap(this::enrichDailyRecord);
    }

    @Override
    public Flux<DailyRecordEnrichedResponse> getAllByOrganization(String organizationId) {
        return dailyRecordRepository.findAllByOrganizationId(organizationId)
                .map(dailyRecordMapper::toDomain)
                .flatMap(this::enrichDailyRecord);
    }

    @Override
    public Mono<DailyRecordEnrichedResponse> getByIdAndOrganization(String id, String organizationId) {
        return dailyRecordRepository.findById(id)
                .map(dailyRecordMapper::toDomain)
                .filter(record -> record.getOrganizationId().equals(organizationId))
                .flatMap(this::enrichDailyRecord)
                .switchIfEmpty(Mono.error(CustomException.notFound("DailyRecord", id)));
    }

    private Mono<DailyRecordEnrichedResponse> enrichDailyRecord(DailyRecord record) {
        Mono<ExternalUser> userMono = externalServiceClient
                .getAdminsByOrganization(record.getOrganizationId())
                .filter(user -> user.getId() != null && user.getId().equals(record.getRecordedByUserId()))
                .next()
                .defaultIfEmpty(new ExternalUser());

        List<String> testingPointIds = record.getTestingPointIds() != null ? record.getTestingPointIds()
                : Collections.emptyList();

        Flux<TestingPoint> testingPointsFlux = Flux.fromIterable(testingPointIds)
                .flatMap(id -> testingPointRepository.findById(id)
                    .map(testingPointMapper::toDomain))
                .onErrorResume(e -> Mono.empty());

        return Mono.zip(userMono, testingPointsFlux.collectList())
                .map(tuple -> DailyRecordEnrichedResponse.builder()
                        .id(record.getId())
                        .recordCode(record.getRecordCode())
                        .testingPoints(tuple.getT2())
                        .recordDate(record.getRecordDate())
                        .level(record.getLevel())
                        .acceptable(record.isAcceptable())
                        .actionRequired(record.isActionRequired())
                        .observations(record.getObservations())
                        .amount(record.getAmount())
                        .recordType(record.getRecordType())
                        .createdAt(record.getCreatedAt())
                        .recordedByUser(tuple.getT1())
                        .organization(tuple.getT1().getOrganization())
                        .build());
    }

    private String generateRecordCode(String recordType) {
        String prefix = "RC";
        if (recordType != null) {
            switch (recordType.toUpperCase()) {
                case "CLORO":
                    prefix = "CL";
                    break;
                case "SULFATO":
                    prefix = "SU";
                    break;
            }
        }
        return prefix + System.currentTimeMillis() % 100000;
    }

    // Method to generate the next record code based on the last existing code
    private Mono<String> generateNextRecordCode(String recordType) {
        String prefix = getRecordTypePrefix(recordType);

        return dailyRecordRepository.findAll()
                .map(dailyRecordMapper::toDomain)
                .filter(record -> record.getRecordCode() != null && record.getRecordCode().startsWith(prefix))
                .map(record -> record.getRecordCode())
                .map(code -> {
                    try {
                        return Integer.parseInt(code.substring(prefix.length()));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .reduce(Integer::max)
                .defaultIfEmpty(0)
                .map(maxNumber -> prefix + String.format("%05d", maxNumber + 1));
    }

    private String getRecordTypePrefix(String recordType) {
        if (recordType != null) {
            switch (recordType.toUpperCase()) {
                case "CLORO":
                    return "CL";
                case "SULFATO":
                    return "SU";
                default:
                    return "RC";
            }
        }
        return "RC";
    }

    /**
     * Obtiene el organizationId del JWT token del usuario autenticado
     * Según estándar PRS01
     */
    private Mono<String> getCurrentUserOrganizationId() {
        return jwtService.getOrganizationIdFromToken()
            .onErrorResume(error -> {
                log.warn("No se pudo obtener organizationId del JWT, usando fallback: {}", error.getMessage());
                return dailyRecordRepository.findAll()
                    .map(dailyRecordMapper::toDomain)
                    .map(DailyRecord::getOrganizationId)
                    .distinct()
                    .next()
                    .switchIfEmpty(Mono.just("default-org-id"));
            });
    }
}
