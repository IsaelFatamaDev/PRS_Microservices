package pe.edu.vallegrande.ms_infraestructura.domain.models;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterBoxTransfer {
    private Long id;
    private Long waterBoxId;
    private Long oldAssignmentId;
    private Long newAssignmentId;
    private String transferReason;
    private List<String> documents;
    private LocalDateTime createdAt;
}