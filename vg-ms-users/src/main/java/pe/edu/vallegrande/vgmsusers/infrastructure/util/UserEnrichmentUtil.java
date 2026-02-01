package pe.edu.vallegrande.vgmsusers.infrastructure.util;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsusers.infrastructure.client.external.OrganizationClient;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserWithLocationResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Component
@lombok.extern.slf4j.Slf4j
public class UserEnrichmentUtil {

        private final OrganizationClient organizationClient;
        private final pe.edu.vallegrande.vgmsusers.infrastructure.client.external.FareClient fareClient;
        private final pe.edu.vallegrande.vgmsusers.infrastructure.client.internal.InfrastructureClient infrastructureClient;

        public UserEnrichmentUtil(OrganizationClient organizationClient,
                        pe.edu.vallegrande.vgmsusers.infrastructure.client.external.FareClient fareClient,
                        pe.edu.vallegrande.vgmsusers.infrastructure.client.internal.InfrastructureClient infrastructureClient) {
                this.organizationClient = organizationClient;
                this.fareClient = fareClient;
                this.infrastructureClient = infrastructureClient;
        }

        public Mono<List<UserWithLocationResponse>> enrichUsersWithLocationInfo(List<UserResponse> users) {
                if (users == null || users.isEmpty()) {
                        return Mono.just(List.of());
                }

                Set<String> uniqueOrgIds = users.stream()
                                .map(UserResponse::getOrganizationId)
                                .filter(id -> id != null && !id.isEmpty())
                                .collect(java.util.stream.Collectors.toSet());

                Set<String> uniqueZoneIds = users.stream()
                                .map(UserResponse::getZoneId)
                                .filter(id -> id != null && !id.isEmpty())
                                .collect(java.util.stream.Collectors.toSet());

                log.info("Enriching {} users. Unique Org IDs: {}, Unique Zone IDs: {}", users.size(),
                                uniqueOrgIds.size(), uniqueZoneIds.size());

                Mono<java.util.Map<String, OrganizationClient.OrganizationResponse>> orgsMono = Flux
                                .fromIterable(uniqueOrgIds)
                                .flatMap(orgId -> organizationClient.getOrganizationById(orgId)
                                                .map(orgResponse -> java.util.Map.entry(orgId, orgResponse))
                                                .onErrorResume(error -> Mono.empty()))
                                .collectMap(java.util.Map.Entry::getKey, java.util.Map.Entry::getValue);

                Mono<java.util.Map<String, String>> faresMono = Flux.fromIterable(uniqueZoneIds)
                                .flatMap(zoneId -> fareClient.getFaresByZoneId(zoneId)
                                                .flatMap(response -> {
                                                        if (response.isSuccess() && response.getData() != null) {
                                                                return Mono.justOrEmpty(response.getData().stream()
                                                                                .filter(fare -> "ACTIVE".equals(
                                                                                                fare.getStatus())
                                                                                                && fare.getFareAmount() != null)
                                                                                .findFirst()
                                                                                .map(fare -> java.util.Map.entry(zoneId,
                                                                                                fare.getFareAmount())));
                                                        }
                                                        log.warn("No active fare found or failure for zone {}", zoneId);
                                                        return Mono.empty();
                                                })
                                                .onErrorResume(error -> {
                                                        log.error("Error processing fare for zone {}: {}", zoneId,
                                                                        error.getMessage());
                                                        return Mono.empty();
                                                }))
                                .collectMap(java.util.Map.Entry::getKey, java.util.Map.Entry::getValue)
                                .doOnSuccess(map -> log.info("Fare map created with {} entries", map.size()));

                Mono<java.util.Map<String, pe.edu.vallegrande.vgmsusers.infrastructure.client.internal.InfrastructureClient.WaterBoxAssignmentResponse>> waterBoxMono = Flux
                                .fromIterable(users)
                                .flatMap(user -> infrastructureClient.getActiveWaterBoxAssignmentByUserId(user.getId())
                                                .map(response -> java.util.Map.entry(user.getId(), response))
                                                .onErrorResume(error -> Mono.empty()))
                                .collectMap(java.util.Map.Entry::getKey, java.util.Map.Entry::getValue);

                return Mono.zip(orgsMono, faresMono, waterBoxMono)
                                .map(tuple -> {
                                        var orgMap = tuple.getT1();
                                        var fareMap = tuple.getT2();
                                        var waterBoxMap = tuple.getT3();
                                        return users.stream().map(
                                                        user -> enrichSingleUser(user, orgMap, fareMap, waterBoxMap))
                                                        .toList();
                                });
        }

