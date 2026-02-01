package pe.edu.vallegrande.vgmsorganizations.infrastructure.document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "fares")
public class FareDocument extends BaseDocument {

    @Id
    private String fareId;

    @Indexed
    private String zoneId;

    private String fareCode;
    private String fareName;
    private String fareDescription;
    private String fareAmount;

    @Indexed
    @Builder.Default
    private String status = "ACTIVE";
}
