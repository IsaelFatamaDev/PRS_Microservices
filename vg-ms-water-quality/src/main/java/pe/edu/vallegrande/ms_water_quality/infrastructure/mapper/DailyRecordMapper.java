package pe.edu.vallegrande.ms_water_quality.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.ms_water_quality.domain.models.DailyRecord;
import pe.edu.vallegrande.ms_water_quality.infrastructure.document.DailyRecordDocument;

/**
 * Mapper: DailyRecord Domain ↔ Document
 * Convierte entre el modelo de dominio y el documento de persistencia.
 */
@Component
public class DailyRecordMapper {

    /**
     * Convierte de Document a Domain Model
     */
    public DailyRecord toDomain(DailyRecordDocument document) {
        if (document == null) {
            return null;
        }

        return DailyRecord.builder()
                .id(document.getId())
                .organizationId(document.getOrganizationId())
                .recordCode(document.getRecordCode())
                .testingPointIds(document.getTestingPointIds())
                .recordDate(document.getRecordDate())
                .level(document.getLevel())
                .acceptable(document.isAcceptable())
                .actionRequired(document.isActionRequired())
                .recordedByUserId(document.getRecordedByUserId())
                .observations(document.getObservations())
                .amount(document.getAmount())
                .recordType(document.getRecordType())
                .createdAt(document.getCreatedAt())
                .deletedAt(document.getDeletedAt())
                .build();
    }

    /**
     * Convierte de Domain Model a Document
     */
    public DailyRecordDocument toDocument(DailyRecord domain) {
        if (domain == null) {
            return null;
        }

        return DailyRecordDocument.builder()
                .id(domain.getId())
                .organizationId(domain.getOrganizationId())
                .recordCode(domain.getRecordCode())
                .testingPointIds(domain.getTestingPointIds())
                .recordDate(domain.getRecordDate())
                .level(domain.getLevel())
                .acceptable(domain.isAcceptable())
                .actionRequired(domain.isActionRequired())
                .recordedByUserId(domain.getRecordedByUserId())
                .observations(domain.getObservations())
                .amount(domain.getAmount())
                .recordType(domain.getRecordType())
                .createdAt(domain.getCreatedAt())
                .deletedAt(domain.getDeletedAt())
                .build();
    }
}
