package pe.edu.vallegrande.vgmsusers.infrastructure.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_code_counters")
@Builder
public class UserCodeCounterDocument {

    @Id
    private String id;

    private String organizationId;

    private Long lastCode;

    @Builder.Default
    private String prefix = "USR";
}
