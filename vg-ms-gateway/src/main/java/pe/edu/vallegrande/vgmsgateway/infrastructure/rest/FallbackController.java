package pe.edu.vallegrande.vgmsgateway.infrastructure.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.vallegrande.vgmsgateway.domain.enums.GatewayError;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

     @GetMapping
     public Mono<ResponseEntity<Map<String, Object>>> fallback() {
          return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                              "timestamp", LocalDateTime.now(),
                              "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                              "error", HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                              "code", GatewayError.SERVER_SERVICE_UNAVAILABLE.getCode(),
                              "message", GatewayError.SERVER_SERVICE_UNAVAILABLE.getMessage())));
     }
}
