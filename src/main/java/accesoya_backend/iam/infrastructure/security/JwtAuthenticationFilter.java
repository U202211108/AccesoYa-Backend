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

                        FilterChain filterChain

        ) throws ServletException, IOException {

                String requestUri = request.getRequestURI();

                String method = request.getMethod();

                System.out.println();
                System.out.println("========================================");
                System.out.println("JWT FILTER");
                System.out.println("Método: " + method);
                System.out.println("URI: " + requestUri);
                System.out.println("========================================");

                // =====================================================
                // AUTHORIZATION HEADER
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
                // BEARER
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
                // TOKEN
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

                        if (!jwtService.isTokenValid(token)) {

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
                        // USER ID
                        // =================================================

                        String userId = jwtService.extractUserId(token);

                        System.out.println(
                                        "JWT: User ID extraído: " +
                                                        userId);

                        UUID uuid;

                        try {

                                uuid = UUID.fromString(userId);

                        } catch (IllegalArgumentException exception) {

                                System.out.println(
                                                "JWT ERROR: subject inválido.");

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
                                                "JWT ERROR: Usuario no encontrado.");

                                SecurityContextHolder.clearContext();

                                filterChain.doFilter(
                                                request,
                                                response);

                                return;
                        }

                        // =================================================
                        // ESTADO
                        // =================================================

                        if (user.getStatus() != UserStatus.ACTIVE) {

                                System.out.println(
                                                "JWT ERROR: Usuario INACTIVE.");

                                SecurityContextHolder.clearContext();

                                filterChain.doFilter(
                                                request,
                                                response);

                                return;
                        }

                        // =================================================
                        // AUTHORITY
                        // =================================================

                        String role = user.getRole().name();

                        String authorityName = "ROLE_" + role;

                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                                        authorityName);

                        // =================================================
                        // AUTHENTICATION
                        // =================================================

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
                        // DEBUG
                        // =================================================

                        System.out.println(
                                        "JWT: Usuario autenticado: " +
                                                        user.getEmail());

                        System.out.println(
                                        "JWT: Rol BD: " +
                                                        role);

                        System.out.println(
                                        "JWT: Authority: " +
                                                        authorityName);

                        System.out.println(
                                        "JWT: Authorities actuales: " +
                                                        authentication.getAuthorities());

                        System.out.println(
                                        "JWT: Authentication creada correctamente.");

                        System.out.println(
                                        "========================================");

                } catch (Exception exception) {

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

                filterChain.doFilter(
                                request,
                                response);
        }
}