package com.substring.auth.auth_app_backend.Controller;

import com.substring.auth.auth_app_backend.Dtos.UpdatedUserDto;
import com.substring.auth.auth_app_backend.Dtos.UserDto;
import com.substring.auth.auth_app_backend.Service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController {
    private final UserService userService;

    /**
     * Get current authenticated user
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        UserDto userDto = userService.getCurrentUser();
        return ResponseEntity.ok(userDto);
    }

    /**
     * Get all users with pagination
     * Query parameters: page (default: 0), size (default: 20), sortBy (default: createdAt), sortDir (asc/desc)
     */
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<UserDto> users = userService.getAllUsers(page, size, sortBy, sortDir);
        return ResponseEntity.ok(users);
    }

    /**
     * Get all users (backward compatibility - returns list)
     */
    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUserList() {
        List<UserDto> users = userService.getAllUser();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID id) {
        UserDto userDto = userService.getUser(id);
        return ResponseEntity.ok(userDto);
    }

    /**
     * Get user by email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        UserDto userDto = userService.getUserByEmail(email);
        return ResponseEntity.ok(userDto);
    }

    /**
     * Update user by ID
     */
    @PutMapping("/edit/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatedUserDto updatedUserDto) {
        UserDto userDto = userService.editUser(id, updatedUserDto);
        return ResponseEntity.ok(userDto);
    }

    /**
     * Delete user by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if user exists
     */
    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> userExists(@PathVariable UUID id) {
        boolean exists = userService.userExists(id);
        return ResponseEntity.ok(exists);
    }
}
