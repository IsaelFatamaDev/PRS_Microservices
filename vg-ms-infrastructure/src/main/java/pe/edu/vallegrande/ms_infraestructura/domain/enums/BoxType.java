package pe.edu.vallegrande.ms_infraestructura.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BoxType {
    CAÑO("CAÑO"),
    BOMBA("BOMBA"),
    OTRO("OTRO");

    private final String value;

    BoxType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static BoxType fromValue(String value) {
        for (BoxType type : BoxType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Valor no válido para BoxType: " + value);
    }
}