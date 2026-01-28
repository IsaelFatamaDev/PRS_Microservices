package pe.edu.vallegrande.vgmsgateway.infrastructure.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
public class GatewayHealthController {

     @GetMapping("/health")
     public Mono<ResponseEntity<Map<String, Object>>> health() {
          return Mono.fromCallable(() -> {
               Map<String, Object> response = new HashMap<>();
               response.put("status", "UP");
               response.put("service", "vg-ms-gateway");
               response.put("timestamp", LocalDateTime.now());
               response.put("version", "1.0.0");
               response.put("description", "API Gateway para Sistema JASS Digital");
               return ResponseEntity.ok(response);
          });
     }

     @GetMapping("/info")
     public Mono<ResponseEntity<Map<String, Object>>> info() {
          return Mono.fromCallable(() -> {
               Map<String, Object> response = new HashMap<>();
               response.put("gateway", "vg-ms-gateway");
               response.put("service_type", "Edge Server");
               response.put("framework", "Spring Cloud Gateway");
               return ResponseEntity.ok(response);
          });
     }
}
