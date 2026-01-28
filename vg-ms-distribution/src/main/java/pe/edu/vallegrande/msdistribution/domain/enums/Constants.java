package pe.edu.vallegrande.msdistribution.domain.enums;

/**
 * Enumeración que define las constantes generales del sistema.
 * Utilizada para estandarizar estados y valores comunes en la aplicación.
 * 
 * @version 1.0
 */
public enum Constants {

    /**
     * Estado activo.
     * Indica que el recurso está disponible y en uso.
     */
    ACTIVE("ACTIVE", "Activo"),

    /**
     * Estado inactivo.
     * Indica que el recurso ha sido deshabilitado o eliminado lógicamente.
     */
    INACTIVE("INACTIVE", "Inactivo");

    private final String value;
    private final String displayName;

    /**
     * Constructor del enum.
     * 
     * @param value       Valor interno del sistema.
     * @param displayName Nombre legible para visualización.
     */
    Constants(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Obtiene el valor interno.
     * 
     * @return Cadena con el valor.
     */
    public String getValue() {
        return value;
    }

    /**
     * Obtiene el nombre para mostrar.
     * 
     * @return Cadena descriptive.
     */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return value;
    }
}
