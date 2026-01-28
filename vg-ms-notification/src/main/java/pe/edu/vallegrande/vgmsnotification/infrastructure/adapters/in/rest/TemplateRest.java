package pe.edu.vallegrande.vgmsnotification.infrastructure.adapters.in.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsnotification.application.dtos.shared.ApiResponse;
import pe.edu.vallegrande.vgmsnotification.application.dtos.template.CreateTemplateRequest;
import pe.edu.vallegrande.vgmsnotification.application.dtos.template.TemplateResponse;
import pe.edu.vallegrande.vgmsnotification.application.mappers.TemplateMapper;
import pe.edu.vallegrande.vgmsnotification.domain.models.NotificationTemplate;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.ICreateTemplateUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.IGetTemplateUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.valueobjects.NotificationChannel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateRest {
    
    private final ICreateTemplateUseCase createTemplateUseCase;
    private final IGetTemplateUseCase getTemplateUseCase;
    
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<TemplateResponse>>> createTemplate(
            @RequestBody CreateTemplateRequest request) {
        
        NotificationTemplate template = TemplateMapper.toDomain(request);
        
        return createTemplateUseCase.execute(template)
            .map(TemplateMapper::toResponse)
            .map(response -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Template created successfully")))
            .onErrorResume(e -> Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()))));
    }
    
    @GetMapping("/code/{code}")
    public Mono<ResponseEntity<ApiResponse<TemplateResponse>>> getTemplateByCode(
            @PathVariable String code) {
        
        return getTemplateUseCase.findByCode(code)
            .map(TemplateMapper::toResponse)
            .map(response -> ResponseEntity
                .ok(ApiResponse.success(response, "Template retrieved successfully")))
            .switchIfEmpty(Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Template not found"))));
    }
    
    @GetMapping("/channel/{channel}")
    public Mono<ResponseEntity<ApiResponse<Flux<TemplateResponse>>>> getTemplatesByChannel(
            @PathVariable String channel) {
        
        try {
            NotificationChannel notificationChannel = NotificationChannel.valueOf(channel.toUpperCase());
            Flux<TemplateResponse> templates = getTemplateUseCase.findByChannel(notificationChannel)
                .map(TemplateMapper::toResponse);
            
            return Mono.just(ResponseEntity
                .ok(ApiResponse.success(templates, "Templates retrieved successfully")));
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid channel: " + channel)));
        }
    }
    
    @GetMapping("/active")
    public Mono<ResponseEntity<ApiResponse<Flux<TemplateResponse>>>> getActiveTemplates() {
        Flux<TemplateResponse> templates = getTemplateUseCase.findActive()
            .map(TemplateMapper::toResponse);
        
        return Mono.just(ResponseEntity
            .ok(ApiResponse.success(templates, "Active templates retrieved successfully")));
    }
}
