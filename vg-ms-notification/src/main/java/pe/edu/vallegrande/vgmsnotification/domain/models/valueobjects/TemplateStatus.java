package pe.edu.vallegrande.vgmsnotification.domain.models.valueobjects;

/**
 * Value Object - Estado de Plantilla
 */
public enum TemplateStatus {
     ACTIVE, // Plantilla activa y en uso
     INACTIVE, // Plantilla desactivada
     DRAFT; // Plantilla en borrador

     public boolean canBeUsed() {
          return this == ACTIVE;
     }
}
