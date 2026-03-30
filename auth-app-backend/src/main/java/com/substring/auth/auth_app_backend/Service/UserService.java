package com.substring.auth.auth_app_backend.Service;

import com.substring.auth.auth_app_backend.Dtos.LoginDto;
import com.substring.auth.auth_app_backend.Dtos.RefreshTokenRequest;
import com.substring.auth.auth_app_backend.Dtos.UpdatedUserDto;
import com.substring.auth.auth_app_backend.Dtos.UserDto;
import com.substring.auth.auth_app_backend.Exception.BadRequestException;
import com.substring.auth.auth_app_backend.Exception.ResourceNotFoundException;
import com.substring.auth.auth_app_backend.Repository.RefreshTokenRepository;
import com.substring.auth.auth_app_backend.Repository.RoleRepository;
import com.substring.auth.auth_app_backend.Repository.UserRepository;
import com.substring.auth.auth_app_backend.Security.CookieService;
import com.substring.auth.auth_app_backend.Security.jwt.JwtAuthenticationResponse;
import com.substring.auth.auth_app_backend.Security.jwt.JwtUtils;
import com.substring.auth.auth_app_backend.model.RefreshToken;
import com.substring.auth.auth_app_backend.model.Role;
import com.substring.auth.auth_app_backend.model.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtUtils jwtUtils;
    private RefreshTokenRepository refreshTokenRepository;
    private CookieService cookieService;

    public void registerUser(User user) {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(()->new RuntimeException("Error : Role not found"));
        user.setRoles(Set.of(userRole));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public JwtAuthenticationResponse authenicateUser(LoginDto loginDto, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(),loginDto.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User userDetails = (User) authentication.getPrincipal();

        //jti for refresh token
        String jti = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .jti(jti)
                .user(userDetails)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtUtils.getRefreshExpiration()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        String refresh = jwtUtils.generateRefreshToken(userDetails,refreshToken.getJti());

        // use cookie service
        cookieService.attachRefreshCookie(response,refresh,(int)jwtUtils.getRefreshExpiration());
        cookieService.addNoStoreHeaders(response);
        // access token generate
        String jwtToken = jwtUtils.generateAccessToken(userDetails);
        return new JwtAuthenticationResponse(jwtToken,refresh,jwtUtils.getJwtExpirationMs(),"Bearer",mapToUserDto(userDetails));

    }

    public JwtAuthenticationResponse refreshTokenRegenerate(
            RefreshTokenRequest refreshTokenRequest,
            HttpServletResponse response, HttpServletRequest request) {

            String refreshToken = readRefreshTokenFromRequest(refreshTokenRequest,request)
                    .orElseThrow(()-> new BadCredentialsException("Refresh token is missing"));

            if(!jwtUtils.isRefreshToken(refreshToken)){
                throw new BadCredentialsException("Invalid Refresh token type");
            }

            String jti = jwtUtils.getJtiFromToken(refreshToken);
            UUID userId = jwtUtils.getUserIdFromJwtToken(refreshToken);
            RefreshToken storedRefreshToken = refreshTokenRepository.findByJti(jti)
                    .orElseThrow(()-> new BadCredentialsException("refresh token not recognized"));

            if(storedRefreshToken.isRevoked()){
                throw new BadCredentialsException("Refresh token is Revoked");
            }
            if(storedRefreshToken.getExpiresAt().isBefore(LocalDateTime.now())){
                throw new BadCredentialsException(("Refresh token is expired"));
            }

            if(!storedRefreshToken.getUser().getId().equals(userId)){
                throw new BadCredentialsException("Refresh token is not belongs to this user");
            }

            //Refresh token is rotated
            storedRefreshToken.setRevoked(true);
            String newJti = UUID.randomUUID().toString();
            storedRefreshToken.setReplacedByToken(newJti);
            refreshTokenRepository.save(storedRefreshToken);

            User user = storedRefreshToken.getUser();
            var newRefreshToken = RefreshToken.builder()
                    .jti(newJti)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusSeconds(jwtUtils.getRefreshExpiration()))
                    .revoked(false)
                    .build();
            refreshTokenRepository.save(newRefreshToken);
            String newAccessToken = jwtUtils.generateAccessToken(user);
            String newRefresh = jwtUtils.generateRefreshToken(user,newRefreshToken.getJti());

            cookieService.attachRefreshCookie(response,newRefresh,(int)jwtUtils.getRefreshExpiration());
            cookieService.addNoStoreHeaders(response);

            return new JwtAuthenticationResponse(newAccessToken,newRefresh, jwtUtils.getJwtExpirationMs(),"Bearer",mapToUserDto(user));
    }

    // this method will read refresh token from request header or body
    private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest refreshTokenRequest,
                                                 HttpServletRequest request) {
        // 1. prefer reading refresh token from cookie
        if(request.getCookies()!=null){
            Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter(c->cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v-> !v.isBlank()).
                    findFirst();
            if(fromCookie.isPresent()){
                return fromCookie;
            }
        }
        // 2. body(refreshTokenRequest)
        if(refreshTokenRequest!=null &&
                refreshTokenRequest.getRefrshToken()!=null &&
                !refreshTokenRequest.getRefrshToken().isBlank()){
            return Optional.of(refreshTokenRequest.getRefrshToken());
        }

        //3. custom header
        String refreshHeader = request.getHeader("X-Refresh-Token");
        if(refreshHeader!=null && !refreshHeader.isBlank()){
            return Optional.of(refreshHeader.trim());
        }

        //4. Authorization = Bearer <Refresh Token
        String authHeader = request.getHeader("Authorization");
        if(authHeader!=null && authHeader.startsWith("Bearer ")){
            String candidate = authHeader.substring(7).trim();
            if(!candidate.isEmpty()){
                try{
                    if(jwtUtils.isRefreshToken(candidate)){
                        return Optional.of(candidate);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return Optional.empty();
    }

    public Void logout(HttpServletRequest request, HttpServletResponse response) {
        readRefreshTokenFromRequest(null,request).ifPresent(
                token ->{
                    try {
                        if(jwtUtils.isRefreshToken(token)){
                            String jti = jwtUtils.getJtiFromToken(token);
                            refreshTokenRepository.findByJti(jti).ifPresent(
                                    rt->{
                                        rt.setRevoked(true);
                                        refreshTokenRepository.save(rt);
                                    }
                            );
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        return null;
    }

    // CRUD operation

    /**
     * Get current authenticated user
     */
    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("User is not authenticated");
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        return mapToUserDto(user);
    }

    /**
     * Get user by ID with proper error handling
     */
    public UserDto getUser(UUID id) {
        if (id == null) {
            throw new BadRequestException("User ID cannot be null");
        }
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        return mapToUserDto(user);
    }

    /**
     * Get all users with pagination and sorting
     */
    public Page<UserDto> getAllUsers(int page, int size, String sortBy, String sortDir) {
        // Validate pagination parameters
        if (page < 0) {
            throw new BadRequestException("Page number must be non-negative");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }

        // Default sorting
        Sort sort = sortDir != null && sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy != null ? sortBy : "createdAt").descending()
                : Sort.by(sortBy != null ? sortBy : "createdAt").ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.findAll(pageable);
        
        return userPage.map(this::mapToUserDto);
    }

    /**
     * Get all users (backward compatibility - returns first 100)
     */
    public List<UserDto> getAllUser() {
        Page<UserDto> userPage = getAllUsers(0, 100, "createdAt", "desc");
        return userPage.getContent();
    }

    /**
     * Update user with validation and proper error handling
     */
    @Transactional
    public UserDto editUser(UUID userId, UpdatedUserDto updatedUserDto) {
        if (userId == null) {
            throw new BadRequestException("User ID cannot be null");
        }
        
        if (updatedUserDto == null) {
            throw new BadRequestException("Update data cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Validate and update name
        if (updatedUserDto.getName() != null) {
            String trimmedName = updatedUserDto.getName().trim();
            if (trimmedName.length() < 2) {
                throw new BadRequestException("Name must be at least 2 characters long");
            }
            if (trimmedName.length() > 100) {
                throw new BadRequestException("Name must not exceed 100 characters");
            }
            user.setName(trimmedName);
        }

        // Validate and update image URL
        if (updatedUserDto.getImage() != null) {
            String trimmedImage = updatedUserDto.getImage().trim();
            if (!trimmedImage.isEmpty()) {
                // Basic URL validation
                if (!isValidUrl(trimmedImage)) {
                    throw new BadRequestException("Image must be a valid URL");
                }
                user.setImage(trimmedImage);
            } else {
                user.setImage(null);
            }
        }

        // Validate and update date of birth
        if (updatedUserDto.getDateOfBirth() != null) {
            LocalDate dob = updatedUserDto.getDateOfBirth();
            // Validate date is not in the future
            if (dob.isAfter(LocalDate.now())) {
                throw new BadRequestException("Date of birth cannot be in the future");
            }
            // Validate reasonable age (e.g., at least 13 years old, max 150 years old)
            LocalDate minDate = LocalDate.now().minusYears(150);
            LocalDate maxDate = LocalDate.now().minusYears(13);
            if (dob.isBefore(minDate) || dob.isAfter(maxDate)) {
                throw new BadRequestException("Date of birth must be between 13 and 150 years ago");
            }
            user.setDateOfBirth(dob);
        }

        // Update timestamp will be handled by @UpdateTimestamp
        User savedUser = userRepository.save(user);
        return mapToUserDto(savedUser);
    }

    /**
     * Delete user by ID
     */
    @Transactional
    public void deleteUser(UUID userId) {
        if (userId == null) {
            throw new BadRequestException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Revoke all refresh tokens for this user
        List<RefreshToken> refreshTokens = refreshTokenRepository.findByUser(user);
        refreshTokens.forEach(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });

        // Delete the user
        userRepository.delete(user);
    }

    /**
     * Check if user exists by ID
     */
    public boolean userExists(UUID userId) {
        if (userId == null) {
            return false;
        }
        return userRepository.existsById(userId);
    }

    /**
     * Get user by email
     */
    public UserDto getUserByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BadRequestException("Email cannot be empty");
        }

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return mapToUserDto(user);
    }

    /**
     * Map User entity to UserDto
     */
    private UserDto mapToUserDto(User user) {
        if (user == null) {
            throw new BadRequestException("User cannot be null");
        }

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setImage(user.getImage());
        userDto.setDateOfBirth(user.getDateOfBirth());
        userDto.setProvider(user.getProvider());
        userDto.setRoles(user.getRoles() != null ? user.getRoles() : new HashSet<>());
        
        return userDto;
    }

    /**
     * Basic URL validation helper
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            // Check if it starts with http:// or https://
            String lowerUrl = url.toLowerCase();
            if (lowerUrl.startsWith("http://") || lowerUrl.startsWith("https://")) {
                // Basic validation - contains at least one dot
                return url.contains(".");
            }
            // Allow relative URLs starting with /
            if (url.startsWith("/")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }



}
