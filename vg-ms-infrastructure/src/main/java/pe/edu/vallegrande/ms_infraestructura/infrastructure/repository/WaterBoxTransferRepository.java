package pe.edu.vallegrande.ms_infraestructura.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.entity.WaterBoxTransferEntity;

@Repository
public interface WaterBoxTransferRepository extends JpaRepository<WaterBoxTransferEntity, Long> {
}