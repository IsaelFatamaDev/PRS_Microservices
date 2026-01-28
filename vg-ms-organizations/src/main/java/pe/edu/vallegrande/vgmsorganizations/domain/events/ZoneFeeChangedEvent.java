package pe.edu.vallegrande.vgmsorganizations.domain.events;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ZoneFeeChangedEvent implements DomainEvent {

     private final String eventId;
     private final LocalDateTime occurredOn;
     private final String zoneId;
     private final BigDecimal previousFee;
     private final BigDecimal newFee;
     private final String changedBy;
     private final String reason;

     public static ZoneFeeChangedEvent from(String zoneId, BigDecimal previousFee,
               BigDecimal newFee, String changedBy, String reason) {
          return ZoneFeeChangedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .occurredOn(LocalDateTime.now())
                    .zoneId(zoneId)
                    .previousFee(previousFee)
                    .newFee(newFee)
                    .changedBy(changedBy)
                    .reason(reason)
                    .build();
     }

     @Override
     public String getEventType() {
          return "ZoneFeeChanged";
     }
}
