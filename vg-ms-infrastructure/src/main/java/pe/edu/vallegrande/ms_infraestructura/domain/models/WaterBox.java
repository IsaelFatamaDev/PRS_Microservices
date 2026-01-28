package pe.edu.vallegrande.ms_infraestructura.domain.models;

import lombok.*;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.BoxType;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterBox {
    private Long id;
    private String organizationId;
    private String boxCode;
    private BoxType boxType;
    private LocalDate installationDate;
    private Long currentAssignmentId;
    private Status status;
    private LocalDateTime createdAt;
}