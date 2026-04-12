package com.eams.auth.infrastructure.http;

import com.eams.auth.application.UserManagementService;
import com.eams.auth.application.dto.RegisterUserRequest;
import com.eams.auth.application.dto.UpdateUserRequest;
import com.eams.auth.application.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Adaptador HTTP para gestión de usuarios (Phase 1.3).
 *
 * Endpoints:
 *   POST  /users              — registrar usuario
 *   GET   /users/{userId}     — obtener perfil
 *   PATCH /users/{userId}     — actualizar perfil
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserManagementService userManagementService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        return userManagementService.register(request);
    }

    @GetMapping("/{userId}")
    public UserResponse getProfile(@PathVariable UUID userId) {
        return userManagementService.getProfile(userId);
    }

    @PatchMapping("/{userId}")
    public UserResponse updateUser(
            @PathVariable UUID userId,
            @RequestBody UpdateUserRequest request) {
        return userManagementService.updateUser(userId, request);
    }
}
