package pe.edu.vallegrande.msdistribution.domain.models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad de dominio que representa un Horario de Distribución.
 * Define cuándo se realiza la distribución en ciertas zonas/calles.
 * 
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "schedule")
public class DistributionSchedule {

    /** Identificador único del horario. */
    @Id
    private String id;

    /** Organización propietaria. */
    private String organizationId;

    /** Código del horario (ej. SCH001). */
    private String scheduleCode;

    /** ID de la zona asociada. */
    private String zoneId;

    /** ID de la calle asociada. */
    private String streetId;

    /** Nombre descriptivo del horario. */
    private String scheduleName;

    /** Días de la semana aplicables (ej. ["MONDAY", "WEDNESDAY"]). */
    @Builder.Default
    private List<String> daysOfWeek = new ArrayList<>();

    /** Hora de inicio (formato HH:mm). */
    private String startTime;

    /** Hora de fin (formato HH:mm). */
    private String endTime;

    /** Duración calculada en horas. */
    private Integer durationHours;

    /** Estado del horario. */
    private String status;

    /** Fecha de creación. */
    private Instant createdAt;
}