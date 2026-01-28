package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "streets")
public class StreetDocument {

    @Id
    private String id;

    @Indexed
    private String zoneId;

    private String streetName;

    @Indexed(unique = true)
    private String streetCode;

    private String description;
    private String status;
    private LocalDateTime createdAt;
    private String createdBy;
}
