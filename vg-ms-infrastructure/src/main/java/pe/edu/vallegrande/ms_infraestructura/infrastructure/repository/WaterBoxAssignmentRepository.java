package pe.edu.vallegrande.ms_infraestructura.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.entity.WaterBoxAssignmentEntity;

import java.util.List;

@Repository
public interface WaterBoxAssignmentRepository extends JpaRepository<WaterBoxAssignmentEntity, Long> {
    List<WaterBoxAssignmentEntity> findByStatus(Status status);
    
    @Query("SELECT wba FROM WaterBoxAssignmentEntity wba WHERE wba.userId = :userId AND wba.status = :status ORDER BY wba.createdAt DESC")
    List<WaterBoxAssignmentEntity> findByUserIdAndStatusOrderByCreatedAtDesc(@Param("userId") String userId, @Param("status") Status status);
}