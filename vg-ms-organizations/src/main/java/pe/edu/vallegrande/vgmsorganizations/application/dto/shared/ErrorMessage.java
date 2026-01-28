package pe.edu.vallegrande.vgmsorganizations.application.dto.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage {
    private int status;
    private LocalDateTime timestamp;
    private String message;
    private String path;

    public static ErrorMessage of(int status, String message, String path) {
        return new ErrorMessage(status, LocalDateTime.now(), message, path);
    }
}
