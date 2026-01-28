package pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.BoxType;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "water_boxes", indexes = {
    @Index(name = "idx_box_code", columnList = "box_code"),
    @Index(name = "idx_organization_id", columnList = "organization_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class WaterBoxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private String organizationId;

    @Column(name = "box_code", nullable = false, unique = true, length = 50)
    private String boxCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "box_type", nullable = false, length = 20)
    private BoxType boxType;

    @Column(name = "installation_date", nullable = false)
    private LocalDate installationDate;

    @Column(name = "current_assignment_id")
    private Long currentAssignmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private Status status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
