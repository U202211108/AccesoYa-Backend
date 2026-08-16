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
            FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {

            if (!jwtService.isTokenValid(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            String userId = jwtService.extractUserId(token);

            User user = userRepository
                    .findById(UUID.fromString(userId))
                    .orElse(null);

            if (user != null &&
                    user.getStatus() == UserStatus.ACTIVE) {

                var authority = new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name());

                var authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(authority));

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);

                System.out.println("========================================");
                System.out.println(
                        "USUARIO AUTENTICADO: " +
                                user.getEmail());
                System.out.println(
                        "ROL BD: " +
                                user.getRole());
                System.out.println(
                        "AUTHORITY: ROLE_" +
                                user.getRole().name());
                System.out.println("========================================");
            }

        } catch (Exception exception) {

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}