package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.mapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper base con métodos comunes para todas las conversiones.
 * Proporciona utilidades de mapeo genéricas.
 */
public abstract class BaseMapper<D, E> {
    
    /**
     * Convierte una entidad de dominio a documento de persistencia.
     * @param domain Entidad de dominio
     * @return Documento de persistencia
     */
    public abstract E toDocument(D domain);
    
    /**
     * Convierte un documento de persistencia a entidad de dominio.
     * @param document Documento de persistencia
     * @return Entidad de dominio
     */
    public abstract D toDomain(E document);
    
    /**
     * Convierte una lista de documentos a lista de entidades de dominio.
     * @param documents Lista de documentos
     * @return Lista de entidades de dominio
     */
    public List<D> toDomainList(List<E> documents) {
        if (documents == null) {
            return null;
        }
        return documents.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte una lista de entidades de dominio a lista de documentos.
     * @param domains Lista de entidades de dominio
     * @return Lista de documentos
     */
    public List<E> toDocumentList(List<D> domains) {
        if (domains == null) {
            return null;
        }
        return domains.stream()
                .map(this::toDocument)
                .collect(Collectors.toList());
    }
}
