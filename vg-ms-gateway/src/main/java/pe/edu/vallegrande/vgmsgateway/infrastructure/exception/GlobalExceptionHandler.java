package pe.edu.vallegrande.vgmsgateway.infrastructure.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import pe.edu.vallegrande.vgmsgateway.domain.enums.GatewayError;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @SuppressWarnings("null")
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Error handling request: {}", ex.getMessage());

        HttpStatus status;
        String errorCode;
        String errorMessage;

        if (ex instanceof AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED;
            errorCode = GatewayError.AUTH_TOKEN_INVALID.getCode();
            errorMessage = GatewayError.AUTH_TOKEN_INVALID.getMessage();
        } else if (ex instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
            errorCode = GatewayError.ACCESS_INSUFFICIENT_PERMISSIONS.getCode();
            errorMessage = GatewayError.ACCESS_INSUFFICIENT_PERMISSIONS.getMessage();
        } else if (ex instanceof ResponseStatusException) {
            status = HttpStatus.valueOf(((ResponseStatusException) ex).getStatusCode().value());
            errorCode = GatewayError.SERVER_INTERNAL_ERROR.getCode();
            errorMessage = ex.getMessage();
        } else if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = GatewayError.VALIDATION_INVALID_PARAMETER.getCode();
            errorMessage = GatewayError.VALIDATION_INVALID_PARAMETER.getMessage();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = GatewayError.SERVER_INTERNAL_ERROR.getCode();
            errorMessage = GatewayError.SERVER_INTERNAL_ERROR.getMessage();
        }

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("success", false);
        apiResponse.put("message", errorMessage);

        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("code", errorCode);
        errorDetails.put("message", errorMessage);
        apiResponse.put("error", errorDetails);

        apiResponse.put("timestamp", LocalDateTime.now().toString());
        apiResponse.put("path", exchange.getRequest().getPath().value());
        apiResponse.put("status", status.value());

        String body;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException jsonEx) {
            log.error("Error serializing error response", jsonEx);
            body = "{\"success\":false,\"message\":\"Error de serialización\",\"error\":{\"code\":\"GW_SERIALIZATION_001\",\"message\":\"Error al procesar la respuesta\"}}";
        }

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
