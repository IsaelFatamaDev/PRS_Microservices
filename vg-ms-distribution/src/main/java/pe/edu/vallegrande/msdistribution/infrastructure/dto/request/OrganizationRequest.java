package pe.edu.vallegrande.msdistribution.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la gestión de organizaciones (si aplica).
 * 
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRequest {

    /** Código de la organización. */
    private String organizationCode;

    /** Nombre de la organización. */
    private String organizationName;

    /** Representante legal. */
    private String legalRepresentative;

    /** Dirección fiscal o física. */
    private String address;

    /** Teléfono de contacto. */
    private String phone;

    /** URL o base64 del logo. */
    private String logo;
}