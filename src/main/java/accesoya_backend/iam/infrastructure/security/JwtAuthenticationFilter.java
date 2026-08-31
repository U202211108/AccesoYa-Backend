package accesoya_backend.iam.infrastructure.security;

import accesoya_backend.iam.domain.model.User;
import accesoya_backend.iam.domain.model.UserStatus;
import accesoya_backend.iam.domain.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
                extends OncePerRequestFilter {

        private final JwtService jwtService;

        private final UserRepository userRepository;

        @Override
        protected void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain)
                        throws ServletException, IOException {

                // =====================================================
                // INFORMACIÓN DE LA PETICIÓN
                // =====================================================

                String requestUri = request.getRequestURI();

                String method = request.getMethod();

                System.out.println();
                System.out.println("========================================");
                System.out.println("JWT FILTER");
                System.out.println("Método: " + method);
                System.out.println("URI: " + requestUri);
                System.out.println("========================================");

                // =====================================================
                // OBTENER AUTHORIZATION HEADER
                // =====================================================

                String authorizationHeader = request.getHeader("Authorization");

                if (authorizationHeader == null ||
                                authorizationHeader.isBlank()) {

                        System.out.println(
                                        "JWT: No existe Authorization header.");

                        filterChain.doFilter(
                                        request,
                                        response);

                        return;
                }

                // =====================================================
                // VALIDAR FORMATO BEARER
                // =====================================================

                if (!authorizationHeader.startsWith("Bearer ")) {

                        System.out.println(
                                        "JWT: Authorization header no tiene formato Bearer.");

                        filterChain.doFilter(
                                        request,
                                        response);

                        return;
                }

                // =====================================================
                // EXTRAER TOKEN
                // =====================================================

                String token = authorizationHeader.substring(7);

                if (token.isBlank()) {

                        System.out.println(
                                        "JWT: Token vacío.");

                        filterChain.doFilter(
                                        request,
                                        response);

                        return;
                }

                try {

                        // =================================================
                        // VALIDAR TOKEN
                        // =================================================

                        System.out.println(
                                        "JWT: Validando token...");

                        boolean valid = jwtService.isTokenValid(token);

                        if (!valid) {

                                System.out.println(
                                                "JWT: TOKEN INVÁLIDO.");

                                SecurityContextHolder.clearContext();

                                filterChain.doFilter(
                                                request,
                                                response);

                                return;
                        }

                        System.out.println(
                                        "JWT: Token válido.");

                        // =================================================
                        // EXTRAER USER ID
                        // =================================================

                        String userId = jwtService.extractUserId(token);

                        System.out.println(
                                        "JWT: User ID extraído: " +
                                                        userId);

                        // =================================================
                        // VALIDAR UUID
                        // =================================================

                        UUID uuid;

                        try {

                                uuid = UUID.fromString(userId);

                        } catch (IllegalArgumentException exception) {

                                System.out.println(
                                                "JWT ERROR: El subject del token no es un UUID válido.");

                                System.out.println(
                                                "Valor recibido: " +
                                                                userId);

                                SecurityContextHolder.clearContext();

                                filterChain.doFilter(
                                                request,
                                                response);

                                return;
                        }

                        // =================================================
                        // BUSCAR USUARIO
                        // =================================================

                        User user = userRepository
                                        .findById(uuid)
                                        .orElse(null);

                        if (user == null) {

                                System.out.println(
                                                "JWT ERROR: Usuario no encontrado en BD.");

                                System.out.println(
                                                "UUID buscado: " +
                                                                uuid);

                                SecurityContextHolder.clearContext();

                                filterChain.doFilter(
                                                request,
                                                response);

                                return;
                        }

                        // =================================================
                        // VALIDAR ESTADO
                        // =================================================

                        System.out.println(
                                        "JWT: Usuario encontrado.");

                        System.out.println(
                                        "Email: " +
                                                        user.getEmail());

                        System.out.println(
                                        "Estado: " +
                                                        user.getStatus());

                        System.out.println(
                                        "Rol: " +
                                                        user.getRole());

                        if (user.getStatus() != UserStatus.ACTIVE) {

                                System.out.println(
                                                "JWT ERROR: Usuario no está ACTIVE.");

                                SecurityContextHolder.clearContext();

                                filterChain.doFilter(
                                                request,
                                                response);

                                return;
                        }

                        // =================================================
                        // CREAR AUTHORITY
                        // =================================================

                        String role = user.getRole().name();

                        String authorityName = "ROLE_" + role;

                        System.out.println(
                                        "JWT: Authority creada: " +
                                                        authorityName);

                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                                        "ROLE_" + role);

                        UsernamePasswordAuthenticationToken authentication =

                                        new UsernamePasswordAuthenticationToken(

                                                        user,

                                                        null,

                                                        List.of(authority));

                        SecurityContextHolder
                                        .getContext()
                                        .setAuthentication(
                                                        authentication);

                        // =================================================
                        // GUARDAR EN SECURITY CONTEXT
                        // =================================================

                        SecurityContextHolder
                                        .getContext()
                                        .setAuthentication(
                                                        authentication);

                        // =================================================
                        // VERIFICACIÓN
                        // =================================================

                        System.out.println(
                                        "JWT: Authentication creada correctamente.");

                        System.out.println(
                                        "JWT: Usuario autenticado: " +
                                                        user.getEmail());

                        System.out.println(
                                        "JWT: Authority: " +
                                                        authorityName);

                        System.out.println(
                                        "JWT: SecurityContext contiene Authentication = " +
                                                        (SecurityContextHolder
                                                                        .getContext()
                                                                        .getAuthentication() != null));

                        System.out.println(
                                        "========================================");

                } catch (Exception exception) {

                        // =================================================
                        // ERROR REAL
                        // =================================================

                        System.out.println();
                        System.out.println(
                                        "========================================");

                        System.out.println(
                                        "JWT ERROR CRÍTICO");

                        System.out.println(
                                        "URI: " +
                                                        requestUri);

                        System.out.println(
                                        "Mensaje: " +
                                                        exception.getMessage());

                        exception.printStackTrace();

                        System.out.println(
                                        "========================================");

                        SecurityContextHolder.clearContext();
                }

                // =====================================================
                // CONTINUAR CADENA
                // =====================================================

                filterChain.doFilter(
                                request,
                                response);
        }
}