package pe.edu.vallegrande.vg_ms_payment.infrastructure.mapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper base con métodos comunes para todas las conversiones
 */
public interface BaseMapper<E, D> {
    
    /**
     * Convierte de Entity a Domain
     */
    D entityToDomain(E entity);
    
    /**
     * Convierte de Domain a Entity
     */
    E domainToEntity(D domain);
    
    /**
     * Convierte lista de Entity a lista de Domain
     */
    default List<D> entityListToDomainList(List<E> entities) {
        return entities.stream()
                .map(this::entityToDomain)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte lista de Domain a lista de Entity
     */
    default List<E> domainListToEntityList(List<D> domains) {
        return domains.stream()
                .map(this::domainToEntity)
                .collect(Collectors.toList());
    }
}