package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuestas paginadas
 * 
 * @param <T> Tipo de elementos en la página
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta paginada con metadatos")
public class PagedResponseDto<T> {
    
    @Schema(description = "Lista de elementos de la página actual")
    private List<T> content;
    
    @Schema(description = "Número de página actual (0-indexed)", example = "0")
    private Integer pageNumber;
    
    @Schema(description = "Tamaño de página", example = "20")
    private Integer pageSize;
    
    @Schema(description = "Total de elementos", example = "100")
    private Long totalElements;
    
    @Schema(description = "Total de páginas", example = "5")
    private Integer totalPages;
    
    @Schema(description = "Indica si es la primera página")
    private Boolean first;
    
    @Schema(description = "Indica si es la última página")
    private Boolean last;
    
    @Schema(description = "Indica si hay una página anterior")
    private Boolean hasPrevious;
    
    @Schema(description = "Indica si hay una página siguiente")
    private Boolean hasNext;
    
    public static <T> PagedResponseDto<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        return PagedResponseDto.<T>builder()
                .content(content)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(pageNumber == 0)
                .last(pageNumber >= totalPages - 1)
                .hasPrevious(pageNumber > 0)
                .hasNext(pageNumber < totalPages - 1)
                .build();
    }
}
