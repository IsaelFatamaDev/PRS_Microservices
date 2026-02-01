package pe.edu.vallegrande.vgmsorganizations.infrastructure.mapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper base con métodos comunes para conversión de listas
 * Según estándar PRS1 - Arquitectura Hexagonal
 * 
 * @param <D> Domain Model (Modelo de dominio)
 * @param <E> Entity/Document (Modelo de persistencia)
 */
public interface BaseMapper<D, E> {
    
    /**
     * Convierte de Entity a Domain
     */
    D toDomain(E entity);
    
    /**
     * Convierte de Domain a Entity
     */
    E toEntity(D domain);
    
    /**
     * Convierte lista de Entity a lista de Domain
     */
    default List<D> toDomainList(List<E> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte lista de Domain a lista de Entity
     */
    default List<E> toEntityList(List<D> domains) {
        if (domains == null) {
            return null;
        }
        return domains.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
