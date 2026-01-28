package pe.edu.vallegrande.vgmsnotification.infrastructure.adapters.in.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsnotification.application.dtos.notification.NotificationResponse;
import pe.edu.vallegrande.vgmsnotification.application.dtos.notification.SendNotificationRequest;
import pe.edu.vallegrande.vgmsnotification.application.dtos.shared.ApiResponse;
import pe.edu.vallegrande.vgmsnotification.application.mappers.NotificationMapper;
import pe.edu.vallegrande.vgmsnotification.domain.models.Notification;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.IGetNotificationUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.IMarkAsReadUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.IRetryFailedNotificationUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.ISendNotificationUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.valueobjects.NotificationStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationRest {
    
    private final ISendNotificationUseCase sendNotificationUseCase;
    private final IGetNotificationUseCase getNotificationUseCase;
    private final IMarkAsReadUseCase markAsReadUseCase;
    private final IRetryFailedNotificationUseCase retryFailedNotificationUseCase;
    
    @PostMapping("/send")
    public Mono<ResponseEntity<ApiResponse<NotificationResponse>>> sendNotification(
            @RequestBody SendNotificationRequest request) {
        
        Notification notification = NotificationMapper.toDomain(request);
        
        return sendNotificationUseCase.execute(notification)
            .map(NotificationMapper::toResponse)
            .map(response -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Notification sent successfully")))
            .onErrorResume(e -> Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(e.getMessage()))));
    }
    
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<NotificationResponse>>> getNotificationById(
            @PathVariable String id) {
        
        return getNotificationUseCase.findById(id)
            .map(NotificationMapper::toResponse)
            .map(response -> ResponseEntity
                .ok(ApiResponse.success(response, "Notification retrieved successfully")))
            .switchIfEmpty(Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Notification not found"))));
    }
    
    @GetMapping("/user/{userId}")
    public Mono<ResponseEntity<ApiResponse<Flux<NotificationResponse>>>> getNotificationsByUserId(
            @PathVariable String userId) {
        
        Flux<NotificationResponse> notifications = getNotificationUseCase.findByUserId(userId)
            .map(NotificationMapper::toResponse);
        
        return Mono.just(ResponseEntity
            .ok(ApiResponse.success(notifications, "Notifications retrieved successfully")));
    }
    
    @GetMapping("/user/{userId}/unread")
    public Mono<ResponseEntity<ApiResponse<Flux<NotificationResponse>>>> getUnreadNotifications(
            @PathVariable String userId) {
        
        Flux<NotificationResponse> notifications = getNotificationUseCase.findUnreadByUserId(userId)
            .map(NotificationMapper::toResponse);
        
        return Mono.just(ResponseEntity
            .ok(ApiResponse.success(notifications, "Unread notifications retrieved successfully")));
    }
    
    @GetMapping("/status/{status}")
    public Mono<ResponseEntity<ApiResponse<Flux<NotificationResponse>>>> getNotificationsByStatus(
            @PathVariable String status) {
        
        try {
            NotificationStatus notificationStatus = NotificationStatus.valueOf(status.toUpperCase());
            Flux<NotificationResponse> notifications = getNotificationUseCase
                .findByStatus(notificationStatus)
                .map(NotificationMapper::toResponse);
            
            return Mono.just(ResponseEntity
                .ok(ApiResponse.success(notifications, "Notifications retrieved successfully")));
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid status: " + status)));
        }
    }
    
    @PatchMapping("/{id}/read")
    public Mono<ResponseEntity<ApiResponse<NotificationResponse>>> markAsRead(
            @PathVariable String id) {
        
        return markAsReadUseCase.execute(id)
            .map(NotificationMapper::toResponse)
            .map(response -> ResponseEntity
                .ok(ApiResponse.success(response, "Notification marked as read")))
            .onErrorResume(e -> Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()))));
    }
    
    @PostMapping("/{id}/retry")
    public Mono<ResponseEntity<ApiResponse<NotificationResponse>>> retryNotification(
            @PathVariable String id) {
        
        return retryFailedNotificationUseCase.execute(id)
            .map(NotificationMapper::toResponse)
            .map(response -> ResponseEntity
                .ok(ApiResponse.success(response, "Notification retry initiated")))
            .onErrorResume(e -> Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()))));
    }
}
