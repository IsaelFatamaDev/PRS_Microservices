package pe.edu.vallegrande.msdistribution.infrastructure.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Mapper base con métodos comunes para conversión entre tipos de datos.
 * Provee utilitarios convertir fechas y manejar nulos.
 * 
 * @version 1.0
 */
public abstract class BaseMapper {

    /**
     * Convierte Instant a LocalDateTime usando la zona horaria del sistema.
     * 
     * @param instant Instante de tiempo o null.
     * @return LocalDateTime o null.
     */
    protected LocalDateTime toLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }

    /**
     * Convierte LocalDateTime a Instant usando la zona horaria del sistema.
     * 
     * @param localDateTime Fecha y hora local o null.
     * @return Instant o null.
     */
    protected Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
    }

    /**
     * Retorna el valor por defecto si el valor dado es nulo.
     * 
     * @param value        Valor a evaluar.
     * @param defaultValue Valor por defecto.
     * @return value si no es nulo, defaultValue en caso contrario.
     */
    protected String defaultIfNull(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * Utilitario genérico para valores por defecto.
     * 
     * @param <T>          Tipo de dato.
     * @param value        Valor a evaluar.
     * @param defaultValue Valor por defecto.
     * @return value si no es nulo, defaultValue en caso contrario.
     */
    protected <T> T defaultIfNull(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
