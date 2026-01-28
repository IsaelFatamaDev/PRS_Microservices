package pe.edu.vallegrande.vgmsinventorypurchases.application.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class AuthenticationService {

     public Mono<String> getCurrentUserId(Authentication authentication) {
          return Mono.fromCallable(() -> {
               if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                    return jwt.getClaimAsString("sub");
               }
               throw new RuntimeException("Usuario no autenticado");
          });
     }

     public Mono<String> getCurrentUserEmail(Authentication authentication) {
          return Mono.fromCallable(() -> {
               if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                    return jwt.getClaimAsString("email");
               }
               throw new RuntimeException("Usuario no autenticado");
          });
     }

     public Mono<String> getCurrentUserName(Authentication authentication) {
          return Mono.fromCallable(() -> {
               if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                    return jwt.getClaimAsString("name");
               }
               throw new RuntimeException("Usuario no autenticado");
          });
     }

     @SuppressWarnings("unchecked")
     public Mono<List<String>> getCurrentUserRoles(Authentication authentication) {
          return Mono.fromCallable(() -> {
               if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                    Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                    if (realmAccess != null) {
                         return (List<String>) realmAccess.get("roles");
                    }
               }
               throw new IllegalStateException("No se pudieron obtener los roles del usuario");
          });
     }

     public Mono<Boolean> hasAdminRole(Authentication authentication) {
          return getCurrentUserRoles(authentication)
                    .map(roles -> roles != null && roles.contains("ADMIN"))
                    .onErrorReturn(false);
     }
}
