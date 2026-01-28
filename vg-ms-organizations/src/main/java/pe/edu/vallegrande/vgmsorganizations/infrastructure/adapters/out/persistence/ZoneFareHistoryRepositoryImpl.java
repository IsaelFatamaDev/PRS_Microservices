package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.domain.models.ZoneFareHistory;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneFareHistoryRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.mappers.ZoneFareHistoryDomainMapper;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories.ZoneFareHistoryMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ZoneFareHistoryRepositoryImpl implements IZoneFareHistoryRepository {

     private final ZoneFareHistoryMongoRepository mongoRepository;

     @Override
     public Mono<ZoneFareHistory> save(ZoneFareHistory history) {
          ZoneFareHistoryDocument document = ZoneFareHistoryDocument.builder()
                    .id(history.getId())
                    .zoneId(history.getZoneId())
                    .previousFee(history.getPreviousFee())
                    .newFee(history.getNewFee())
                    .effectiveDate(history.getEffectiveDate())
                    .changeDate(history.getChangeDate())
                    .changedBy(history.getChangedBy())
                    .reason(history.getReason())
                    .build();

          return mongoRepository.save(document)
                    .map(saved -> ZoneFareHistory.builder()
                              .id(saved.getId())
                              .zoneId(saved.getZoneId())
                              .previousFee(saved.getPreviousFee())
                              .newFee(saved.getNewFee())
                              .effectiveDate(saved.getEffectiveDate())
                              .changeDate(saved.getChangeDate())
                              .changedBy(saved.getChangedBy())
                              .reason(saved.getReason())
                              .build());
     }

     @Override
     public Flux<ZoneFareHistory> findByZoneId(String zoneId) {
          return mongoRepository.findByZoneId(zoneId)
                    .map(doc -> ZoneFareHistory.builder()
                              .id(doc.getId())
                              .zoneId(doc.getZoneId())
                              .previousFee(doc.getPreviousFee())
                              .newFee(doc.getNewFee())
                              .effectiveDate(doc.getEffectiveDate())
                              .changeDate(doc.getChangeDate())
                              .changedBy(doc.getChangedBy())
                              .reason(doc.getReason())
                              .build());
     }
}
