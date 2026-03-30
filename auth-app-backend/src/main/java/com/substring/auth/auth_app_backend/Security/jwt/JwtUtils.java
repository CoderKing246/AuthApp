package com.substring.auth.auth_app_backend.Security.jwt;

import com.substring.auth.auth_app_backend.model.Role;
import com.substring.auth.auth_app_backend.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Data
public class JwtUtils {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token.expiration}")
    private Long jwtExpirationMs;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value(("${jwt.refresh-token.expiration}"))
    private long refreshExpiration;

    public String getJwtFromHeader(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

    // parse the token
    public Jws<Claims> parse(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token);
    };

    public boolean isAccessToken(String token){
        Claims c = parse(token).getPayload();
        return "access".equals(c.get("typ"));
    }

    public boolean isRefreshToken(String token){
        Claims c = parse(token).getPayload();
        return "refresh".equals(c.get("typ"));
    }

    public UUID getUserIdFromJwtToken(String token){
        Claims claims = parse(token)
                .getPayload();
            return UUID.fromString(claims.getSubject());

    }

    // get id of token(refresh)
    public String getJtiFromToken(String token){
        return parse(token).getPayload().getId();
    }

    // builder
    public String generateAccessToken(User user){
        List<String> roles = user.getRoles()== null ? List.of():
                user.getRoles().stream().map(Role::getName).toList();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime() + jwtExpirationMs )))
                .issuer(issuer)
                .claims(Map.of(
                        "email",user.getEmail(),
                        "roles",roles,
                        "typ","access"
                ))
                .signWith(key())
                .compact();


    }

    // generate refresh token
    public String generateRefreshToken(User user,String jti){
        return Jwts.builder()
                .id(jti)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime() + refreshExpiration )))
                .claim("typ","refresh")
                .signWith(key())
                .compact();
    }

    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtSecret));
    }

    public boolean validateToken(String token) {
        try {
            parse(token).getPayload();
            return true;
        } catch (ExpiredJwtException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException(e);
        } catch (MalformedJwtException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
}
