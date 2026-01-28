package pe.edu.vallegrande.vgmsnotification.application.dto.shared;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage {
    private String error;
    private String message;
    private String path;
    private Integer status;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
