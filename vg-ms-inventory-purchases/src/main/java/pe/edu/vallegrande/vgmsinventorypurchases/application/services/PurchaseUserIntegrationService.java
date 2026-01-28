package pe.edu.vallegrande.vgmsinventorypurchases.application.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.PurchaseStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.client.internal.UsersServiceClient;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.external.UserDetailsResponse;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.PurchaseResponse;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.PurchaseWithUserDetailsResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PurchaseUserIntegrationService {

    private final PurchaseService purchaseService;
    private final UsersServiceClient usersServiceClient;

    public PurchaseUserIntegrationService(PurchaseService purchaseService, UsersServiceClient usersServiceClient) {
        this.purchaseService = purchaseService;
        this.usersServiceClient = usersServiceClient;
    }

    /**
     * Obtiene todas las compras enriquecidas con información de usuario y organización
     */
    public Flux<PurchaseWithUserDetailsResponse> findAllWithUserDetails() {
        return purchaseService.findAll()
                .flatMap(this::enrichPurchaseWithUserDetails);
    }

    /**
     * Obtiene compras por organización con información enriquecida
     * Además valida que el usuario responsable pertenezca efectivamente a la organización
     */
    public Flux<PurchaseWithUserDetailsResponse> findByOrganizationIdWithUserDetails(String organizationId) {
        return purchaseService.findByOrganizationId(organizationId)
                .flatMap(purchase -> enrichPurchaseWithUserDetails(purchase)
                        .filter(enrichedPurchase -> {
                            // Validar que la organización del usuario coincida con la de la compra
                            if (enrichedPurchase.getOrganizationInfo() != null &&
                                enrichedPurchase.getOrganizationInfo().getOrganizationId() != null) {
                                boolean matches = organizationId.equals(enrichedPurchase.getOrganizationInfo().getOrganizationId());
                                if (!matches) {
                                    log.warn("Compra {} filtrada: organización del usuario ({}) no coincide con la solicitada ({})",
                                            purchase.getPurchaseId(),
                                            enrichedPurchase.getOrganizationInfo().getOrganizationId(),
                                            organizationId);
                                }
                                return matches;
                            }
                            return true; // Mantener si no se puede validar
                        }));
    }

    /**
     * Obtiene compras por usuario específico con validación de organización
     */
    public Flux<PurchaseWithUserDetailsResponse> findByUserIdWithValidation(String userId, String organizationId) {
        return usersServiceClient.validateUserOrganization(userId, organizationId)
                .flatMapMany(isValid -> {
                    if (!isValid) {
                        log.warn("Usuario {} no pertenece a la organización {}", userId, organizationId);
                        return Flux.empty();
                    }

                    return purchaseService.findByOrganizationId(organizationId)
                            .filter(purchase -> userId.equals(purchase.getRequestedByUserId()))
                            .flatMap(this::enrichPurchaseWithUserDetails);
                });
    }

    /**
     * Obtiene compras por estado con información enriquecida
     */
    public Flux<PurchaseWithUserDetailsResponse> findByStatusWithUserDetails(String organizationId, PurchaseStatus status) {
        return purchaseService.findByStatus(organizationId, status)
                .flatMap(this::enrichPurchaseWithUserDetails);
    }

    /**
     * Obtiene una compra específica enriquecida con información del usuario
     */
    public Mono<PurchaseWithUserDetailsResponse> findByIdWithUserDetails(String purchaseId) {
        return purchaseService.findById(purchaseId)
                .flatMap(this::enrichPurchaseWithUserDetails);
    }

    /**
     * Valida que un usuario pueda realizar operaciones en una organización específica
     */
    public Mono<Boolean> validateUserAccess(String userId, String organizationId) {
        return usersServiceClient.validateUserOrganization(userId, organizationId);
    }

    /**
     * Método privado que enriquece una respuesta de compra con información del usuario
     */
    private Mono<PurchaseWithUserDetailsResponse> enrichPurchaseWithUserDetails(PurchaseResponse purchase) {
        if (purchase.getRequestedByUserId() == null || purchase.getRequestedByUserId().trim().isEmpty()) {
            // Si no hay usuario asociado, retornar sin enriquecer
            return Mono.just(mapToEnrichedResponse(purchase, null));
        }

        return usersServiceClient.getUserDetails(purchase.getRequestedByUserId())
                .map(userDetails -> mapToEnrichedResponse(purchase, userDetails))
                .onErrorResume(error -> {
                    log.warn("Error al obtener detalles del usuario {}: {}",
                            purchase.getRequestedByUserId(), error.getMessage());
                    // En caso de error, retornar respuesta sin enriquecer
                    return Mono.just(mapToEnrichedResponse(purchase, null));
                });
    }

    /**
     * Mapea una PurchaseResponse y UserDetailsResponse a PurchaseWithUserDetailsResponse
     */
    private PurchaseWithUserDetailsResponse mapToEnrichedResponse(PurchaseResponse purchase, UserDetailsResponse userDetails) {
        PurchaseWithUserDetailsResponse.PurchaseWithUserDetailsResponseBuilder builder =
                PurchaseWithUserDetailsResponse.builder()
                        .purchaseId(purchase.getPurchaseId())
                        .organizationId(purchase.getOrganizationId())
                        .purchaseCode(purchase.getPurchaseCode())
                        .supplierId(purchase.getSupplierId())
                        .supplierName(purchase.getSupplierName())
                        .supplierCode(purchase.getSupplierCode())
                        .purchaseDate(purchase.getPurchaseDate())
                        .deliveryDate(purchase.getDeliveryDate())
                        .totalAmount(purchase.getTotalAmount())
                        .status(purchase.getStatus())
                        .requestedByUserId(purchase.getRequestedByUserId())
                        .approvedByUserId(purchase.getApprovedByUserId())
                        .invoiceNumber(purchase.getInvoiceNumber())
                        .observations(purchase.getObservations())
                        .createdAt(purchase.getCreatedAt())
                        .details(purchase.getDetails());

        // Agregar información del usuario si está disponible
        if (userDetails != null && userDetails.getSuccess() && userDetails.getData() != null) {
            UserDetailsResponse.UserData userData = userDetails.getData();

            // Mapear información del usuario
            PurchaseWithUserDetailsResponse.UserInfo userInfo = PurchaseWithUserDetailsResponse.UserInfo.builder()
                    .id(userData.getId())
                    .userCode(userData.getUserCode())
                    .firstName(userData.getFirstName())
                    .lastName(userData.getLastName())
                    .fullName((userData.getFirstName() != null ? userData.getFirstName() + " " : "") +
                             (userData.getLastName() != null ? userData.getLastName() : ""))
                    .documentType(userData.getDocumentType())
                    .documentNumber(userData.getDocumentNumber())
                    .email(userData.getEmail())
                    .phone(userData.getPhone())
                    .address(userData.getAddress())
                    .roles(userData.getRoles())
                    .status(userData.getStatus())
                    .build();

            builder.requestedByUser(userInfo);

            // Mapear información de la organización si está disponible
            if (userData.getOrganization() != null) {
                PurchaseWithUserDetailsResponse.OrganizationInfo orgInfo =
                        PurchaseWithUserDetailsResponse.OrganizationInfo.builder()
                                .organizationId(userData.getOrganization().getOrganizationId())
                                .organizationCode(userData.getOrganization().getOrganizationCode())
                                .organizationName(userData.getOrganization().getOrganizationName())
                                .address(userData.getOrganization().getAddress())
                                .phone(userData.getOrganization().getPhone())
                                .legalRepresentative(userData.getOrganization().getLegalRepresentative())
                                .status(userData.getOrganization().getStatus())
                                .build();

                builder.organizationInfo(orgInfo);
            }
        }

        return builder.build();
    }
}
