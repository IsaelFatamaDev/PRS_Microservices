package pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.BoxType;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterBoxResponse {
    private Long id;
    private String organizationId;
    private String boxCode;
    private BoxType boxType;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate installationDate;
    
    private Long currentAssignmentId;
    private Status status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}