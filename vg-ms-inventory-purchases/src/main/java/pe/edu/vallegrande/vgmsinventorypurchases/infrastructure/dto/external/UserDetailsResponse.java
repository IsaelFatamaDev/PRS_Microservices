package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.external;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponse {
    private Boolean success;
    private String message;
    private UserData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserData {
        private String id;
        private String userCode;
        private String firstName;
        private String lastName;
        private String documentType;
        private String documentNumber;
        private String email;
        private String phone;
        private String address;
        private List<String> roles;
        private String status;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private OrganizationInfo organization;
        private ZoneInfo zone;
        private StreetInfo street;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizationInfo {
        private String organizationId;
        private String organizationCode;
        private String organizationName;
        private String address;
        private String phone;
        private String legalRepresentative;
        private String status;
        private String logo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZoneInfo {
        private String zoneId;
        private String zoneCode;
        private String zoneName;
        private String description;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreetInfo {
        private String streetId;
        private String streetCode;
        private String streetName;
        private String streetType;
        private String status;
    }
}
