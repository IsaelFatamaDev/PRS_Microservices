package pe.edu.vallegrande.vgmsorganizations.infrastructure.rest.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.vallegrande.vgmsorganizations.application.services.OrganizationService;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common.ErrorMessage;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common.ResponseDto;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.OrganizationResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.OrganizationStatsResponse;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import java.util.List;

@RestController
@RequestMapping("/api/internal")
@Slf4j
@Validated
public class InternalRest {

        private final OrganizationService organizationService;

        public InternalRest(OrganizationService organizationService) {
                this.organizationService = organizationService;
        }

        /**
         * Obtener todas las organizaciones con datos básicos (sin zonas/calles)
         */
        /**
         * Obtener todas las organizaciones con datos básicos (sin zonas/calles)
         */
        @GetMapping("/organizations")
        public Mono<ResponseEntity<ResponseDto<List<OrganizationStatsResponse>>>> getOrganizationsLight() {
                return organizationService.findAllLight()
                                .collectList()
                                .map(orgs -> ResponseEntity.ok(new ResponseDto<>(true, orgs,
                                                "Organizaciones obtenidas correctamente")))
                                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body(new ResponseDto<>(false,
                                                                new ErrorMessage(500, "Error al obtener Organizaciones",
                                                                                e.getMessage())))));
        }

        /**
         * Obtener organización por ID con datos básicos (sin zonas/calles)
         */
        /**
         * Obtener organización por ID con datos básicos (sin zonas/calles)
         */
        @GetMapping("/organizations/{id}")
        public Mono<ResponseEntity<ResponseDto<OrganizationResponse>>> getOrganizationByIdLight(
                        @PathVariable String id) {
                return organizationService.findByIdLight(id)
                                .map(org -> ResponseEntity.ok(
                                                new ResponseDto<>(true, org, "Organización obtenida correctamente")))
                                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body(new ResponseDto<>(false,
                                                                new ErrorMessage(500, "Error al obtener Organización",
                                                                                e.getMessage())))));
        }

        /**
         * Obtener organización por ID con todos los datos (zonas, calles, parámetros)
         */
        /**
         * Obtener organización por ID con todos los datos (zonas, calles, parámetros)
         */
        @GetMapping("/organizations/{id}/complete")
        public Mono<ResponseEntity<ResponseDto<OrganizationResponse>>> getOrganizationComplete(
                        @PathVariable String id) {
                return organizationService.findById(id)
                                .map(org -> ResponseEntity.ok(new ResponseDto<>(true, org,
                                                "Organización completa obtenida correctamente")))
                                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body(new ResponseDto<>(false,
                                                                new ErrorMessage(500,
                                                                                "Error al obtener Organización completa",
                                                                                e.getMessage())))));
        }

        /**
         * Obtener todas las organizaciones con estadísticas (contadores de zonas y
         * calles)
         */
        /**
         * Obtener todas las organizaciones con estadísticas (contadores de zonas y
         * calles)
         */
        @GetMapping("/organizations/stats")
        public Mono<ResponseEntity<ResponseDto<List<OrganizationStatsResponse>>>> getOrganizationsWithStats() {
                return organizationService.findAllWithStats()
                                .collectList()
                                .map(orgs -> ResponseEntity.ok(
                                                new ResponseDto<>(true, orgs, "Estadísticas obtenidas correctamente")))
                                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body(new ResponseDto<>(false,
                                                                new ErrorMessage(500, "Error al obtener estadísticas",
                                                                                e.getMessage())))));
        }

        /**
         * Obtener organización por ID con estadísticas (contadores de zonas y calles)
         */
        /**
         * Obtener organización por ID con estadísticas (contadores de zonas y calles)
         */
        @GetMapping("/organizations/{id}/stats")
        public Mono<ResponseEntity<ResponseDto<OrganizationStatsResponse>>> getOrganizationByIdWithStats(
                        @PathVariable String id) {
                return organizationService.findByIdWithStats(id)
                                .map(org -> ResponseEntity.ok(new ResponseDto<>(true, org,
                                                "Estadística de organización obtenida correctamente")))
                                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body(new ResponseDto<>(false,
                                                                new ErrorMessage(500,
                                                                                "Error al obtener estadística de Organización",
                                                                                e.getMessage())))));
        }

        /**
         * Test endpoint para verificar conexión a base de datos
         */
        /**
         * Test endpoint para verificar conexión a base de datos
         */
        @GetMapping("/test/organizations/count")
        public Mono<ResponseEntity<ResponseDto<Long>>> testOrganizationsCount() {
                return organizationService.findAll()
                                .count()
                                .map(count -> ResponseEntity
                                                .ok(new ResponseDto<>(true, count, "Conteo realizado correctamente")))
                                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body(new ResponseDto<>(false, 0L, "Error al contar organizaciones"))));
        }
}
