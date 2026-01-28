package pe.edu.vallegrande.vgmsorganizations.application.dto.organization;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
    private String name;

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "^[0-9]{11}$", message = "El RUC debe tener 11 dígitos")
    private String ruc;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 300, message = "La dirección no puede exceder 300 caracteres")
    private String address;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos")
    private String phone;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;

    @NotBlank(message = "La región es obligatoria")
    private String region;

    @NotBlank(message = "La provincia es obligatoria")
    private String province;

    @NotBlank(message = "El distrito es obligatorio")
    private String district;

    @NotBlank(message = "El representante legal es obligatorio")
    private String legalRepresentative;

    @NotBlank(message = "El tipo de organización es obligatorio")
    private String type; // JASS, MUNICIPAL, COOPERATIVE, PRIVATE

    @NotBlank(message = "El usuario creador es obligatorio")
    private String createdBy;
}
