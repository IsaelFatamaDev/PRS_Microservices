package pe.edu.vallegrande.vgmsauthentication.infrastructure.client.external.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO para usuario completo obtenido desde MS-users
 */
@Data
public class UserCompleteDto {
    private String userId;
    private String userCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String dni;
    private String address;
    private Boolean active;

    private OrganizationInfo organization;
    private ZoneInfo zone;
    private StreetInfo street;
    private List<RoleInfo> roles;

    @Data
    public static class OrganizationInfo {
        private String organizationId;
        private String name;
        private String type;
    }

    @Data
    public static class ZoneInfo {
        private String zoneId;
        private String name;
        private String description;
    }

    @Data
    public static class StreetInfo {
        private String streetId;
        private String name;
        private String reference;
    }

    @Data
    public static class RoleInfo {
        private String roleId;
        private String name;
        private String description;
        private Boolean active;
    }
}