package pe.edu.vallegrande.vgmsorganizations.domain.enums;

/**
 * Constantes del dominio
 * Según PRS1 - Valores como String para compatibilidad con MongoDB
 */
public final class Constants {
    
    // Estados
    public static final String ACTIVE = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";
    
    // Constructor privado para evitar instanciación
    private Constants() {
        throw new UnsupportedOperationException("Esta es una clase de constantes y no debe ser instanciada");
    }
}
