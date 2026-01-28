package pe.edu.vallegrande.vg_ms_payment.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vg_ms_payment.domain.models.PaymentDetail;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.entity.PaymentDetailEntity;

/**
 * Mapper para convertir entre PaymentDetailEntity (PostgreSQL) y PaymentDetail (Domain)
 */
@Component
public class PaymentDetailMapper implements BaseMapper<PaymentDetailEntity, PaymentDetail> {
    
    @Override
    public PaymentDetail entityToDomain(PaymentDetailEntity entity) {
        if (entity == null) {
            return null;
        }
        
        PaymentDetail detail = new PaymentDetail();
        detail.setPaymentDetailId(entity.getPaymentDetailId());
        detail.setPaymentId(entity.getPaymentId());
        detail.setConcept(entity.getConcept());
        detail.setYear(entity.getYear());
        detail.setMonth(entity.getMonth());
        detail.setAmount(entity.getAmount());
        detail.setDescription(entity.getDescription());
        detail.setPeriodStart(entity.getPeriodStart());
        detail.setPeriodEnd(entity.getPeriodEnd());
        detail.setNew(entity.isNew());
        
        return detail;
    }
    
    @Override
    public PaymentDetailEntity domainToEntity(PaymentDetail domain) {
        if (domain == null) {
            return null;
        }
        
        PaymentDetailEntity entity = new PaymentDetailEntity();
        entity.setPaymentDetailId(domain.getPaymentDetailId());
        entity.setPaymentId(domain.getPaymentId());
        entity.setConcept(domain.getConcept());
        entity.setYear(domain.getYear());
        entity.setMonth(domain.getMonth());
        entity.setAmount(domain.getAmount());
        entity.setDescription(domain.getDescription());
        entity.setPeriodStart(domain.getPeriodStart());
        entity.setPeriodEnd(domain.getPeriodEnd());
        entity.setNew(domain.isNew());
        
        return entity;
    }
}