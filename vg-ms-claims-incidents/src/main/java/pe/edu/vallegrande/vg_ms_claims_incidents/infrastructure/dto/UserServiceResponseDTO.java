package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

/**
 * DTO para mapear la respuesta del MS-USUARIOS
 */
@Data
public class UserServiceResponseDTO {
    
    @JsonProperty("success")
    private Boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private UserServiceDataDTO data;
    
    @Data
    public static class UserServiceDataDTO {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("userCode")
        private String userCode;
        
        @JsonProperty("firstName")
        private String firstName;
        
        @JsonProperty("lastName")
        private String lastName;
        
        @JsonProperty("documentType")
        private String documentType;
        
        @JsonProperty("documentNumber")
        private String documentNumber;
        
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("phone")
        private String phone;
        
        @JsonProperty("address")
        private String address;
        
        @JsonProperty("roles")
        private List<String> roles;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("createdAt")
        private LocalDateTime createdAt;
        
        @JsonProperty("updatedAt")
        private LocalDateTime updatedAt;
        
        @JsonProperty("organization")
        private OrganizationDTO organization;
        
        @JsonProperty("zone")
        private ZoneDTO zone;
        
        @JsonProperty("street")
        private StreetDTO street;
    }
    
    @Data
    public static class OrganizationDTO {
        @JsonProperty("organizationId")
        private String organizationId;
        
        @JsonProperty("organizationCode")
        private String organizationCode;
        
        @JsonProperty("organizationName")
        private String organizationName;
        
        @JsonProperty("legalRepresentative")
        private String legalRepresentative;
        
        @JsonProperty("phone")
        private String phone;
        
        @JsonProperty("address")
        private String address;
        
        @JsonProperty("status")
        private String status;
    }
    
    @Data
    public static class ZoneDTO {
        @JsonProperty("zoneId")
        private String zoneId;
        
        @JsonProperty("zoneCode")
        private String zoneCode;
        
        @JsonProperty("zoneName")
        private String zoneName;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("status")
        private String status;
    }
    
    @Data
    public static class StreetDTO {
        @JsonProperty("streetId")
        private String streetId;
        
        @JsonProperty("streetCode")
        private String streetCode;
        
        @JsonProperty("streetName")
        private String streetName;
        
        @JsonProperty("streetType")
        private String streetType;
        
        @JsonProperty("status")
        private String status;
    }
    
    /**
     * Convierte la respuesta del MS-USUARIOS a nuestro UserDTO
     */
    public UserDTO toUserDTO() {
        if (!success || data == null) {
            return null;
        }
        
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(data.getId());
        userDTO.setUserCode(data.getUserCode());
        userDTO.setUsername(data.getUserCode());
        userDTO.setStatus(data.getStatus());
        userDTO.setRoles(data.getRoles());
        
        // Personal Info
        UserDTO.PersonalInfo personalInfo = new UserDTO.PersonalInfo();
        personalInfo.setFirstName(data.getFirstName());
        personalInfo.setLastName(data.getLastName());
        personalInfo.setDocumentType(data.getDocumentType());
        personalInfo.setDocumentNumber(data.getDocumentNumber());
        userDTO.setPersonalInfo(personalInfo);
        
        // Contact
        UserDTO.Contact contact = new UserDTO.Contact();
        contact.setEmail(data.getEmail());
        contact.setPhone(data.getPhone());
        userDTO.setContact(contact);
        
        // Address
        UserDTO.Address address = new UserDTO.Address();
        address.setStreet(data.getAddress());
        if (data.getZone() != null) {
            address.setDistrict(data.getZone().getZoneName());
        }
        if (data.getOrganization() != null) {
            address.setProvince(data.getOrganization().getOrganizationName());
        }
        address.setDepartment("Lima"); // Valor por defecto
        address.setCountry("Perú");
        userDTO.setAddress(address);
        
        // Organization ID
        if (data.getOrganization() != null) {
            userDTO.setOrganizationId(data.getOrganization().getOrganizationId());
        }
        
        // Timestamps
        if (data.getCreatedAt() != null) {
            userDTO.setCreatedAt(Date.from(data.getCreatedAt().toInstant(ZoneOffset.UTC)));
        }
        if (data.getUpdatedAt() != null) {
            userDTO.setUpdatedAt(Date.from(data.getUpdatedAt().toInstant(ZoneOffset.UTC)));
        }
        
        return userDTO;
    }
}