        private UserWithLocationResponse enrichSingleUser(UserResponse user,
                        java.util.Map<String, OrganizationClient.OrganizationResponse> orgMap,
                        java.util.Map<String, String> fareMap,
                        java.util.Map<String, pe.edu.vallegrande.vgmsusers.infrastructure.client.internal.InfrastructureClient.WaterBoxAssignmentResponse> waterBoxMap) {

                UserWithLocationResponse.WaterBoxAssignmentInfo waterBoxInfo = null;
                if (waterBoxMap.containsKey(user.getId())) {
                        var wb = waterBoxMap.get(user.getId());
                        waterBoxInfo = UserWithLocationResponse.WaterBoxAssignmentInfo.builder()
                                        .id(wb.getId())
                                        .waterBoxId(wb.getWaterBoxId())
                                        .userId(wb.getUserId())
                                        .startDate(wb.getStartDate())
                                        .endDate(wb.getEndDate())
                                        .monthlyFee(wb.getMonthlyFee())
                                        .status(wb.getStatus())
                                        .createdAt(wb.getCreatedAt())
                                        .transferId(wb.getTransferId())
                                        .boxCode(wb.getBoxCode())
                                        .boxType(wb.getBoxType())
                                        .build();
                }

                OrganizationClient.OrganizationResponse orgResponse = orgMap.get(user.getOrganizationId());

                if (orgResponse == null || !orgResponse.isStatus() || orgResponse.getData() == null) {
                        return createUserWithLocationResponseWithoutOrg(user, waterBoxInfo);
                }

                OrganizationClient.OrganizationData orgData = orgResponse.getData();
                OrganizationClient.Zone zone = null;
                OrganizationClient.Street street = null;

                if (user.getZoneId() != null && orgData.getZones() != null) {
                        zone = orgData.getZones().stream()
                                        .filter(z -> z.getZoneId().equals(user.getZoneId()))
                                        .findFirst()
                                        .orElse(null);

                        if (zone != null && user.getStreetId() != null && zone.getStreets() != null) {
                                street = zone.getStreets().stream()
                                                .filter(s -> s.getStreetId().equals(user.getStreetId()))
                                                .findFirst()
                                                .orElse(null);
                        }
                }

                UserWithLocationResponse.OrganizationInfo orgInfo = UserWithLocationResponse.OrganizationInfo
                                .builder()
                                .organizationId(orgData.getOrganizationId())
                                .organizationCode(orgData.getOrganizationCode())
                                .organizationName(orgData.getOrganizationName())
                                .legalRepresentative(orgData.getLegalRepresentative())
                                .address(orgData.getAddress())
                                .phone(orgData.getPhone())
                                .status(orgData.getStatus())
                                .logo(orgData.getLogo())
                                .build();

                UserWithLocationResponse.ZoneInfo zoneInfo = null;
                if (zone != null) {
                        zoneInfo = UserWithLocationResponse.ZoneInfo.builder()
                                        .zoneId(zone.getZoneId())
                                        .zoneCode(zone.getZoneCode())
                                        .zoneName(zone.getZoneName())
                                        .description(zone.getDescription())
                                        .status(zone.getStatus())
                                        .build();
                }

                UserWithLocationResponse.StreetInfo streetInfo = null;
                if (street != null) {
                        streetInfo = UserWithLocationResponse.StreetInfo.builder()
                                        .streetId(street.getStreetId())
                                        .streetCode(street.getStreetCode())
                                        .streetName(street.getStreetName())
                                        .streetType(street.getStreetType())
                                        .status(street.getStatus())
                                        .build();
                }

                String fareAmount = null;
                if (user.getZoneId() != null) {
                        fareAmount = fareMap.get(user.getZoneId());
                }

                return UserWithLocationResponse.builder()
                                .id(user.getId())
                                .userCode(user.getUserCode())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .documentType(user.getDocumentType())
                                .documentNumber(user.getDocumentNumber())
                                .email(user.getEmail())
                                .phone(user.getPhone())
                                .address(user.getAddress())
                                .roles(user.getRoles())
                                .status(user.getStatus())
                                .createdAt(user.getCreatedAt())
                                .updatedAt(user.getUpdatedAt())
                                .organization(orgInfo)
                                .zone(zoneInfo)
                                .street(streetInfo)
                                .waterBoxAssignment(waterBoxInfo)
                                .fareAmount(fareAmount)
                                .build();
        }

        public UserWithLocationResponse createUserWithLocationResponseWithoutOrg(UserResponse user,
                        UserWithLocationResponse.WaterBoxAssignmentInfo waterBoxInfo) {
                return UserWithLocationResponse.builder()
                                .id(user.getId())
                                .userCode(user.getUserCode())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .documentType(user.getDocumentType())
                                .documentNumber(user.getDocumentNumber())
                                .email(user.getEmail())
                                .phone(user.getPhone())
                                .address(user.getAddress())
                                .roles(user.getRoles())
                                .status(user.getStatus())
                                .createdAt(user.getCreatedAt())
                                .updatedAt(user.getUpdatedAt())
                                .organization(null)
                                .zone(null)
                                .street(null)
                                .waterBoxAssignment(waterBoxInfo)
                                .fareAmount(null)
                                .build();
        }
}
