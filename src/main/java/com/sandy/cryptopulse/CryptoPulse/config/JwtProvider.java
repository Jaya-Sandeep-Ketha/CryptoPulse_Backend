package com.sandy.cryptopulse.CryptoPulse.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.util.*;

public class JwtProvider {
    private static SecretKey key = Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());
    public static String generateToken(Authentication auth){
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        String roles = populateAuthorities(authorities);
        String jwt = Jwts.builder().setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 86400000))  // Correct expiration
                .claim("email", auth.getName()).claim("authorities",roles)
                .signWith(key).compact();
        return jwt;
    }

    public static String getEmailFromToken(String token){
        token = token.substring(7);
        Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        String email = String.valueOf(claims.get("email"));
        return email;
    }

    private static String populateAuthorities(Collection<? extends GrantedAuthority> authorities){
        Set<String> auth = new HashSet<>();
        for(GrantedAuthority authority : authorities){
            auth.add(authority.getAuthority());
        }
        return String.join(",", auth);
    }
}
