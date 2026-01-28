package pe.edu.vallegrande.ms_water_quality.infrastructure.rest.admin;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.ms_water_quality.application.services.DailyRecordService;
import pe.edu.vallegrande.ms_water_quality.application.services.QualityTestService;
import pe.edu.vallegrande.ms_water_quality.application.services.TestingPointService;
import pe.edu.vallegrande.ms_water_quality.domain.models.TestingPoint;
import pe.edu.vallegrande.ms_water_quality.infrastructure.dto.ResponseDto;
import pe.edu.vallegrande.ms_water_quality.infrastructure.dto.request.DailyRecordCreateRequest;
import pe.edu.vallegrande.ms_water_quality.infrastructure.dto.request.QualityTestCreateRequest;
import pe.edu.vallegrande.ms_water_quality.infrastructure.dto.request.TestingPointCreateRequest;
import pe.edu.vallegrande.ms_water_quality.infrastructure.dto.response.TestingPointResponse;
import pe.edu.vallegrande.ms_water_quality.infrastructure.dto.response.enriched.DailyRecordEnrichedResponse;
import pe.edu.vallegrande.ms_water_quality.infrastructure.dto.response.enriched.QualityTestEnrichedResponse;
import pe.edu.vallegrande.ms_water_quality.infrastructure.dto.response.enriched.TestingPointEnrichedResponse;
import pe.edu.vallegrande.ms_water_quality.infrastructure.exception.CustomException;
import pe.edu.vallegrande.ms_water_quality.infrastructure.security.JwtService;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/admin/quality")
@PreAuthorize("hasRole('ADMIN')")
@Validated
@Slf4j
public class AdminRest {

    private final TestingPointService testingPointService;
    private final QualityTestService qualityTestService;
    private final DailyRecordService dailyRecordService;
    private final JwtService jwtService;

    // Constructor explícito según estándar PRS01
    public AdminRest(TestingPointService testingPointService,
            QualityTestService qualityTestService,
            DailyRecordService dailyRecordService,
            JwtService jwtService) {
        this.testingPointService = testingPointService;
        this.qualityTestService = qualityTestService;
        this.dailyRecordService = dailyRecordService;
        this.jwtService = jwtService;
    }

    // #region Testing Points

    @GetMapping("/sampling-points")
    public Mono<ResponseDto<List<TestingPointEnrichedResponse>>> getAllTestingPoints() {
        log.info("Getting testing points for current user's organization");
        return jwtService.getOrganizationIdFromToken()
                .flatMapMany(testingPointService::getAllByOrganization)
                .collectList()
                .map(list -> new ResponseDto<>(true, list, null));
    }

    @GetMapping("/sampling-points/active")
    public Mono<ResponseDto<List<TestingPointEnrichedResponse>>> getAllActiveTestingPoints() {
        log.info("Getting active testing points for current user's organization");
        return jwtService.getOrganizationIdFromToken()
                .flatMapMany(testingPointService::getAllActiveByOrganization)
                .collectList()
                .map(list -> new ResponseDto<>(true, list, null));
    }

    @GetMapping("/sampling-points/inactive")
    public Mono<ResponseDto<List<TestingPointEnrichedResponse>>> getAllInactiveTestingPoints() {
        log.info("Getting inactive testing points for current user's organization");
        return jwtService.getOrganizationIdFromToken()
                .flatMapMany(testingPointService::getAllInactiveByOrganization)
                .collectList()
                .map(list -> new ResponseDto<>(true, list, null));
    }

    @GetMapping("/sampling-points/{id}")
    public Mono<ResponseDto<TestingPointEnrichedResponse>> getTestingPointById(@PathVariable String id) {
        log.info("Getting testing point by id: {} for current user's organization", id);
        return jwtService.getOrganizationIdFromToken()
                .flatMap(orgId -> testingPointService.getByIdAndOrganization(id, orgId))
                .map(data -> new ResponseDto<>(true, data, null))
                .switchIfEmpty(Mono.error(CustomException.notFound("TestingPoint", id)));
    }

