package pe.edu.vallegrande.vgmsorganizations.infrastructure.mapper;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.document.FareDocument;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.FareRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.FareResponse;

public class FareMapper {

    /**
     * Convierte de Request a Domain Model
     */
    public static Fare toDomain(FareRequest request) {
        if (request == null) {
            return null;
        }

        return Fare.builder()
                .zoneId(request.getZoneId())
                .fareName(request.getFareName())
                .fareDescription(request.getFareDescription())
                .fareAmount(request.getFareAmount())
                .build();
    }

    /**
     * Convierte de Domain Model a Document
     */
    public static FareDocument toDocument(Fare fare) {
        if (fare == null) {
            return null;
        }

        FareDocument document = FareDocument.builder()
                .fareId(fare.getId())
                .zoneId(fare.getZoneId())
                .fareCode(fare.getFareCode())
                .fareName(fare.getFareName())
                .fareDescription(fare.getFareDescription())
                .fareAmount(fare.getFareAmount())
                .status(fare.getStatus())
                .build();

        // Set inherited fields from BaseDocument
        document.setCreatedAt(fare.getCreatedAt());
        document.setUpdatedAt(fare.getUpdatedAt());

        return document;
    }

    /**
     * Convierte de Document a Domain Model
     */
    public static Fare toDomain(FareDocument document) {
        if (document == null) {
            return null;
        }

        return Fare.builder()
                .id(document.getFareId())
                .fareCode(document.getFareCode())
                .zoneId(document.getZoneId())
                .fareName(document.getFareName())
                .fareDescription(document.getFareDescription())
                .fareAmount(document.getFareAmount())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    /**
     * Convierte de Domain Model a Response (sin zoneName)
     */
    public static FareResponse toResponse(Fare fare) {
        if (fare == null) {
            return null;
        }

        return FareResponse.builder()
                .id(fare.getId())
                .fareCode(fare.getFareCode())
                .zoneId(fare.getZoneId())
                .fareName(fare.getFareName())
                .fareDescription(fare.getFareDescription())
                .fareAmount(fare.getFareAmount())
                .status(fare.getStatus())
                .createdAt(fare.getCreatedAt())
                .updatedAt(fare.getUpdatedAt())
                .build();
    }

    /**
     * Convierte de Domain Model a Response (con zoneName)
     */
    public static FareResponse toResponse(Fare fare, String zoneName) {
        FareResponse response = toResponse(fare);
        if (response != null) {
            response.setZoneName(zoneName);
        }
        return response;
    }
}
