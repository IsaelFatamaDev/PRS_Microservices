package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class UserDTO {
    private String userId;
    private String userCode;
    private String username;
    private String status;
    private List<String> roles;
    private String organizationId;
    private PersonalInfo personalInfo;
    private Contact contact;
    private Address address;
    private Date createdAt;
    private Date updatedAt;

    @Data
    public static class PersonalInfo {
        private String firstName;
        private String lastName;
        private String documentType;
        private String documentNumber;
        
        public String getFullName() {
            return firstName + " " + lastName;
        }
    }

    @Data
    public static class Contact {
        private String email;
        private String phone;
    }

    @Data
    public static class Address {
        private String street;
        private String district;
        private String province;
        private String department;
        private String country;
        
        public String getFullAddress() {
            return street + ", " + district + ", " + province + ", " + department;
        }
    }

    // Métodos de conveniencia
    public String getFullName() {
        return personalInfo != null ? personalInfo.getFullName() : username;
    }

    public String getContactEmail() {
        return contact != null ? contact.getEmail() : null;
    }

    public String getContactPhone() {
        return contact != null ? contact.getPhone() : null;
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public List<String> getNotificationEmails() {
        if (contact != null && contact.getEmail() != null) {
            return List.of(contact.getEmail());
        }
        return List.of();
    }
}
