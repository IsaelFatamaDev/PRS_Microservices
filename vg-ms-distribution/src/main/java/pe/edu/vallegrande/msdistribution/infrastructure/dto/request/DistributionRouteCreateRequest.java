package pe.edu.vallegrande.msdistribution.infrastructure.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import java.util.List;

/**
 * DTO para crear una nueva ruta de distribución.
 * Incluye la lista ordenada de zonas.
 * 
 * @version 1.0
 */
@Value
@Builder
@Jacksonized
public class DistributionRouteCreateRequest {

    /** ID de la organización. */
    @NotBlank(message = "El ID de organización es obligatorio")
    String organizationId;

    /** Código de ruta (opcional, autogenerado si es nulo). */
    String routeCode;

    /** Nombre descriptivo de la ruta. */
    @NotBlank(message = "El nombre de ruta es obligatorio")
    String routeName;

    /** Lista de zonas que componen la ruta. */
    @NotEmpty(message = "Debe incluir al menos una zona")
    @Valid
    List<ZoneEntry> zones;

    /** Duración total estimada en horas. */
    @NotNull(message = "La duración estimada total es obligatoria")
    Integer totalEstimatedDuration;

    /** Usuario responsable. */
    @NotBlank(message = "El ID del usuario responsable es obligatorio")
    String responsibleUserId;

    /**
     * DTO interno para representar una zona dentro de la ruta.
     */
    @Value
    @Builder
    @Jacksonized
    public static class ZoneEntry {

        /** ID de la zona. */
        @NotBlank(message = "El ID de zona es obligatorio")
        String zoneId;

        /** Orden en la ruta. */
        @NotNull(message = "El orden es obligatorio")
        Integer order;

        /** Duración estimada en esta zona (horas). */
        @NotNull(message = "La duración estimada es obligatoria")
        Integer estimatedDuration;
    }
}