package pe.edu.vallegrande.msdistribution.infrastructure.client.external.dto;

import lombok.Data;

@Data
public class ExternalStreet {
    private String status;
    private String streetCode;
    private String streetId;
    private String streetType;
    private String streetName;
}
