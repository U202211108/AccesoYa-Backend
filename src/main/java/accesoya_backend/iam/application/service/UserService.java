package accesoya_backend.iam.application.service;

import accesoya_backend.iam.application.dto.AuthResponse;
import accesoya_backend.iam.application.dto.LoginRequest;
import accesoya_backend.iam.application.dto.RegisterRequest;
import accesoya_backend.iam.application.dto.UpdateProfileRequest;
import accesoya_backend.iam.application.dto.UpdateUserRoleRequest;
import accesoya_backend.iam.application.dto.UpdateUserStatusRequest;
import accesoya_backend.iam.application.dto.UserResponse;
import accesoya_backend.iam.domain.model.Role;
import accesoya_backend.iam.domain.model.User;
import accesoya_backend.iam.domain.model.UserStatus;
import accesoya_backend.iam.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import accesoya_backend.iam.infrastructure.security.JwtService;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;

        @Transactional
        public UserResponse register(RegisterRequest request) {

                String normalizedEmail = request.email()
                                .trim()
                                .toLowerCase();

                if (userRepository.existsByEmail(normalizedEmail)) {
                        throw new IllegalArgumentException(
                                        "El correo electrónico ya está registrado");
                }

                User user = User.builder()
                                .firstName(request.firstName().trim())
                                .lastName(request.lastName().trim())
                                .email(normalizedEmail)
                                .password(passwordEncoder.encode(request.password()))
                                .role(Role.USER)
                                .status(UserStatus.ACTIVE)
                                .build();

                User savedUser = userRepository.save(user);

                return UserResponse.from(savedUser);
        }

        @Transactional(readOnly = true)
        public AuthResponse login(LoginRequest request) {

                String normalizedEmail = request.email()
                                .trim()
                                .toLowerCase();

                User user = userRepository.findByEmail(normalizedEmail)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Credenciales inválidas"));

                if (user.getStatus() != UserStatus.ACTIVE) {
                        throw new IllegalArgumentException(
                                        "El usuario se encuentra inactivo");
                }

                if (!passwordEncoder.matches(
                                request.password(),
                                user.getPassword())) {
                        throw new IllegalArgumentException(
                                        "Credenciales inválidas");
                }

                String token = jwtService.generateToken(user);

                return new AuthResponse(
                                token,
                                user.getId(),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getEmail(),
                                user.getRole());
        }

        // =====================================================
        // REFRESCAR SESIÓN
        // =====================================================

        @Transactional(readOnly = true)
        public AuthResponse refreshSession(
                        Authentication authentication) {

                if (authentication == null ||
                                authentication.getPrincipal() == null) {

                        throw new IllegalArgumentException(
                                        "Usuario no autenticado");
                }

                User authenticatedUser = (User) authentication.getPrincipal();

                User user = userRepository
                                .findById(authenticatedUser.getId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Usuario no encontrado"));

                if (user.getStatus() != UserStatus.ACTIVE) {

                        throw new IllegalArgumentException(
                                        "El usuario se encuentra inactivo");
                }

                String token = jwtService.generateToken(user);

                return new AuthResponse(
                                token,
                                user.getId(),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getEmail(),
                                user.getRole());
        }

        @Transactional(readOnly = true)
        public UserResponse getMyProfile(
                        Authentication authentication) {

                User authenticatedUser = (User) authentication.getPrincipal();

                User user = userRepository
                                .findById(authenticatedUser.getId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Usuario no encontrado"));

                return UserResponse.from(user);
        }

        @Transactional
        public UserResponse updateMyProfile(
                        Authentication authentication,
                        UpdateProfileRequest request) {

                User authenticatedUser = (User) authentication.getPrincipal();

                User user = userRepository
                                .findById(authenticatedUser.getId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Usuario no encontrado"));

                user.setFirstName(
                                request.firstName().trim());

                user.setLastName(
                                request.lastName().trim());

                return UserResponse.from(user);
        }

        @Transactional(readOnly = true)
        public List<UserResponse> getAllUsers() {

                return userRepository.findAll()
                                .stream()
                                .map(UserResponse::from)
                                .toList();
        }

        @Transactional(readOnly = true)
        public UserResponse getUserById(UUID id) {

                User user = userRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Usuario no encontrado"));

                return UserResponse.from(user);
        }

        @Transactional
        public UserResponse updateRole(
                        UUID id,
                        UpdateUserRoleRequest request,
                        Authentication authentication) {

                User authenticatedUser = (User) authentication.getPrincipal();

                if (authenticatedUser.getId().equals(id)) {
                        throw new IllegalArgumentException(
                                        "Un administrador no puede modificar su propio rol");
                }

                User user = userRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Usuario no encontrado"));

                user.setRole(request.role());

                return UserResponse.from(user);
        }

        @Transactional
        public UserResponse updateStatus(
                        UUID id,
                        UpdateUserStatusRequest request,
                        Authentication authentication) {

                User authenticatedUser = (User) authentication.getPrincipal();

                if (authenticatedUser.getId().equals(id)) {
                        throw new IllegalArgumentException(
                                        "Un administrador no puede modificar su propio estado");
                }

                User user = userRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Usuario no encontrado"));

                user.setStatus(request.status());

                return UserResponse.from(user);
        }
}