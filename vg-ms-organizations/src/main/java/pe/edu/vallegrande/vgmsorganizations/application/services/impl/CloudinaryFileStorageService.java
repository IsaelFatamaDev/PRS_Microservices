package pe.edu.vallegrande.vgmsorganizations.application.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Servicio de almacenamiento de archivos usando Cloudinary (GRATUITO)
 * Plan gratuito: 25GB almacenamiento + 25,000 transformaciones/mes
 */
@Service
public class CloudinaryFileStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryFileStorageService.class);

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    private Cloudinary cloudinary;

    // Constructor explícito (PRS1 Standard - No usar @RequiredArgsConstructor con
    // @Value)
    public CloudinaryFileStorageService(
            @Value("${cloudinary.cloud-name:dir3kg8ud}") String cloudName,
            @Value("${cloudinary.api-key:263961767643955}") String apiKey,
            @Value("${cloudinary.api-secret:ouQeY1qekTSODuOr-vrfGSaEAQI}") String apiSecret) {
        this.cloudName = cloudName != null && !cloudName.trim().isEmpty() ? cloudName : "dir3kg8ud";
        this.apiKey = apiKey != null && !apiKey.trim().isEmpty() ? apiKey : "263961767643955";
        this.apiSecret = apiSecret != null && !apiSecret.trim().isEmpty() ? apiSecret : "ouQeY1qekTSODuOr-vrfGSaEAQI";
    }

    private Cloudinary getCloudinary() {
        if (cloudinary == null) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret));
        }
        return cloudinary;
    }

    public Mono<String> processLogo(String base64Logo) {
        if (base64Logo == null || base64Logo.trim().isEmpty()) {
            return Mono.just("");
        }

        return Mono.fromCallable(() -> {
            try {
                // Subir a Cloudinary con optimizaciones automáticas
                Map uploadResult = getCloudinary().uploader().upload(base64Logo,
                        ObjectUtils.asMap(
                                "folder", "jass/organizations/logos",
                                "transformation", "w_300,h_300,c_fill,q_auto,f_auto", // Optimización automática
                                "resource_type", "image"));

                String url = uploadResult.get("secure_url").toString();

                // Retornar URL segura (HTTPS)
                return url;

            } catch (Exception e) {
                log.error("Error subiendo logo a Cloudinary: {}", e.getMessage(), e);
                // En lugar de fallar, retornar string vacío
                return "";
            }
        })
                .onErrorReturn(""); // Fallback en caso de error
    }

    public Mono<Void> deleteLogo(String logoUrl) {
        if (logoUrl == null || logoUrl.trim().isEmpty() || !logoUrl.contains("cloudinary.com")) {
            return Mono.empty();
        }

        return Mono.fromRunnable(() -> {
            try {
                // Extraer public_id de la URL
                String publicId = extractPublicIdFromUrl(logoUrl);
                if (publicId != null) {
                    getCloudinary().uploader().destroy(publicId, ObjectUtils.emptyMap());
                }
            } catch (Exception e) {
                // Silently ignore deletion errors
            }
        });
    }

    private String extractPublicIdFromUrl(String url) {
        try {
            // URL ejemplo:
            // https://res.cloudinary.com/demo/image/upload/v1234567890/jass/organizations/logos/abc123.jpg
            String[] parts = url.split("/upload/");
            if (parts.length > 1) {
                String afterUpload = parts[1];
                // Remover versión si existe (v1234567890/)
                if (afterUpload.startsWith("v")) {
                    int slashIndex = afterUpload.indexOf("/");
                    if (slashIndex > 0) {
                        afterUpload = afterUpload.substring(slashIndex + 1);
                    }
                }
                // Remover extensión
                int dotIndex = afterUpload.lastIndexOf(".");
                if (dotIndex > 0) {
                    afterUpload = afterUpload.substring(0, dotIndex);
                }
                return afterUpload;
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}