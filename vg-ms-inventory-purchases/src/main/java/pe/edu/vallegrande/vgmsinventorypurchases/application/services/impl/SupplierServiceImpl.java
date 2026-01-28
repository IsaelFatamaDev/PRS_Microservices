package pe.edu.vallegrande.vgmsinventorypurchases.application.services.impl;

import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsinventorypurchases.application.services.SupplierService;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.SupplierStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.Supplier;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.SupplierRequest;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.SupplierResponse;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.exception.CustomException;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.mapper.SupplierMapper;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository.SupplierRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class SupplierServiceImpl implements SupplierService {

     private final SupplierRepository supplierRepository;
     private final SupplierMapper supplierMapper;

     public SupplierServiceImpl(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
          this.supplierRepository = supplierRepository;
          this.supplierMapper = supplierMapper;
     }

     @Override
     public Flux<SupplierResponse> findAll(String organizationId) {
          return supplierRepository.findByOrganizationId(organizationId)
                    .map(supplierMapper::toDomain)
                    .map(this::mapToResponse);
     }

     @Override
     public Mono<SupplierResponse> findById(String id) {
          return supplierRepository.findById(id)
                    .map(supplierMapper::toDomain)
                    .map(this::mapToResponse)
                    .switchIfEmpty(
                              Mono.error(new CustomException("Proveedor no encontrado", "SUPPLIER_NOT_FOUND", 404)));
     }

     @Override
     public Mono<SupplierResponse> create(SupplierRequest request) {
          return supplierRepository
                    .existsBySupplierCodeAndOrganizationId(request.getSupplierCode(), request.getOrganizationId())
                    .flatMap(exists -> {
                         if (Boolean.TRUE.equals(exists)) {
                              return Mono.error(new CustomException("Ya existe un proveedor con este código",
                                        "SUPPLIER_CODE_EXISTS", 400));
                         }

                         Supplier supplier = mapToDomain(request);

                         supplier.setCreatedAt(LocalDateTime.now());

                         if (supplier.getStatus() == null) {
                              supplier.setStatus(SupplierStatus.ACTIVO);
                         }

                         return supplierRepository.save(supplierMapper.toEntity(supplier))
                                   .map(supplierMapper::toDomain)
                                   .map(this::mapToResponse);
                    });
     }

     @Override
     public Mono<SupplierResponse> update(String id, SupplierRequest request) {
          return supplierRepository.findById(id)
                    .switchIfEmpty(
                              Mono.error(new CustomException("Proveedor no encontrado", "SUPPLIER_NOT_FOUND", 404)))
                    .map(supplierMapper::toDomain)
                    .flatMap(existingSupplier -> {
                         if (!existingSupplier.getSupplierCode().equals(request.getSupplierCode())) {
                              return supplierRepository
                                        .existsBySupplierCodeAndOrganizationId(request.getSupplierCode(),
                                                  request.getOrganizationId())
                                        .flatMap(exists -> {
                                             if (Boolean.TRUE.equals(exists)) {
                                                  return Mono.error(new CustomException(
                                                            "Ya existe un proveedor con este código",
                                                            "SUPPLIER_CODE_EXISTS", 400));
                                             }
                                             return updateSupplier(existingSupplier, request);
                                        });
                         }
                         return updateSupplier(existingSupplier, request);
                    });
     }

     private Mono<SupplierResponse> updateSupplier(Supplier existingSupplier, SupplierRequest request) {
          existingSupplier.setSupplierCode(request.getSupplierCode());
          existingSupplier.setSupplierName(request.getSupplierName());
          existingSupplier.setContactPerson(request.getContactPerson());
          existingSupplier.setPhone(request.getPhone());
          existingSupplier.setEmail(request.getEmail());
          existingSupplier.setAddress(request.getAddress());

          if (request.getStatus() != null) {
               existingSupplier.setStatus(request.getStatus());
          }

          return supplierRepository.save(supplierMapper.toEntity(existingSupplier))
                    .map(supplierMapper::toDomain)
                    .map(this::mapToResponse);
     }

     @Override
     public Mono<Void> delete(String id) {
          return supplierRepository.findById(id)
                    .switchIfEmpty(
                              Mono.error(new CustomException("Proveedor no encontrado", "SUPPLIER_NOT_FOUND", 404)))
                    .map(supplierMapper::toDomain)
                    .flatMap(supplier -> {
                         supplier.setStatus(SupplierStatus.INACTIVO);
                         return supplierRepository.save(supplierMapper.toEntity(supplier));
                    })
                    .then();
     }

     @Override
     public Flux<SupplierResponse> findAllByStatus(String organizationId, SupplierStatus status) {
          return supplierRepository.findByOrganizationIdAndStatus(organizationId, status)
                    .map(supplierMapper::toDomain)
                    .map(this::mapToResponse);
     }

     @Override
     public Mono<SupplierResponse> restore(String id) {
          return supplierRepository.findById(id)
                    .switchIfEmpty(
                              Mono.error(new CustomException("Proveedor no encontrado", "SUPPLIER_NOT_FOUND", 404)))
                    .map(supplierMapper::toDomain)
                    .flatMap(supplier -> {
                         supplier.setStatus(SupplierStatus.ACTIVO);
                         return supplierRepository.save(supplierMapper.toEntity(supplier))
                                   .map(supplierMapper::toDomain)
                                   .map(this::mapToResponse);
                    });
     }

     private Supplier mapToDomain(SupplierRequest request) {
          return Supplier.builder()
                    .organizationId(request.getOrganizationId())
                    .supplierCode(request.getSupplierCode())
                    .supplierName(request.getSupplierName())
                    .contactPerson(request.getContactPerson())
                    .phone(request.getPhone())
                    .email(request.getEmail())
                    .address(request.getAddress())
                    .status(request.getStatus())
                    .build();
     }

     private SupplierResponse mapToResponse(Supplier supplier) {
          return SupplierResponse.builder()
                    .supplierId(supplier.getSupplierId())
                    .organizationId(supplier.getOrganizationId())
                    .supplierCode(supplier.getSupplierCode())
                    .supplierName(supplier.getSupplierName())
                    .contactPerson(supplier.getContactPerson())
                    .phone(supplier.getPhone())
                    .email(supplier.getEmail())
                    .address(supplier.getAddress())
                    .status(supplier.getStatus())
                    .createdAt(supplier.getCreatedAt())
                    .build();
     }
}
