package accesoya_backend.iam.infrastructure.config;

import accesoya_backend.iam.infrastructure.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http

                                // =====================================================
                                // CORS
                                // =====================================================

                                .cors(cors -> cors.configurationSource(
                                                corsConfigurationSource()))

                                // =====================================================
                                // CSRF
                                // =====================================================

                                .csrf(csrf -> csrf.disable())

                                // =====================================================
                                // SESIONES
                                // =====================================================

                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                // =====================================================
                                // AUTORIZACIÓN
                                // =====================================================

                                .authorizeHttpRequests(auth -> auth

                                                // -------------------------------------------------
                                                // CORS PREFLIGHT
                                                // -------------------------------------------------

                                                .requestMatchers(
                                                                HttpMethod.OPTIONS,
                                                                "/**")
                                                .permitAll()

                                                // -------------------------------------------------
                                                // AUTENTICACIÓN
                                                // -------------------------------------------------

                                                .requestMatchers(
                                                                "/api/auth/register",
                                                                "/api/auth/login")
                                                .permitAll()

                                                // -------------------------------------------------
                                                // MAPA PÚBLICO
                                                // -------------------------------------------------

                                                .requestMatchers(
                                                                "/api/places/map",
                                                                "/api/places/map/search")
                                                .permitAll()

                                                // -------------------------------------------------
                                                // SWAGGER
                                                // -------------------------------------------------

                                                .requestMatchers(
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/v3/api-docs/**")
                                                .permitAll()

                                                // -------------------------------------------------
                                                // DASHBOARD
                                                // -------------------------------------------------

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/dashboard")
                                                .authenticated()

                                                // -------------------------------------------------
                                                // NOTIFICACIONES
                                                // -------------------------------------------------

                                                .requestMatchers(
                                                                "/api/notifications/**")
                                                .authenticated()

                                                // -------------------------------------------------
                                                // RESTO DE LA API
                                                // -------------------------------------------------

                                                .requestMatchers(
                                                                "/api/**")
                                                .authenticated()

                                                // -------------------------------------------------
                                                // RESTO
                                                // -------------------------------------------------

                                                .anyRequest().denyAll())

                                // =====================================================
                                // JWT FILTER
                                // =====================================================

                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        // =====================================================
        // CORS
        // =====================================================

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(
                                List.of(
                                                "http://localhost:4200"));

                configuration.setAllowedMethods(
                                List.of(
                                                "GET",
                                                "POST",
                                                "PUT",
                                                "PATCH",
                                                "DELETE",
                                                "OPTIONS"));

                configuration.setAllowedHeaders(
                                List.of(
                                                "Authorization",
                                                "Content-Type",
                                                "Accept"));

                configuration.setExposedHeaders(
                                List.of(
                                                "Authorization"));

                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration(
                                "/**",
                                configuration);

                return source;
        }
}