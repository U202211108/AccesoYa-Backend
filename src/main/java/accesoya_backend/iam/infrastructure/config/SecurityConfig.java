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

        // =====================================================
        // SECURITY FILTER CHAIN
        // =====================================================

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
                                //
                                // La aplicación utiliza JWT.
                                // No se utiliza sesión HTTP.
                                //
                                // =====================================================

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(
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
                                                // AUTENTICACIÓN PÚBLICA
                                                // -------------------------------------------------
                                                //
                                                // Registro y login NO requieren JWT.
                                                //
                                                // -------------------------------------------------

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/auth/register",
                                                                "/api/auth/login")
                                                .permitAll()

                                                // -------------------------------------------------
                                                // SWAGGER / OPENAPI
                                                // -------------------------------------------------

                                                .requestMatchers(
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/v3/api-docs/**")
                                                .permitAll()

                                                // -------------------------------------------------
                                                // MAPA PÚBLICO
                                                // -------------------------------------------------
                                                //
                                                // La consulta pública del mapa no requiere
                                                // autenticación.
                                                //
                                                // -------------------------------------------------

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/places/map",
                                                                "/api/places/map/search")
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
                                                //
                                                // Todo endpoint /api/** requiere autenticación.
                                                //
                                                // La autorización por rol se controla mediante
                                                // @PreAuthorize en los controladores/servicios
                                                // correspondientes.
                                                //
                                                // Roles:
                                                //
                                                // CONSULTOR
                                                // OPERADOR_FLNOC
                                                // SUPERVISOR
                                                // ADMIN
                                                //
                                                // -------------------------------------------------

                                                .requestMatchers(
                                                                "/api/**")
                                                .authenticated()

                                                // -------------------------------------------------
                                                // RESTO DE RUTAS
                                                // -------------------------------------------------

                                                .anyRequest()
                                                .denyAll())

                                // =====================================================
                                // JWT AUTHENTICATION FILTER
                                // =====================================================

                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        // =====================================================
        // CORS CONFIGURATION
        // =====================================================

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                // -------------------------------------------------
                // FRONTEND ANGULAR
                // -------------------------------------------------

                configuration.setAllowedOrigins(
                                List.of(
                                                "http://localhost:4200"));

                // -------------------------------------------------
                // MÉTODOS HTTP
                // -------------------------------------------------

                configuration.setAllowedMethods(
                                List.of(
                                                "GET",
                                                "POST",
                                                "PUT",
                                                "PATCH",
                                                "DELETE",
                                                "OPTIONS"));

                // -------------------------------------------------
                // HEADERS
                // -------------------------------------------------

                configuration.setAllowedHeaders(
                                List.of(
                                                "Authorization",
                                                "Content-Type",
                                                "Accept"));

                // -------------------------------------------------
                // HEADERS EXPUESTOS
                // -------------------------------------------------

                configuration.setExposedHeaders(
                                List.of(
                                                "Authorization"));

                // -------------------------------------------------
                // CREDENTIALS
                // -------------------------------------------------

                configuration.setAllowCredentials(true);

                // -------------------------------------------------
                // REGISTRAR CONFIGURACIÓN
                // -------------------------------------------------

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration(
                                "/**",
                                configuration);

                return source;
        }
}