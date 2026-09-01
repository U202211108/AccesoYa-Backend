package accesoya_backend.iam.interfaces.rest;

import accesoya_backend.iam.application.dto.UpdateUserRoleRequest;
import accesoya_backend.iam.application.dto.UpdateUserStatusRequest;
import accesoya_backend.iam.application.dto.UserResponse;
import accesoya_backend.iam.application.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Administration", description = "Administración de usuarios de AccesoYa")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

        private final UserService userService;

        // =====================================================
        // LISTAR USUARIOS
        // =====================================================

        @GetMapping
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Listar usuarios")
        public ResponseEntity<List<UserResponse>> getAllUsers() {

                return ResponseEntity.ok(
                                userService.getAllUsers());
        }

        // =====================================================
        // OBTENER USUARIO
        // =====================================================

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        public ResponseEntity<UserResponse> getUserById(
                        @PathVariable UUID id) {

                return ResponseEntity.ok(
                                userService.getUserById(id));
        }

        // =====================================================
        // CAMBIAR ROL
        // =====================================================

        @PatchMapping("/{id}/role")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        public ResponseEntity<UserResponse> updateRole(

                        @PathVariable UUID id,

                        @Valid @RequestBody UpdateUserRoleRequest request,

                        Authentication authentication

        ) {

                return ResponseEntity.ok(

                                userService.updateRole(
                                                id,
                                                request,
                                                authentication));
        }

        // =====================================================
        // CAMBIAR ESTADO
        // =====================================================

        @PatchMapping("/{id}/status")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        public ResponseEntity<UserResponse> updateStatus(

                        @PathVariable UUID id,

                        @Valid @RequestBody UpdateUserStatusRequest request,

                        Authentication authentication

        ) {

                return ResponseEntity.ok(

                                userService.updateStatus(
                                                id,
                                                request,
                                                authentication));
        }
}