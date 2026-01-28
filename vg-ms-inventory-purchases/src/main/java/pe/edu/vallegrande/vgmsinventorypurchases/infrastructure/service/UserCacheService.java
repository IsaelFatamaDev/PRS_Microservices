package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.client.internal.UsersServiceClient;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.EnrichedInventoryMovementResponse;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de caché en memoria para información de usuarios
 * Reduce llamadas al microservicio de usuarios
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheService {

     private final UsersServiceClient usersServiceClient;

     // Caché simple con TTL de 5 minutos
     private final Map<String, CachedUserInfo> cache = new ConcurrentHashMap<>();
     private static final Duration CACHE_TTL = Duration.ofMinutes(5);
     private static final int MAX_CACHE_SIZE = 100;

     /**
      * Obtiene información del usuario con caché
      */
     public Mono<EnrichedInventoryMovementResponse.UserInfo> getUserInfo(String userId) {
          if (userId == null || userId.trim().isEmpty()) {
               return Mono.just(createDefaultUserInfo("UNKNOWN"));
          }

          // Verificar si está en caché y es válido
          CachedUserInfo cached = cache.get(userId);
          if (cached != null && !cached.isExpired()) {
               log.debug("✅ Usuario obtenido desde caché: {}", userId);
               return Mono.just(cached.userInfo);
          }

          // Obtener desde el servicio con retry
          return usersServiceClient.getUserDetails(userId)
                    .timeout(Duration.ofSeconds(3))
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(100))
                              .maxBackoff(Duration.ofMillis(500)))
                    .map(response -> {
                         if (response.getSuccess() && response.getData() != null) {
                              var userData = response.getData();
                              log.debug("✅ Usuario obtenido desde servicio: {} {}",
                                        userData.getFirstName(), userData.getLastName());

                              var userInfo = EnrichedInventoryMovementResponse.UserInfo.builder()
                                        .id(userData.getId())
                                        .userCode(userData.getUserCode())
                                        .firstName(userData.getFirstName())
                                        .lastName(userData.getLastName())
                                        .fullName(userData.getFirstName() + " " + userData.getLastName())
                                        .email(userData.getEmail())
                                        .phone(userData.getPhone())
                                        .build();

                              // Guardar en caché
                              putInCache(userId, userInfo);
                              return userInfo;
                         } else {
                              log.warn("⚠️ No se pudo obtener información del usuario: {}", userId);
                              return createDefaultUserInfo(userId);
                         }
                    })
                    .onErrorResume(error -> {
                         log.error("❌ Error obteniendo usuario {}: {}", userId, error.getMessage());
                         return Mono.just(createDefaultUserInfo(userId));
                    });
     }

     /**
      * Guarda información en caché con control de tamaño
      */
     private void putInCache(String userId, EnrichedInventoryMovementResponse.UserInfo userInfo) {
          // Limpiar caché si está lleno
          if (cache.size() >= MAX_CACHE_SIZE) {
               cleanExpiredEntries();

               // Si sigue lleno, limpiar el más antiguo
               if (cache.size() >= MAX_CACHE_SIZE) {
                    cache.entrySet().stream()
                              .min((e1, e2) -> Long.compare(e1.getValue().timestamp, e2.getValue().timestamp))
                              .ifPresent(entry -> cache.remove(entry.getKey()));
               }
          }

          cache.put(userId, new CachedUserInfo(userInfo, System.currentTimeMillis()));
     }

     /**
      * Limpia entradas expiradas del caché
      */
     private void cleanExpiredEntries() {
          cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
          log.debug("🧹 Caché limpiado. Tamaño actual: {}", cache.size());
     }

     /**
      * Invalida el caché de un usuario específico
      */
     public void invalidate(String userId) {
          cache.remove(userId);
          log.debug("🗑️ Usuario removido del caché: {}", userId);
     }

     /**
      * Limpia todo el caché
      */
     public void invalidateAll() {
          int size = cache.size();
          cache.clear();
          log.info("🗑️ Caché limpiado completamente. Entradas removidas: {}", size);
     }

     /**
      * Obtiene estadísticas del caché
      */
     public Map<String, Object> getCacheStats() {
          long expired = cache.values().stream()
                    .filter(CachedUserInfo::isExpired)
                    .count();

          return Map.of(
                    "totalEntries", cache.size(),
                    "expiredEntries", expired,
                    "maxSize", MAX_CACHE_SIZE,
                    "ttlMinutes", CACHE_TTL.toMinutes());
     }

     /**
      * Crea información de usuario por defecto
      */
     private EnrichedInventoryMovementResponse.UserInfo createDefaultUserInfo(String userId) {
          return EnrichedInventoryMovementResponse.UserInfo.builder()
                    .id(userId)
                    .userCode("USR???")
                    .firstName("Usuario")
                    .lastName("Desconocido")
                    .fullName("Usuario Desconocido")
                    .email("")
                    .phone("")
                    .build();
     }

     /**
      * Clase interna para almacenar información en caché con timestamp
      */
     private static class CachedUserInfo {
          private final EnrichedInventoryMovementResponse.UserInfo userInfo;
          private final long timestamp;

          CachedUserInfo(EnrichedInventoryMovementResponse.UserInfo userInfo, long timestamp) {
               this.userInfo = userInfo;
               this.timestamp = timestamp;
          }

          boolean isExpired() {
               return System.currentTimeMillis() - timestamp > CACHE_TTL.toMillis();
          }
     }
}
