package pe.edu.vallegrande.ms_infraestructura.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.entity.WaterBoxEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaterBoxRepository extends JpaRepository<WaterBoxEntity, Long> {
    List<WaterBoxEntity> findByStatus(Status status);
    Optional<WaterBoxEntity> findByCurrentAssignmentId(Long currentAssignmentId);
}