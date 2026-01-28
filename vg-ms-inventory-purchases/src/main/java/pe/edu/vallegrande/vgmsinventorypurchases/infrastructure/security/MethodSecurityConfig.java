package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;

@Configuration(proxyBeanMethods = false)
@EnableReactiveMethodSecurity
public class MethodSecurityConfig {
}
