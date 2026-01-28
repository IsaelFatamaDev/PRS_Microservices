package pe.edu.vallegrande.vg_ms_payment.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vg_ms_payment.domain.models.Payment;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.entity.PaymentEntity;

/**
 * Mapper para convertir entre PaymentEntity (PostgreSQL) y Payment (Domain)
 */
@Component
public class PaymentMapper implements BaseMapper<PaymentEntity, Payment> {
    
    @Override
    public Payment entityToDomain(PaymentEntity entity) {
        if (entity == null) {
            return null;
        }
        
        Payment payment = new Payment();
        payment.setPaymentId(entity.getPaymentId());
        payment.setOrganizationId(entity.getOrganizationId());
        payment.setPaymentCode(entity.getPaymentCode());
        payment.setUserId(entity.getUserId());
        payment.setWaterBoxId(entity.getWaterBoxId());
        payment.setPaymentType(entity.getPaymentType());
        payment.setPaymentMethod(entity.getPaymentMethod());
        payment.setTotalAmount(entity.getTotalAmount());
        payment.setPaymentDate(entity.getPaymentDate());
        payment.setPaymentStatus(entity.getPaymentStatus());
        payment.setExternalReference(entity.getExternalReference());
        payment.setCreatedAt(entity.getCreatedAt());
        payment.setUpdatedAt(entity.getUpdatedAt());
        
        return payment;
    }
    
    @Override
    public PaymentEntity domainToEntity(Payment domain) {
        if (domain == null) {
            return null;
        }
        
        PaymentEntity entity = new PaymentEntity();
        entity.setPaymentId(domain.getPaymentId());
        entity.setOrganizationId(domain.getOrganizationId());
        entity.setPaymentCode(domain.getPaymentCode());
        entity.setUserId(domain.getUserId());
        entity.setWaterBoxId(domain.getWaterBoxId());
        entity.setPaymentType(domain.getPaymentType());
        entity.setPaymentMethod(domain.getPaymentMethod());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setPaymentDate(domain.getPaymentDate());
        entity.setPaymentStatus(domain.getPaymentStatus());
        entity.setExternalReference(domain.getExternalReference());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setNew(domain.isNew());
        
        return entity;
    }
}