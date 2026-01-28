package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.mappers;

import pe.edu.vallegrande.vgmsorganizations.domain.models.ZoneFareHistory;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.entities.ZoneFareHistoryDocument;

/**
 * Mapper entre Domain Model y MongoDB Document
 * Responsabilidad: Conversión Domain ↔ Document (Persistence)
 */
public class ZoneFareHistoryDomainMapper {

     /**
      * Domain → Document (para guardar en MongoDB)
      */
     public static ZoneFareHistoryDocument toDocument(ZoneFareHistory history) {
          if (history == null)
               return null;

          return ZoneFareHistoryDocument.builder()
                    .id(history.getId())
                    .zoneId(history.getZoneId())
                    .previousFee(history.getPreviousFee())
                    .newFee(history.getNewFee())
                    .effectiveDate(history.getEffectiveDate())
                    .changeDate(history.getChangeDate())
                    .changedBy(history.getChangedBy())
                    .reason(history.getReason())
                    .build();
     }

     /**
      * Document → Domain (al recuperar de MongoDB)
      */
     public static ZoneFareHistory toDomain(ZoneFareHistoryDocument document) {
          if (document == null)
               return null;

          return ZoneFareHistory.builder()
                    .id(document.getId())
                    .zoneId(document.getZoneId())
                    .previousFee(document.getPreviousFee())
                    .newFee(document.getNewFee())
                    .effectiveDate(document.getEffectiveDate())
                    .changeDate(document.getChangeDate())
                    .changedBy(document.getChangedBy())
                    .reason(document.getReason())
                    .build();
     }
}
