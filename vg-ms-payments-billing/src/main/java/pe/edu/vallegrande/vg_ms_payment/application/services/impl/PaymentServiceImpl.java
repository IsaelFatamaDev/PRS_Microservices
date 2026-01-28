package pe.edu.vallegrande.vg_ms_payment.application.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vg_ms_payment.application.services.PaymentService;
import pe.edu.vallegrande.vg_ms_payment.domain.models.Payment;
import pe.edu.vallegrande.vg_ms_payment.domain.models.PaymentDetail;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.request.PaymentCreateRequest;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.response.PaymentResponse;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.response.EnrichedPaymentResponse;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.repository.PaymentDetailRepository;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.repository.PaymentRepository;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.service.UserService;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.mapper.PaymentMapper;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.mapper.PaymentDetailMapper;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.mapper.PaymentDtoMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final UserService userService;
    private final PaymentMapper paymentMapper;
    private final PaymentDetailMapper paymentDetailMapper;
    private final PaymentDtoMapper paymentDtoMapper;

    @Override
    public Mono<EnrichedPaymentResponse> getEnrichedById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .map(paymentMapper::entityToDomain)
                .flatMap(payment -> 
                    paymentDetailRepository.findByPaymentId(payment.getPaymentId())
                        .map(paymentDetailMapper::entityToDomain)
                        .collectList()
                        .flatMap(details -> {
                            // Obtener información completa del usuario
                            Mono<UserService.UserResponse> userMono = userService.getUserByIdAutoAuth(payment.getUserId(), payment.getOrganizationId())
                                    .onErrorResume(error -> {
                                        log.warn("No se pudo obtener información completa del usuario: {}, intentando método básico", error.getMessage());
                                        return userService.getUserByIdAutoAuth(payment.getUserId())
                                                .onErrorResume(error2 -> {
                                                    log.warn("No se pudo obtener información del usuario: {}", error2.getMessage());
                                                    UserService.UserResponse emptyUser = new UserService.UserResponse();
                                                    emptyUser.setFirstName("N/A");
                                                    emptyUser.setLastName("N/A");
                                                    return Mono.just(emptyUser);
                                                });
                                    });
                            
                            // Obtener información de la organización
                            Mono<UserService.OrganizationInfo> orgMono = userService.getOrganizationByIdAutoAuth(payment.getOrganizationId())
                                    .onErrorResume(error -> {
                                        log.warn("No se pudo obtener información de la organización: {}", error.getMessage());
                                        UserService.OrganizationInfo emptyOrg = new UserService.OrganizationInfo();
                                        emptyOrg.setOrganizationName("N/A");
                                        return Mono.just(emptyOrg);
                                    });
                            
                            return Mono.zip(userMono, orgMono)
                                    .map(tuple -> {
                                        UserService.UserResponse userResponse = tuple.getT1();
                                        UserService.OrganizationInfo orgResponse = tuple.getT2();
                                        
                                        return buildEnrichedPaymentResponse(payment, details, userResponse, orgResponse);
                                    });
                        })
                )
                .doOnNext(payment -> log.info("enriched payment retrieved by id: {}", payment.getPaymentId()));
    }

    @Override
    public Flux<EnrichedPaymentResponse> getAllEnriched() {
        return paymentRepository.findAll()
                .map(paymentMapper::entityToDomain)
                .flatMap(payment -> 
                    paymentDetailRepository.findByPaymentId(payment.getPaymentId())
                        .map(paymentDetailMapper::entityToDomain)
                        .collectList()
                        .flatMap(details -> {
                            // Obtener información completa del usuario
                            Mono<UserService.UserResponse> userMono = userService.getUserByIdAutoAuth(payment.getUserId(), payment.getOrganizationId())
                                    .onErrorResume(error -> {
                                        log.warn("No se pudo obtener información completa del usuario: {}, intentando método básico", error.getMessage());
                                        return userService.getUserByIdAutoAuth(payment.getUserId())
                                                .onErrorResume(error2 -> {
                                                    log.warn("No se pudo obtener información del usuario: {}", error2.getMessage());
                                                    UserService.UserResponse emptyUser = new UserService.UserResponse();
                                                    emptyUser.setFirstName("N/A");
                                                    emptyUser.setLastName("N/A");
                                                    return Mono.just(emptyUser);
                                                });
                                    });
                            
                            // Obtener información de la organización
                            Mono<UserService.OrganizationInfo> orgMono = userService.getOrganizationByIdAutoAuth(payment.getOrganizationId())
                                    .onErrorResume(error -> {
                                        log.warn("No se pudo obtener información de la organización: {}", error.getMessage());
                                        UserService.OrganizationInfo emptyOrg = new UserService.OrganizationInfo();
                                        emptyOrg.setOrganizationName("N/A");
                                        return Mono.just(emptyOrg);
                                    });
                            
                            return Mono.zip(userMono, orgMono)
                                    .map(tuple -> {
                                        UserService.UserResponse userResponse = tuple.getT1();
                                        UserService.OrganizationInfo orgResponse = tuple.getT2();
                                        
                                        return buildEnrichedPaymentResponse(payment, details, userResponse, orgResponse);
                                    });
                        })
                )
                .doOnNext(payment -> log.info("enriched payment retrieved: {}", payment.getPaymentId()));
    }

    @Override
    public Mono<PaymentResponse> update(String paymentId, PaymentCreateRequest request) {
        return paymentRepository.findById(paymentId)
                .map(paymentMapper::entityToDomain)
                .flatMap(existingPayment -> {
                    // Actualizar campos del pago usando el mapper
                    Payment updatedPayment = paymentDtoMapper.requestToDomain(request);
                    updatedPayment.setPaymentId(existingPayment.getPaymentId());
                    updatedPayment.setCreatedAt(existingPayment.getCreatedAt());
                    updatedPayment.setUpdatedAt(LocalDateTime.now());
                    updatedPayment.setNew(false);

                    return paymentRepository.save(paymentMapper.domainToEntity(updatedPayment))
                            .map(paymentMapper::entityToDomain)
                            .flatMap(savedPayment -> {
                                if (request.getDetails() == null || request.getDetails().isEmpty()) {
                                    return Mono.just(savedPayment);
                                }
                                
                                // Eliminar detalles existentes y guardar nuevos
                                return paymentDetailRepository.deleteByPaymentId(savedPayment.getPaymentId())
                                        .then(Flux.fromIterable(request.getDetails())
                                                .flatMap(detailRequest -> {
                                                    PaymentDetail detail = paymentDtoMapper.detailRequestToDomain(detailRequest, savedPayment.getPaymentId());
                                                    return paymentDetailRepository.save(paymentDetailMapper.domainToEntity(detail));
                                                })
                                                .then(Mono.just(savedPayment)));
                            })
                            .flatMap(savedPayment ->
                                    paymentDetailRepository.findByPaymentId(savedPayment.getPaymentId())
                                            .map(paymentDetailMapper::entityToDomain)
                                            .collectList()
                                            .map(detailsList -> {
                                                PaymentResponse response = paymentDtoMapper.domainToResponse(savedPayment);
                                                response.setDetails(paymentDtoMapper.detailDomainListToResponseList(detailsList));
                                                return response;
                                            })
                            );
                })
                .doOnNext(payment -> log.info("payment updated: {}", payment));
    }

    @Override
    public Mono<PaymentResponse> save(PaymentCreateRequest request) {
        log.info("Starting payment creation with request: {}", request);
        
        try {
            // Usar el mapper para convertir request a domain
            Payment payment = paymentDtoMapper.requestToDomain(request);
            log.info("Payment object created: {}", payment);

            // Convertir domain a entity y guardar
            return paymentRepository.save(paymentMapper.domainToEntity(payment))
                    .map(paymentMapper::entityToDomain)
                    .flatMap(savedPayment -> {
                        log.info("Payment saved successfully: {}", savedPayment.getPaymentId());
                        
                        if (request.getDetails() == null || request.getDetails().isEmpty()) {
                            log.info("No payment details to save");
                            return Mono.just(savedPayment);
                        }
                        
                        log.info("Saving {} payment details", request.getDetails().size());
                        
                        // Guardar detalles usando mappers
                        return Flux.fromIterable(request.getDetails())
                                .flatMap(detailRequest -> {
                                    PaymentDetail detail = paymentDtoMapper.detailRequestToDomain(detailRequest, savedPayment.getPaymentId());
                                    log.info("Saving payment detail: {}", detail);
                                    return paymentDetailRepository.save(paymentDetailMapper.domainToEntity(detail));
                                })
                                .then(Mono.just(savedPayment));
                    })
                    // 🚀 Cargar detalles después de insertarlos
                    .flatMap(savedPayment -> {
                        log.info("Loading payment details for payment: {}", savedPayment.getPaymentId());
                        return paymentDetailRepository.findByPaymentId(savedPayment.getPaymentId())
                                .map(paymentDetailMapper::entityToDomain)
                                .collectList()
                                .map(detailsList -> {
                                    log.info("Found {} payment details", detailsList.size());
                                    PaymentResponse response = paymentDtoMapper.domainToResponse(savedPayment);
                                    response.setDetails(paymentDtoMapper.detailDomainListToResponseList(detailsList));
                                    return response;
                                });
                    })
                    .doOnError(error -> {
                        log.error("Error in payment creation process: {}", error.getMessage(), error);
                    });
        } catch (Exception e) {
            log.error("Unexpected error in payment creation: {}", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<Void> deleteById(String paymentId) {
        log.info("Starting physical deletion of payment with ID: {}", paymentId);
        
        return paymentRepository.findById(paymentId)
                .flatMap(payment -> {
                    log.info("Found payment to delete: {}", payment.getPaymentId());
                    
                    // Primero eliminar todos los detalles del pago usando el nuevo método
                    return paymentDetailRepository.deleteByPaymentId(paymentId)
                            .then(paymentRepository.deleteById(paymentId))
                            .doOnSuccess(v -> log.info("Payment and all its details deleted successfully"))
                            .doOnError(error -> log.error("Error deleting payment: {}", error.getMessage()));
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Payment not found with ID: " + paymentId)))
                .then();
    }

    @Override
    public Mono<Void> deleteAll() {
        log.info("Starting bulk physical deletion of all payments and payment details");
        
        return paymentDetailRepository.deleteAll()
                .doOnSuccess(v -> log.info("All payment details deleted successfully"))
                .doOnError(error -> log.error("Error deleting payment details: {}", error.getMessage()))
                .then(paymentRepository.deleteAll())
                .doOnSuccess(v -> log.info("All payments deleted successfully"))
                .doOnError(error -> log.error("Error deleting payments: {}", error.getMessage()))
                .doOnSuccess(v -> log.info("Bulk deletion completed successfully"))
                .then();
    }

    @Override
    public Flux<PaymentResponse> getAll() {
        return paymentRepository.findAll()
                .map(paymentMapper::entityToDomain)
                .flatMap(payment -> 
                    paymentDetailRepository.findByPaymentId(payment.getPaymentId())
                        .map(paymentDetailMapper::entityToDomain)
                        .collectList()
                        .map(details -> {
                            PaymentResponse response = paymentDtoMapper.domainToResponse(payment);
                            response.setDetails(paymentDtoMapper.detailDomainListToResponseList(details));
                            return response;
                        })
                )
                .doOnNext(payment -> log.debug("Payment retrieved: {}", payment.getPaymentId()));
    }

    @Override
    public Mono<PaymentResponse> getById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .map(paymentMapper::entityToDomain)
                .flatMap(payment -> 
                    paymentDetailRepository.findByPaymentId(payment.getPaymentId())
                        .map(paymentDetailMapper::entityToDomain)
                        .collectList()
                        .map(details -> {
                            PaymentResponse response = paymentDtoMapper.domainToResponse(payment);
                            response.setDetails(paymentDtoMapper.detailDomainListToResponseList(details));
                            return response;
                        })
                )
                .doOnNext(payment -> log.info("Payment retrieved by id: {}", payment.getPaymentId()));
    }

    /**
     * Construye la respuesta enriquecida con toda la información del usuario y organización
     */
    private EnrichedPaymentResponse buildEnrichedPaymentResponse(Payment payment, List<PaymentDetail> details, 
                                                               UserService.UserResponse userResponse, 
                                                               UserService.OrganizationInfo orgResponse) {
        EnrichedPaymentResponse response = new EnrichedPaymentResponse();
        
        // Información del pago
        response.setPaymentId(payment.getPaymentId());
        response.setOrganizationId(payment.getOrganizationId());
        response.setPaymentCode(payment.getPaymentCode());
        response.setUserId(payment.getUserId());
        response.setWaterBoxId(payment.getWaterBoxId());
        response.setPaymentType(payment.getPaymentType());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setTotalAmount(payment.getTotalAmount());
        response.setPaymentDate(payment.getPaymentDate());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setExternalReference(payment.getExternalReference());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        response.setDetails(paymentDtoMapper.detailDomainListToResponseList(details));
        
        // Información de la organización (del endpoint de organización)
        if (orgResponse != null) {
            response.setOrganizationName(orgResponse.getOrganizationName() != null ? orgResponse.getOrganizationName() : "N/A");
            response.setOrganizationLogo(orgResponse.getLogo());
        }
        
        // Información completa del usuario
        if (userResponse != null) {
            response.setUserName(userResponse.getFullName() != null && !userResponse.getFullName().isEmpty() ? userResponse.getFullName() : "N/A");
            response.setFirstName(userResponse.getFirstName());
            response.setLastName(userResponse.getLastName());
            response.setUserDocument(userResponse.getDocumentNumber());
            response.setEmail(userResponse.getEmail());
            response.setPhone(userResponse.getPhone());
            response.setUserAddress(userResponse.getAddress());
            response.setFareAmount(userResponse.getFareAmount() != null ? userResponse.getFareAmount() : "N/A");

            // Información de la organización (del usuario - más completa)
            if (userResponse.getOrganization() != null) {
                // Si no tenemos el nombre de la organización del endpoint de organización, usar el del usuario
                if (response.getOrganizationName() == null || "N/A".equals(response.getOrganizationName())) {
                    response.setOrganizationName(userResponse.getOrganization().getOrganizationName());
                }
            }

            // Información de la asignación de caja de agua
            if (userResponse.getWaterBoxAssignment() != null) {
                response.setAssignedWaterBoxId(userResponse.getWaterBoxAssignment().getWaterBoxId());
                response.setBoxCode(userResponse.getWaterBoxAssignment().getBoxCode());
            }
        }
        
        return response;
    }}