    @PostMapping("/sampling-points")
    public Mono<ResponseEntity<ResponseDto<TestingPointResponse>>> saveTestingPoint(
            @Valid @RequestBody TestingPointCreateRequest request) {
        log.info("Creating new testing point: {}", request.getPointName());
        return jwtService.getOrganizationIdFromToken()
                .flatMap(orgId -> {
                    request.setOrganizationId(orgId);
                    return testingPointService.save(request);
                })
                .map(data -> ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDto<>(true, data, null)));
    }

    @PutMapping("/sampling-points/{id}")
    public Mono<ResponseDto<TestingPoint>> updateTestingPoint(@PathVariable String id,
            @RequestBody TestingPoint point) {
        return jwtService.getOrganizationIdFromToken()
                .flatMap(orgId -> {
                    point.setOrganizationId(orgId);
                    return testingPointService.update(id, point);
                })
                .map(data -> new ResponseDto<>(true, data, null))
                .switchIfEmpty(Mono.error(CustomException.notFound("TestingPoint", id)));
    }

    @DeleteMapping("/sampling-points/{id}")
    public Mono<ResponseDto<Void>> deleteTestingPoint(@PathVariable String id) {
        return testingPointService.delete(id).then(Mono.just(new ResponseDto<>(true, null, null)));
    }

    @PatchMapping("/sampling-points/activate/{id}")
    public Mono<ResponseDto<TestingPointEnrichedResponse>> activateTestingPoint(@PathVariable String id) {
        return testingPointService.activate(id)
                .map(data -> new ResponseDto<>(true, data, null))
                .switchIfEmpty(Mono.error(CustomException.notFound("TestingPoint", id)));
    }

    @PatchMapping("/sampling-points/deactivate/{id}")
    public Mono<ResponseDto<TestingPointEnrichedResponse>> deactivateTestingPoint(@PathVariable String id) {
        return testingPointService.deactivate(id)
                .map(data -> new ResponseDto<>(true, data, null))
                .switchIfEmpty(Mono.error(CustomException.notFound("TestingPoint", id)));
    }

    // Debug endpoint
    @GetMapping("/sampling-points/debug")
    public Mono<ResponseDto<Object>> debugSamplingPoints() {
        return testingPointService.debugGetAll()
                .map(data -> new ResponseDto<>(true, data, null));
    }

    // #region Quality Tests

    @GetMapping("/tests")
    public Mono<ResponseDto<List<QualityTestEnrichedResponse>>> getAllTests() {
        log.info("Getting quality tests for current user's organization");
        return jwtService.getOrganizationIdFromToken()
                .flatMapMany(qualityTestService::getAllByOrganization)
                .collectList()
                .map(list -> new ResponseDto<>(true, list, null));
    }

    @GetMapping("/tests/{id}")
    public Mono<ResponseDto<QualityTestEnrichedResponse>> getTestById(@PathVariable String id) {
        log.info("Getting quality test by id: {} for current user's organization", id);
        return jwtService.getOrganizationIdFromToken()
                .flatMap(orgId -> qualityTestService.getByIdAndOrganization(id, orgId))
                .map(data -> new ResponseDto<>(true, data, null))
                .switchIfEmpty(Mono.error(CustomException.notFound("QualityTest", id)));
    }

    @PostMapping("/tests")
    public Mono<ResponseEntity<ResponseDto<QualityTestEnrichedResponse>>> saveTest(
            @RequestBody QualityTestCreateRequest request) {
        return jwtService.getOrganizationIdFromToken()
                .flatMap(orgId -> {
                    request.setOrganizationId(orgId);
                    return qualityTestService.save(request);
                })
                .map(data -> ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDto<>(true, data, null)));
    }

    @PutMapping("/tests/{id}")
    public Mono<ResponseDto<QualityTestEnrichedResponse>> updateTest(@PathVariable String id,
            @RequestBody QualityTestCreateRequest request) {
        return jwtService.getOrganizationIdFromToken()
                .flatMap(orgId -> {
                    request.setOrganizationId(orgId);
                    return qualityTestService.update(id, request);
                })
                .map(data -> new ResponseDto<>(true, data, null))
                .switchIfEmpty(Mono.error(CustomException.notFound("QualityTest", id)));
    }

    @DeleteMapping("/tests/{id}")
    public Mono<ResponseDto<Void>> deleteTest(@PathVariable String id) {
        return qualityTestService.delete(id).then(Mono.just(new ResponseDto<>(true, null, null)));
    }

    @DeleteMapping("/tests/physical/{id}")
    public Mono<ResponseDto<Void>> deleteTestPhysically(@PathVariable String id) {
        return qualityTestService.deletePhysically(id).then(Mono.just(new ResponseDto<>(true, null, null)));
    }

    @PatchMapping("/tests/restore/{id}")
    public Mono<ResponseDto<QualityTestEnrichedResponse>> restoreTest(@PathVariable String id) {
        return qualityTestService.restore(id)
                .map(data -> new ResponseDto<>(true, data, null))
                .switchIfEmpty(Mono.error(CustomException.notFound("QualityTest", id)));
    }

    // #region Daily Records

    @GetMapping("/daily-records")
    public Mono<ResponseDto<List<DailyRecordEnrichedResponse>>> getAllDailyRecords() {
        log.info("Getting daily records for current user's organization");
        return jwtService.getOrganizationIdFromToken()
                .flatMapMany(dailyRecordService::getAllByOrganization)
                .collectList()
                .map(list -> new ResponseDto<>(true, list, null));
    }

    @GetMapping("/daily-records/{id}")
    public Mono<ResponseDto<DailyRecordEnrichedResponse>> getDailyRecordById(@PathVariable String id) {
        log.info("Getting daily record by id: {} for current user's organization", id);
        return jwtService.getOrganizationIdFromToken()
                .flatMap(orgId -> dailyRecordService.getByIdAndOrganization(id, orgId))
                .map(data -> new ResponseDto<>(true, data, null))
                .switchIfEmpty(Mono.error(CustomException.notFound("DailyRecord", id)));
    }

    @PostMapping("/daily-records")
    public Mono<ResponseEntity<ResponseDto<DailyRecordEnrichedResponse>>> saveDailyRecord(
            @RequestBody DailyRecordCreateRequest request) {
        return jwtService.getOrganizationIdFromToken()
                .flatMap(orgId -> {
                    request.setOrganizationId(orgId);
                    return dailyRecordService.save(request);
                })
                .map(data -> ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDto<>(true, data, null)));
    }

    @PutMapping("/daily-records/{id}")
    public Mono<ResponseDto<DailyRecordEnrichedResponse>> updateDailyRecord(@PathVariable String id,
            @RequestBody DailyRecordCreateRequest request) {
        return jwtService.getOrganizationIdFromToken()
                .flatMap(orgId -> {
                    request.setOrganizationId(orgId);
                    return dailyRecordService.update(id, request);
                })
                .map(data -> new ResponseDto<>(true, data, null))
                .switchIfEmpty(Mono.error(CustomException.notFound("DailyRecord", id)));
    }

    @DeleteMapping("/daily-records/{id}")
    public Mono<ResponseDto<Void>> deleteDailyRecord(@PathVariable String id) {
        return dailyRecordService.delete(id).then(Mono.just(new ResponseDto<>(true, null, null)));
    }

    @DeleteMapping("/daily-records/physical/{id}")
    public Mono<ResponseDto<Void>> deleteDailyRecordPhysically(@PathVariable String id) {
        return dailyRecordService.deletePhysically(id).then(Mono.just(new ResponseDto<>(true, null, null)));
    }

    @PatchMapping("/daily-records/restore/{id}")
    public Mono<ResponseDto<DailyRecordEnrichedResponse>> restoreDailyRecord(@PathVariable String id) {
        return dailyRecordService.restore(id)
                .map(data -> new ResponseDto<>(true, data, null))
                .switchIfEmpty(Mono.error(CustomException.notFound("DailyRecord", id)));
    }
}
