package pe.edu.vallegrande.vg_ms_payment.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vg_ms_payment.domain.models.Payment;
import pe.edu.vallegrande.vg_ms_payment.domain.models.PaymentDetail;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.request.PaymentCreateRequest;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.request.PaymentDRequest;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.response.PaymentResponse;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.response.PaymentDResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Domain y DTOs
 */
@Component
public class PaymentDtoMapper {
    
    /**
     * Convierte PaymentCreateRequest a Payment (Domain)
     */
    public Payment requestToDomain(PaymentCreateRequest request) {
        if (request == null) {
            return null;
        }
        
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setOrganizationId(request.getOrganizationId());
        payment.setPaymentCode(request.getPaymentCode());
        payment.setUserId(request.getUserId());
        payment.setWaterBoxId(request.getWaterBoxId());
        payment.setPaymentType(request.getPaymentType());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTotalAmount(request.getTotalAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setPaymentStatus(request.getPaymentStatus());
        payment.setExternalReference(request.getExternalReference());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setNew(true);
        
        return payment;
    }
    
    /**
     * Convierte Payment (Domain) a PaymentResponse
     */
    public PaymentResponse domainToResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setOrganizationId(payment.getOrganizationId());
        response.setPaymentCode(payment.getPaymentCode());
        response.setUserId(payment.getUserId());
        response.setWaterBoxId(payment.getWaterBoxId());
        response.setPaymentType(payment.getPaymentType());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setTotalAmount(payment.getTotalAmount());
        response.setPaymentDate(payment.getPaymentDate());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setExternalReference(payment.getExternalReference());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        
        return response;
    }
    
    /**
     * Convierte PaymentDRequest a PaymentDetail (Domain)
     */
    public PaymentDetail detailRequestToDomain(PaymentDRequest request, String paymentId) {
        if (request == null) {
            return null;
        }
        
        PaymentDetail detail = new PaymentDetail();
        detail.setPaymentDetailId(UUID.randomUUID().toString());
        detail.setPaymentId(paymentId);
        detail.setConcept(request.getConcept());
        detail.setYear(request.getYear());
        detail.setMonth(request.getMonth());
        detail.setAmount(request.getAmount());
        detail.setDescription(request.getDescription());
        detail.setPeriodStart(request.getPeriodStart());
        detail.setPeriodEnd(request.getPeriodEnd());
        detail.setNew(true);
        
        return detail;
    }
    
    /**
     * Convierte PaymentDetail (Domain) a PaymentDResponse
     */
    public PaymentDResponse detailDomainToResponse(PaymentDetail detail) {
        if (detail == null) {
            return null;
        }
        
        PaymentDResponse response = new PaymentDResponse();
        response.setPaymentDetailId(detail.getPaymentDetailId());
        response.setPaymentId(detail.getPaymentId());
        response.setConcept(detail.getConcept());
        response.setYear(detail.getYear());
        response.setMonth(detail.getMonth());
        response.setAmount(detail.getAmount());
        response.setDescription(detail.getDescription());
        response.setPeriodStart(detail.getPeriodStart());
        response.setPeriodEnd(detail.getPeriodEnd());
        
        return response;
    }
    
    /**
     * Convierte lista de PaymentDRequest a lista de PaymentDetail
     */
    public List<PaymentDetail> detailRequestListToDomainList(List<PaymentDRequest> requests, String paymentId) {
        if (requests == null) {
            return null;
        }
        
        return requests.stream()
                .map(request -> detailRequestToDomain(request, paymentId))
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte lista de PaymentDetail a lista de PaymentDResponse
     */
    public List<PaymentDResponse> detailDomainListToResponseList(List<PaymentDetail> details) {
        if (details == null) {
            return null;
        }
        
        return details.stream()
                .map(this::detailDomainToResponse)
                .collect(Collectors.toList());
    }
}