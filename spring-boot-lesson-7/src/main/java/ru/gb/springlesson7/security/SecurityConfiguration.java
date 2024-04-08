package ru.gb.springlesson7.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/admin/**").hasAuthority("admin")
                .requestMatchers("/user/**").hasAnyAuthority("user", "admin")
                .requestMatchers("/auth/**").authenticated()
                .requestMatchers("/any/**").permitAll()
                .requestMatchers("/api/**").permitAll()
                .anyRequest().denyAll()
            )
            .formLogin(Customizer.withDefaults()) // Включаем форму логина для HTML-страниц
            .oauth2ResourceServer(configurer -> configurer
                .jwt(jwtConfigurer -> jwtConfigurer
                    .jwtAuthenticationConverter(jwtGrantedAuthoritiesConverter())
                )
            );

        return http.build();
    }

    // Конвертер для извлечения ролей из JWT и преобразования в SimpleGrantedAuthority
    private JwtAuthenticationConverter jwtGrantedAuthoritiesConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            if (jwt.getClaim("realm_access") != null) {
                Map<String, Object> realmAccess = jwt.getClaim("realm_access");
                if (realmAccess.containsKey("roles") && realmAccess.get("roles") instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) realmAccess.get("roles");
                    return roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                }
            }
            return List.of();
        });
        return converter;
    }
}
