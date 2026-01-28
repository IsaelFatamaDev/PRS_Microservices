package pe.edu.vallegrande.vgmsgateway.domain.enums;

public enum GatewayError {

    // Auth
    AUTH_TOKEN_INVALID("GW_AUTH_001", "No estás autorizado. Token de autenticación inválido"),
    AUTH_TOKEN_EXPIRED("GW_AUTH_002", "Tu sesión ha expirado. Por favor, inicia sesión nuevamente"),
    AUTH_TOKEN_MISSING("GW_AUTH_003", "Token de autenticación requerido"),

    // Access
    ACCESS_INSUFFICIENT_PERMISSIONS("GW_ACCESS_001", "No puedes acceder a este recurso. Permisos insuficientes"),
    ACCESS_ROLE_NOT_FOUND("GW_ACCESS_002", "No se pudo determinar tu rol de usuario"),
    ACCESS_RESOURCE_FORBIDDEN("GW_ACCESS_003", "Acceso denegado a este recurso"),

    // Validation
    VALIDATION_INVALID_PARAMETER("GW_VALIDATION_001", "Parámetros inválidos en la solicitud"),
    VALIDATION_MISSING_PARAMETER("GW_VALIDATION_002", "Faltan parámetros requeridos"),
    VALIDATION_INVALID_FORMAT("GW_VALIDATION_003", "Formato de datos incorrecto"),

    // Server
    SERVER_INTERNAL_ERROR("GW_INTERNAL_001", "Error interno del servidor. Intente nuevamente más tarde"),
    SERVER_SERVICE_UNAVAILABLE("GW_INTERNAL_002", "Servicio temporalmente no disponible"),
    SERVER_TIMEOUT("GW_INTERNAL_003", "Tiempo de espera agotado. Intente nuevamente"),
    SERVER_SERIALIZATION_ERROR("GW_SERIALIZATION_001", "Error al procesar la respuesta"),

    // Not Found
    NOT_FOUND_RESOURCE("GW_NOT_FOUND_001", "El recurso solicitado no fue encontrado"),
    NOT_FOUND_ENDPOINT("GW_NOT_FOUND_002", "Endpoint no encontrado"),
    NOT_FOUND_SERVICE("GW_NOT_FOUND_003", "Servicio no disponible"),

    // Bad Request
    BAD_REQUEST_INVALID_DATA("GW_BAD_REQUEST_001", "Los datos enviados no son válidos"),
    BAD_REQUEST_MALFORMED_JSON("GW_BAD_REQUEST_002", "Formato JSON inválido"),
    BAD_REQUEST_INVALID_CONTENT_TYPE("GW_BAD_REQUEST_003", "Tipo de contenido no soportado");

    private final String code;
    private final String message;

    GatewayError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
