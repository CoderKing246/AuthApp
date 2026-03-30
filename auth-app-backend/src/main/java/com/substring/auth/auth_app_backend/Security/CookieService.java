package com.substring.auth.auth_app_backend.Security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import java.lang.String;

@Service
@Getter
public class CookieService {


    private final String refreshTokenCookieName;

    private final boolean cookieSecure;

    private final boolean cookieHttpOnly;

    private final String cookieDomain;

    private final String cookieSameSite;

    public CookieService(
            @Value("${jwt.refresh-token.cookie-name}") String refreshTokenCookieName,
            @Value("${jwt.cookie-secure}") boolean cookieSecure,
            @Value("${jwt.cookie-http-only}") boolean cookieHttpOnly,
            @Value("${jwt.cookie-domain}") String cookieDomain,
            @Value("${jwt.cookie-same-site}") String cookieSameSite
    ) {
        this.refreshTokenCookieName = refreshTokenCookieName;
        this.cookieSecure = cookieSecure;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieDomain = cookieDomain;
        this.cookieSameSite = cookieSameSite;
    }


    // create method to attach cookie to response.
    public void attachRefreshCookie(HttpServletResponse response,String value,int maxAge){
        var responseCookieBuilder = ResponseCookie.from(refreshTokenCookieName,value)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(cookieSameSite);
        if(cookieDomain!=null && !cookieDomain.isBlank()){
            responseCookieBuilder.domain(cookieDomain);
        }
        ResponseCookie responseCookie = responseCookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE,responseCookie.toString());
    }

    // clear refresh cookie
    public void clearRefreshCookie(HttpServletResponse response){
        var responseCookieBuilder = ResponseCookie.from(refreshTokenCookieName,"")
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite);
        if(cookieDomain!=null && !cookieDomain.isBlank()){
            responseCookieBuilder.domain(cookieDomain);
        }
        ResponseCookie responseCookie = responseCookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE,responseCookie.toString());
    }

    public void addNoStoreHeaders(HttpServletResponse response){
        response.setHeader(HttpHeaders.CACHE_CONTROL,"no-store");
        response.setHeader("Pragma","no-cache");
    }
}
