package pe.edu.vallegrande.msdistribution.infrastructure.client.external.dto;

import lombok.Data;

/**
 * DTO que representa una organización externa.
 * Mapea la estructura del servicio de organizaciones.
 * 
 * @version 1.0
 */
@Data
public class ExternalOrganization {

    /** Nombre de la organización. */
    private String organizationName;

    /** Código de la organización. */
    private String organizationCode;

    /** ID único de la organización. */
    private String organizationId;

    /** Estado activo/inactivo. */
    private String status;

    /** Dirección física. */
    private String address;

    /** Teléfono de contacto. */
    private String phone;

    /** Representante legal. */
    private String legalRepresentative;
}
