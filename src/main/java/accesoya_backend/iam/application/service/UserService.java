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

import accesoya_backend.iam.infrastructure.security.JwtService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

        private final UserRepository userRepository;

        private final PasswordEncoder passwordEncoder;

        private final JwtService jwtService;

        // =====================================================
        // REGISTRO
        // =====================================================

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

                                // =============================================
                                // TODO NUEVO USUARIO
                                // =============================================
                                // Todo usuario registrado comienza como CONSULTOR.
                                // Solo un ADMIN puede elevar posteriormente
                                // su nivel de acceso.
                                .role(Role.CONSULTOR)

                                .status(UserStatus.ACTIVE)
                                .build();

                User savedUser = userRepository.save(user);

                return UserResponse.from(savedUser);
        }

        // =====================================================
        // LOGIN
        // =====================================================

        @Transactional(readOnly = true)
        public AuthResponse login(
                        LoginRequest request) {

                String normalizedEmail = request.email()
                                .trim()
                                .toLowerCase();

                User user = userRepository
                                .findByEmail(normalizedEmail)
                                .orElseThrow(
                                                () -> new IllegalArgumentException(
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
                                .findById(
                                                authenticatedUser.getId())
                                .orElseThrow(
                                                () -> new IllegalArgumentException(
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

        // =====================================================
        // MI PERFIL
        // =====================================================

        @Transactional(readOnly = true)
        public UserResponse getMyProfile(
                        Authentication authentication) {

                User authenticatedUser = (User) authentication.getPrincipal();

                User user = userRepository
                                .findById(
                                                authenticatedUser.getId())
                                .orElseThrow(
                                                () -> new IllegalArgumentException(
                                                                "Usuario no encontrado"));

                return UserResponse.from(user);
        }

        // =====================================================
        // ACTUALIZAR MI PERFIL
        // =====================================================

        @Transactional
        public UserResponse updateMyProfile(
                        Authentication authentication,
                        UpdateProfileRequest request) {

                User authenticatedUser = (User) authentication.getPrincipal();

                User user = userRepository
                                .findById(
                                                authenticatedUser.getId())
                                .orElseThrow(
                                                () -> new IllegalArgumentException(
                                                                "Usuario no encontrado"));

                user.setFirstName(
                                request.firstName().trim());

                user.setLastName(
                                request.lastName().trim());

                return UserResponse.from(user);
        }

        // =====================================================
        // LISTAR USUARIOS
        // SOLO ADMIN
        // =====================================================

        @Transactional(readOnly = true)
        public List<UserResponse> getAllUsers() {

                return userRepository
                                .findAll()
                                .stream()
                                .map(UserResponse::from)
                                .toList();
        }

        // =====================================================
        // OBTENER USUARIO
        // SOLO ADMIN
        // =====================================================

        @Transactional(readOnly = true)
        public UserResponse getUserById(
                        UUID id) {

                User user = userRepository
                                .findById(id)
                                .orElseThrow(
                                                () -> new IllegalArgumentException(
                                                                "Usuario no encontrado"));

                return UserResponse.from(user);
        }

        // =====================================================
        // CAMBIAR ROL
        // SOLO ADMIN
        // =====================================================

        @Transactional
        public UserResponse updateRole(
                        UUID id,
                        UpdateUserRoleRequest request,
                        Authentication authentication) {

                if (authentication == null ||
                                !(authentication.getPrincipal() instanceof User authenticatedUser)) {

                        throw new SecurityException(
                                        "Usuario no autenticado");
                }

                // =====================================================
                // NO PERMITIR QUE EL ADMIN SE MODIFIQUE A SÍ MISMO
                // =====================================================

                if (authenticatedUser.getId().equals(id)) {

                        throw new IllegalArgumentException(
                                        "Un administrador no puede modificar su propio rol");
                }

                // =====================================================
                // BUSCAR USUARIO
                // =====================================================

                User user = userRepository
                                .findById(id)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Usuario no encontrado"));

                // =====================================================
                // ACTUALIZAR ROL
                // =====================================================

                user.setRole(request.role());

                return UserResponse.from(user);
        }

        // =====================================================
        // CAMBIAR ESTADO
        // SOLO ADMIN
        // =====================================================

        @Transactional
        public UserResponse updateStatus(

                        UUID id,

                        UpdateUserStatusRequest request,

                        Authentication authentication) {

                User authenticatedUser = (User) authentication.getPrincipal();

                // -------------------------------------------------
                // NO MODIFICAR PROPIA CUENTA
                // -------------------------------------------------

                if (authenticatedUser
                                .getId()
                                .equals(id)) {

                        throw new IllegalArgumentException(
                                        "Un administrador no puede modificar su propio estado");
                }

                User user = userRepository
                                .findById(id)
                                .orElseThrow(
                                                () -> new IllegalArgumentException(
                                                                "Usuario no encontrado"));

                // -------------------------------------------------
                // PROTEGER ÚLTIMO ADMIN
                // -------------------------------------------------

                if (

                user.getRole() == Role.ADMIN

                                && user.getStatus() == UserStatus.ACTIVE

                                && request.status() == UserStatus.INACTIVE

                                && userRepository
                                                .countByRoleAndStatus(
                                                                Role.ADMIN,
                                                                UserStatus.ACTIVE) <= 1

                ) {

                        throw new IllegalArgumentException(
                                        "No se puede desactivar el último administrador activo del sistema");
                }

                user.setStatus(
                                request.status());

                return UserResponse.from(user);
        }
}