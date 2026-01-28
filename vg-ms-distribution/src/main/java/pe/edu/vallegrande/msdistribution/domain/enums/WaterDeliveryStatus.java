package pe.edu.vallegrande.msdistribution.domain.enums;

/**
 * Enumeración para el estado de entrega de agua en un programa de distribución.
 * Gestiona el ciclo de vida del suministro de agua.
 * 
 * <p>
 * Estados principales:
 * </p>
 * <ul>
 * <li><b>CON_AGUA</b>: Agua entregada (Predeterminado al crear).</li>
 * <li><b>SIN_AGUA</b>: Sin agua (Asignado al eliminar/desactivar).</li>
 * </ul>
 * 
 * @version 1.0
 */
public enum WaterDeliveryStatus {

    /**
     * Estado que indica que el agua ha sido entregada exitosamente.
     * Es el estado por defecto al crear un nuevo programa.
     */
    CON_AGUA("Con Agua", "Agua entregada exitosamente - Estado por defecto"),

    /**
     * Estado que indica que no se entregó agua.
     * Se asigna automáticamente cuando un programa es eliminado o desactivado.
     */
    SIN_AGUA("Sin Agua", "No se entregó agua - Se asigna automáticamente al eliminar");

    private final String displayName;
    private final String description;

    /**
     * Constructor del enum.
     * 
     * @param displayName Nombre corto para visualización.
     * @param description Descripción detallada del estado.
     */
    WaterDeliveryStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Obtiene el nombre para mostrar.
     * 
     * @return Nombre del estado.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Obtiene la descripción del estado.
     * 
     * @return Descripción detallada.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Obtiene el estado por defecto para nuevos programas.
     * 
     * @return Estado CON_AGUA.
     */
    public static WaterDeliveryStatus getDefault() {
        return CON_AGUA;
    }

    /**
     * Obtiene el estado correspondiente a un programa eliminado.
     * 
     * @return Estado SIN_AGUA.
     */
    public static WaterDeliveryStatus getDeletedStatus() {
        return SIN_AGUA;
    }
}
