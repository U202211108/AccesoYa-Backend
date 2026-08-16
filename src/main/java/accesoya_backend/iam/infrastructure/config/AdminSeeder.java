package accesoya_backend.iam.infrastructure.config;

import accesoya_backend.iam.domain.model.Role;
import accesoya_backend.iam.domain.model.User;
import accesoya_backend.iam.domain.model.UserStatus;
import accesoya_backend.iam.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner createAdmin() {

        return args -> {

            String adminEmail = "admin@accesoya.com";

            if (userRepository.existsByEmail(adminEmail)) {
                return;
            }

            User admin = User.builder()
                    .firstName("AccesoYa")
                    .lastName("Administrador")
                    .email(adminEmail)
                    .password(
                            passwordEncoder.encode("Admin12345"))
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();

            userRepository.save(admin);

            System.out.println(
                    "Usuario administrador creado: "
                            + adminEmail);
        };
    }
}