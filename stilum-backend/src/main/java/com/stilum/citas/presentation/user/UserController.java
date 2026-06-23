package com.stilum.citas.presentation.user;

import com.stilum.citas.application.user.UserService;
import com.stilum.citas.application.user.dto.CreateUserRequest;
import com.stilum.citas.application.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints de usuarios del tenant — accesible por ADMIN_TENANT.
 * RN-TENANT-001: TenantContext garantiza que solo ve sus propios usuarios.
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('ADMIN_TENANT', 'SUPER_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Gestión de usuarios (profesionales y admins) del tenant")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios del tenant actual")
    public ResponseEntity<List<UserResponse>> listar() {
        return ResponseEntity.ok(userService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un usuario")
    public ResponseEntity<UserResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.obtener(id));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo usuario en el tenant")
    public ResponseEntity<UserResponse> crear(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.crear(req));
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar usuario")
    public ResponseEntity<UserResponse> desactivar(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.desactivar(id));
    }

    @PatchMapping("/{id}/activar")
    @Operation(summary = "Reactivar usuario desactivado")
    public ResponseEntity<UserResponse> activar(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.activar(id));
    }
}
