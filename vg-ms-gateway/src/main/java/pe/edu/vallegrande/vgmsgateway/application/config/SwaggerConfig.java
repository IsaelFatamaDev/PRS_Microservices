package pe.edu.vallegrande.vgmsgateway.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class SwaggerConfig {

     /**
      * Configuración para servir Swagger UI a través del Gateway
      * Esto permite que las documentaciones de los microservicios sean accesibles
      * a través de rutas unificadas desde el Gateway
      */
     @Bean
     public RouterFunction<ServerResponse> swaggerRoutes() {
          return route(GET("/swagger-ui.html"),
                    request -> ServerResponse
                              .temporaryRedirect(java.net.URI.create("/docs/gateway/swagger-ui/index.html")).build())

                    .andRoute(GET("/"), request -> ServerResponse.ok().bodyValue(
                              """
                                        <!DOCTYPE html>
                                        <html>
                                        <head>
                                            <title>Gateway - JASS Digital</title>
                                            <style>
                                                body { font-family: Arial, sans-serif; margin: 40px; }
                                                .service { margin: 20px 0; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }
                                                .service h3 { color: #2c3e50; }
                                                .links a { display: inline-block; margin: 5px 10px 5px 0; padding: 8px 16px;
                                                          background: #3498db; color: white; text-decoration: none; border-radius: 4px; }
                                                .links a:hover { background: #2980b9; }
                                                .endpoint { background: #f8f9fa; padding: 10px; margin: 5px 0; border-radius: 4px; }
                                            </style>
                                        </head>
                                        <body>
                                            <h1>🚀 JASS Digital - API Gateway</h1>
                                            <p>Bienvenido al Gateway de Microservicios del Sistema JASS</p>

                                            <div class="service">
                                                <h3>🔐 Servicio de Autenticación</h3>
                                                <div class="links">
                                                    <a href="/docs/auth/swagger-ui/index.html" target="_blank">📖 Swagger UI</a>
                                                    <a href="/docs/auth/v3/api-docs" target="_blank">📄 OpenAPI JSON</a>
                                                    <a href="/health/auth" target="_blank">💚 Health Check</a>
                                                </div>
                                                <div class="endpoint"><strong>Endpoint Base:</strong> <code>/auth/*</code></div>
                                            </div>

                                            <div class="service">
                                                <h3>👥 Servicio de Usuarios</h3>
                                                <div class="links">
                                                    <a href="/docs/users/swagger-ui/index.html" target="_blank">📖 Swagger UI</a>
                                                    <a href="/docs/users/v3/api-docs" target="_blank">📄 OpenAPI JSON</a>
                                                    <a href="/health/users" target="_blank">💚 Health Check</a>
                                                </div>
                                                <div class="endpoint"><strong>Management:</strong> <code>/management/users/*</code> (Solo SUPER_ADMIN)</div>
                                                <div class="endpoint"><strong>Admin:</strong> <code>/admin/users/*</code> (SUPER_ADMIN, ADMIN)</div>
                                                <div class="endpoint"><strong>Common:</strong> <code>/common/users/*</code> (Todos los roles)</div>
                                            </div>

                                            <div class="service">
                                                <h3>🔧 Enlaces Útiles</h3>
                                                <div class="links">
                                                    <a href="/actuator" target="_blank">⚙️ Actuator</a>
                                                    <a href="/actuator/gateway/routes" target="_blank">🛣️ Ver Rutas</a>
                                                </div>
                                            </div>

                                            <hr>
                                            <p><em>Gateway ejecutándose en puerto 9090 | Autenticación: 8086 | Usuarios: 8085</em></p>
                                        </body>
                                        </html>
                                        """));
     }
}
