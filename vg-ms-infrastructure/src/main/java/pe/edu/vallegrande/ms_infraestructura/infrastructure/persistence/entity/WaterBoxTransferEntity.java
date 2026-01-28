package pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.StringListConverter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "water_box_transfers", indexes = {
    @Index(name = "idx_water_box_id", columnList = "water_box_id"),
    @Index(name = "idx_old_assignment_id", columnList = "old_assignment_id"),
    @Index(name = "idx_new_assignment_id", columnList = "new_assignment_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class WaterBoxTransferEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "water_box_id", nullable = false)
    private Long waterBoxId;

    @Column(name = "old_assignment_id", nullable = false)
    private Long oldAssignmentId;

    @Column(name = "new_assignment_id", nullable = false)
    private Long newAssignmentId;

    @Column(name = "transfer_reason", nullable = false, length = 255)
    private String transferReason;

    @Column(name = "documents", columnDefinition = "text")
    @Convert(converter = StringListConverter.class)
    private List<String> documents;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
