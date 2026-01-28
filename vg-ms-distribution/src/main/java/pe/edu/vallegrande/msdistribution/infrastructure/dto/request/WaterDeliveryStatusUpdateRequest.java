package pe.edu.vallegrande.msdistribution.infrastructure.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * DTO inmutable para actualizar el estado de entrega de agua de un programa.
 * Utilizado para confirmar si hubo o no suministro.
 * 
 * @version 1.0
 */
@Value
@Builder
@Jacksonized
public class WaterDeliveryStatusUpdateRequest {

    /**
     * Nuevo estado de entrega.
     * Valores permitidos: CON_AGUA, SIN_AGUA.
     */
    @NotBlank(message = "El estado de entrega de agua es requerido")
    @Pattern(regexp = "^(CON_AGUA|SIN_AGUA)$", message = "Estado inválido. Solo se permite: CON_AGUA o SIN_AGUA")
    String waterDeliveryStatus;

    /** Observaciones adicionales (opcional). */
    String observations;

    /** Identificador del usuario que confirma la entrega. */
    String confirmedBy;
}
