package pe.edu.vallegrande.msdistribution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de Distribución - Sistema JASS Digital
 * 
 * DESCRIPCIÓN:
 * Microservicio encargado de gestionar la distribución de agua potable
 * en comunidades rurales administradas por JASS (Juntas Administradoras
 * de Servicios de Saneamiento).
 * 
 * FUNCIONALIDADES PRINCIPALES:
 * - Gestión de programas de distribución de agua
 * - Administración de rutas y horarios de distribución
 * - Control de tarifas del servicio
 * - Monitoreo del estado de entrega de agua (con/sin agua)
 * - Estadísticas y reportes de distribución
 * 
 * TECNOLOGÍAS:
 * - Spring Boot 3.4.5
 * - Spring WebFlux (Programación Reactiva)
 * - MongoDB (Base de datos NoSQL)
 * - Spring Security + OAuth2 (Keycloak)
 * - Docker (Containerización)
 * 
 * ARQUITECTURA:
 * - Arquitectura Hexagonal (Ports & Adapters)
 * - Domain-Driven Design (DDD)
 * - Programación Reactiva (Reactor)
 * - RESTful API
 * 
 * ESTRUCTURA DE PAQUETES:
 * - domain: Entidades y lógica de negocio
 * - application: Casos de uso y servicios
 * - infrastructure: Adaptadores (REST, DB, Security)
 * 
 * ENDPOINTS BASE:
 * - /internal/** - Endpoints administrativos (requiere rol ADMIN)
 * - /api/public/** - Endpoints públicos
 * - /actuator/** - Monitoreo y health checks
 * 
 * CONFIGURACIÓN:
 * - Puerto: 8086
 * - Context Path: /jass/ms-distribution
 * - Perfil activo: prod (por defecto)
 * 
 * @author Valle Grande
 * @version 2.0.0
 * @since 2024
 */
@SpringBootApplication
public class VgMsDistribution {
    public static void main(String[] args) {
        SpringApplication.run(VgMsDistribution.class, args);
    }
}