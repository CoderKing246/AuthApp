package com.substring.auth.auth_app_backend.Controller;

import com.substring.auth.auth_app_backend.Dtos.LoginDto;
import com.substring.auth.auth_app_backend.Dtos.RefreshTokenRequest;
import com.substring.auth.auth_app_backend.Dtos.RegisterDto;
import com.substring.auth.auth_app_backend.Repository.UserRepository;
import com.substring.auth.auth_app_backend.Service.UserService;
import com.substring.auth.auth_app_backend.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

    private UserService userService;
    private UserRepository userRepository;


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDto loginDto, HttpServletResponse response){
        return ResponseEntity.ok(userService.authenicateUser(loginDto,response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response){
        return ResponseEntity.ok(userService.logout(request,response));
    }

    // refresh token regenerate and validate api
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest refreshTokenRequest ,
            HttpServletResponse response,
            HttpServletRequest request
    ){
        return ResponseEntity.ok(userService.refreshTokenRegenerate(refreshTokenRequest,response,request));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterDto registerDto){
        if(userRepository.existsByEmail(registerDto.getEmail())){
            throw new RuntimeException("Email already exists");
        }
        User user = new User();
        user.setName(registerDto.getName());
        user.setEmail(registerDto.getEmail());
        user.setPassword(registerDto.getPassword());

        userService.registerUser(user);
    return ResponseEntity.ok("Successfully created user with email : "+registerDto.getEmail());
    }

}
