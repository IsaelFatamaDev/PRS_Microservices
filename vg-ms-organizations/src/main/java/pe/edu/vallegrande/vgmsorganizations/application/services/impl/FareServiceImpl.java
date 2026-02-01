package pe.edu.vallegrande.vgmsorganizations.application.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.application.services.FareService;
import pe.edu.vallegrande.vgmsorganizations.domain.enums.Constants;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.FareRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.FareResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.CustomException;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.mapper.FareMapper;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.repository.FareRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.repository.ZoneRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
public class FareServiceImpl implements FareService {

        private final FareRepository fareRepository;
        private final ZoneRepository zoneRepository;

        // Constructor explícito (PRS1 Standard - No usar @RequiredArgsConstructor)
        public FareServiceImpl(FareRepository fareRepository, ZoneRepository zoneRepository) {
                this.fareRepository = fareRepository;
                this.zoneRepository = zoneRepository;
        }

        @Override
        public Mono<FareResponse> create(FareRequest request) {
                // Validar que la zona existe
                return zoneRepository.findById(request.getZoneId())
                                .switchIfEmpty(Mono.error(new CustomException(404, "Zona no encontrada",
                                                "La zona con ID " + request.getZoneId() + " no existe")))
                                .flatMap(zone ->
                                // Desactivar todas las tarifas activas de esta zona
                                fareRepository.findAllByZoneIdAndStatus(request.getZoneId(), Constants.ACTIVE)
                                                .map(FareMapper::toDomain)
                                                .flatMap(activeFare -> {
                                                        activeFare.setStatus(Constants.INACTIVE);
                                                        activeFare.setUpdatedAt(Instant.now());
                                                        return fareRepository.save(FareMapper.toDocument(activeFare));
                                                })
                                                .then(Mono.defer(() ->
                                                // Generar código y crear la nueva tarifa
                                                fareRepository.findFirstByOrderByFareCodeDesc()
                                                                .map(fare -> generateFareCode(fare.getFareCode()))
                                                                .defaultIfEmpty("TAR001")
                                                                .flatMap(fareCode -> {
                                                                        Fare fare = FareMapper.toDomain(request);
                                                                        fare.setFareCode(fareCode);
                                                                        fare.setStatus(Constants.ACTIVE);
                                                                        fare.setCreatedAt(Instant.now());

                                                                        return fareRepository
                                                                                        .save(FareMapper.toDocument(
                                                                                                        fare))
                                                                                        .map(FareMapper::toDomain)
                                                                                        .map(savedFare -> FareMapper
                                                                                                        .toResponse(
                                                                                                                        savedFare,
                                                                                                                        zone.getZoneName()));
                                                                }))));
        }

        @Override
        public Flux<FareResponse> findAll() {
                return fareRepository.findAll()
                                .map(FareMapper::toDomain)
                                .flatMap(this::enrichWithZoneName);
        }

        @Override
        public Mono<FareResponse> findById(String id) {
                return fareRepository.findById(id)
                                .switchIfEmpty(Mono.error(new CustomException(404, "Tarifa no encontrada",
                                                "La tarifa con ID " + id + " no existe")))
                                .map(FareMapper::toDomain)
                                .flatMap(this::enrichWithZoneName);
        }

        @Override
        public Flux<FareResponse> findByZoneId(String zoneId) {
                return fareRepository.findAllByZoneId(zoneId)
                                .map(FareMapper::toDomain)
                                .flatMap(this::enrichWithZoneName);
        }

        @Override
        public Mono<FareResponse> update(String id, FareRequest request) {
                return fareRepository.findById(id)
                                .switchIfEmpty(Mono.error(new CustomException(404, "Tarifa no encontrada",
                                                "La tarifa con ID " + id + " no existe")))
                                // Validar que la zona existe si se está actualizando
                                .flatMap(existingDoc -> zoneRepository.findById(request.getZoneId())
                                                .switchIfEmpty(Mono.error(new CustomException(404, "Zona no encontrada",
                                                                "La zona con ID " + request.getZoneId()
                                                                                + " no existe")))
                                                .map(zone -> {
                                                        Fare existing = FareMapper.toDomain(existingDoc);
                                                        existing.setZoneId(request.getZoneId());
                                                        existing.setFareName(request.getFareName());
                                                        existing.setFareDescription(request.getFareDescription());
                                                        existing.setFareAmount(request.getFareAmount());
                                                        existing.setUpdatedAt(Instant.now());
                                                        return existing;
                                                })
                                                .flatMap(updatedFare -> fareRepository
                                                                .save(FareMapper.toDocument(updatedFare))
                                                                .map(FareMapper::toDomain)
                                                                .flatMap(this::enrichWithZoneName)));
        }

        @Override
        public Mono<Void> delete(String id) {
                return fareRepository.findById(id)
                                .switchIfEmpty(Mono.error(new CustomException(404, "Tarifa no encontrada",
                                                "La tarifa con ID " + id + " no existe")))
                                .map(FareMapper::toDomain)
                                .flatMap(fare -> {
                                        fare.setStatus(Constants.INACTIVE);
                                        fare.setUpdatedAt(Instant.now());
                                        return fareRepository.save(FareMapper.toDocument(fare));
                                })
                                .then();
        }

        @Override
        public Mono<Void> restore(String id) {
                return fareRepository.findById(id)
                                .switchIfEmpty(Mono.error(new CustomException(404, "Tarifa no encontrada",
                                                "La tarifa con ID " + id + " no existe")))
                                .map(FareMapper::toDomain)
                                .flatMap(fare -> {
                                        fare.setStatus(Constants.ACTIVE);
                                        fare.setUpdatedAt(Instant.now());
                                        return fareRepository.save(FareMapper.toDocument(fare));
                                })
                                .then();
        }

        /**
         * Enriquece la respuesta con el nombre de la zona
         */
        private Mono<FareResponse> enrichWithZoneName(Fare fare) {
                return zoneRepository.findById(fare.getZoneId())
                                .map(zone -> FareMapper.toResponse(fare, zone.getZoneName()))
                                .defaultIfEmpty(FareMapper.toResponse(fare, "Zona no encontrada"));
        }

        /**
         * Genera el siguiente código de tarifa
         */
        private String generateFareCode(String lastCode) {
                if (lastCode == null || lastCode.isEmpty()) {
                        return "TAR001";
                }

                // Extraer el número del código anterior
                String numberPart = lastCode.substring(3); // Quitar "TAR"
                int nextNumber = Integer.parseInt(numberPart) + 1;

                // Formatear con ceros a la izquierda
                return String.format("TAR%03d", nextNumber);
        }
}
