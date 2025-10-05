//package com.url.shorterner.security.jwt;
//
//import com.url.shorterner.service.UserDetailsImpl;
//import io.jsonwebtoken.Jwt;
//import io.jsonwebtoken.JwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.security.Key;
//import java.util.Date;
//import java.util.stream.Collectors;
//
//@Component
//public class JwtUtils {
//    @Value("${jwt.secret}")
//    private String jwtSecret;
//    @Value("${jwt.expiration}")
//    private String jwtExpirationMs;
//    public String getJWTFromHeader(HttpServletRequest request) {
//        String bearerToken = request.getHeader("Authorization");
//        if(bearerToken != null && bearerToken.startsWith("Bearer ")) {
//            bearerToken.substring(7);
//        }
//        return null;
//    }
//
//    public String generateToken(UserDetailsImpl userDetails) {
//        String username = userDetails.getUsername();
//        String roles = userDetails.getAuthorities().stream()
//                .map(authority -> authority.getAuthority())
//                .collect(Collectors.joining(","));
//        return Jwts.builder()
//                .subject(username)
//                .claim("roles", roles)
//                .issuedAt(new Date())
//                .expiration(new Date((new Date().getTime() + jwtExpirationMs)))
//                .signWith(key())
//                .compact();
//    }
//
//    public String getUserNameFromJwtToken(String token) {
//        return Jwts.parser()
//                .verifyWith((SecretKey) key())
//                .build().parseSignedClaims(token)
//                .getPayload().getSubject();
//    }
//
//    private Key key(){
//        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
//    }
//
//    public boolean validateToken(String authToken) {
//        try {
//            Jwts.parser().verifyWith((SecretKey) key())
//                    .build().parseSignedClaims(authToken);
//            return true;
//        } catch (JwtException e) {
//            throw new RuntimeException(e);
//        } catch (IllegalArgumentException e) {
//            throw new RuntimeException(e);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
package com.url.shorterner.security.jwt;

import com.url.shorterner.service.UserDetailsImpl;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs; // ✅ must be long

    public String getJWTFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // ✅ return actual token
        }
        return null;
    }

    public String generateToken(UserDetailsImpl userDetails) {
        String username = userDetails.getUsername();
        String roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(username) // ✅ changed
                .claim("roles", roles)
                .setIssuedAt(new Date()) // ✅ changed
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // ✅ fixed
                .signWith(key())
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key()) // ✅ changed
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key()) // ✅ changed
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }
}