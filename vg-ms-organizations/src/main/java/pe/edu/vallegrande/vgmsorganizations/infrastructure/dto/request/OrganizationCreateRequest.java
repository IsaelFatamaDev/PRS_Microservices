package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrganizationCreateRequest {
    private String name;
    private String address;
    private String phone;
    private String legalRepresentative;
    private LocalDateTime creationDate;
    private Boolean status;
    private LocalDateTime registrationDate;
}
