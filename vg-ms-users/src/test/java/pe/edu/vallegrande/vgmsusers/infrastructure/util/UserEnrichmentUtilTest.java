package pe.edu.vallegrande.vgmsusers.infrastructure.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.vallegrande.vgmsusers.infrastructure.client.external.FareClient;
import pe.edu.vallegrande.vgmsusers.infrastructure.client.external.OrganizationClient;
import pe.edu.vallegrande.vgmsusers.infrastructure.client.internal.InfrastructureClient;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserWithLocationResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserEnrichmentUtilTest {

    @Mock
    private OrganizationClient organizationClient;

    @Mock
    private FareClient fareClient;

    @Mock
    private InfrastructureClient infrastructureClient;

    private UserEnrichmentUtil userEnrichmentUtil;

    @BeforeEach
    void setUp() {
        userEnrichmentUtil = new UserEnrichmentUtil(organizationClient, fareClient, infrastructureClient);
    }

    @Test
    void enrichUsersWithLocationInfo_ShouldReturnFareAmount_WhenFareExistsAndIsActive() {
        // Arrange
        String zoneId = "zone1";
        String fareAmount = "10.00";
        UserResponse user = new UserResponse();
        user.setId("user1");
        user.setOrganizationId("org1");
        user.setZoneId(zoneId);

        OrganizationClient.OrganizationResponse orgResponse = new OrganizationClient.OrganizationResponse();
        orgResponse.setStatus(true);
        OrganizationClient.OrganizationData orgData = new OrganizationClient.OrganizationData();
        orgData.setOrganizationId("org1");
        orgResponse.setData(orgData);

        FareClient.FareData fareData = new FareClient.FareData();
        fareData.setStatus("ACTIVE");
        fareData.setFareAmount(fareAmount);
        FareClient.FareResponse fareResponse = new FareClient.FareResponse(true, "Success", List.of(fareData));

        when(organizationClient.getOrganizationById(anyString())).thenReturn(Mono.just(orgResponse));
        when(fareClient.getFaresByZoneId(zoneId)).thenReturn(Mono.just(fareResponse));
        when(infrastructureClient.getActiveWaterBoxAssignmentByUserId(anyString())).thenReturn(Mono.empty());

        // Act
        Mono<List<UserWithLocationResponse>> result = userEnrichmentUtil.enrichUsersWithLocationInfo(List.of(user));

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(users -> {
                    UserWithLocationResponse enrichedUser = users.get(0);
                    return fareAmount.equals(enrichedUser.getFareAmount());
                })
                .verifyComplete();
    }

    @Test
    void enrichUsersWithLocationInfo_ShouldHandleNullFareAmount_WhenFareExistsButAmountIsNull() {
        // Arrange
        String zoneId = "zone1";
        UserResponse user = new UserResponse();
        user.setId("user1");
        user.setOrganizationId("org1");
        user.setZoneId(zoneId);

        OrganizationClient.OrganizationResponse orgResponse = new OrganizationClient.OrganizationResponse();
        orgResponse.setStatus(true);
        OrganizationClient.OrganizationData orgData = new OrganizationClient.OrganizationData();
        orgData.setOrganizationId("org1");
        orgResponse.setData(orgData);

        FareClient.FareData fareData = new FareClient.FareData();
        fareData.setStatus("ACTIVE");
        fareData.setFareAmount(null); // Simulate null amount
        FareClient.FareResponse fareResponse = new FareClient.FareResponse(true, "Success", List.of(fareData));

        when(organizationClient.getOrganizationById(anyString())).thenReturn(Mono.just(orgResponse));
        when(fareClient.getFaresByZoneId(zoneId)).thenReturn(Mono.just(fareResponse));
        when(infrastructureClient.getActiveWaterBoxAssignmentByUserId(anyString())).thenReturn(Mono.empty());

        // Act
        Mono<List<UserWithLocationResponse>> result = userEnrichmentUtil.enrichUsersWithLocationInfo(List.of(user));

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(users -> {
                    UserWithLocationResponse enrichedUser = users.get(0);
                    return enrichedUser.getFareAmount() == null;
                })
                .verifyComplete();
    }

    @Test
    void enrichUsersWithLocationInfo_ShouldSkipFareWithNullAmount_AndPickValidOne() {
        // Arrange
        String zoneId = "zone1";
        String validFareAmount = "15.00";
        UserResponse user = new UserResponse();
        user.setId("user1");
        user.setOrganizationId("org1");
        user.setZoneId(zoneId);

        OrganizationClient.OrganizationResponse orgResponse = new OrganizationClient.OrganizationResponse();
        orgResponse.setStatus(true);
        OrganizationClient.OrganizationData orgData = new OrganizationClient.OrganizationData();
        orgData.setOrganizationId("org1");
        orgResponse.setData(orgData);

        // Bad fare (null amount)
        FareClient.FareData badFare = new FareClient.FareData();
        badFare.setStatus("ACTIVE");
        badFare.setFareAmount(null);

        // Good fare
        FareClient.FareData goodFare = new FareClient.FareData();
        goodFare.setStatus("ACTIVE");
        goodFare.setFareAmount(validFareAmount);

        // List with bad fare FIRST
        FareClient.FareResponse fareResponse = new FareClient.FareResponse(true, "Success", List.of(badFare, goodFare));

        when(organizationClient.getOrganizationById(anyString())).thenReturn(Mono.just(orgResponse));
        when(fareClient.getFaresByZoneId(zoneId)).thenReturn(Mono.just(fareResponse));
        when(infrastructureClient.getActiveWaterBoxAssignmentByUserId(anyString())).thenReturn(Mono.empty());

        // Act
        Mono<List<UserWithLocationResponse>> result = userEnrichmentUtil.enrichUsersWithLocationInfo(List.of(user));

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(users -> {
                    UserWithLocationResponse enrichedUser = users.get(0);
                    // Currently, this will fail (return null) because it picks the first one and
                    // crashes
                    // We want it to eventually return validFareAmount
                    return validFareAmount.equals(enrichedUser.getFareAmount());
                })
                .verifyComplete();
    }
}
