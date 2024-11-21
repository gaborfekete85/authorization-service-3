package com.infra.authorization.services.jwt;

import com.infra.authorization.model.UserDetail;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.UUID;

public interface JWTService {
    String generateToken(UserDetails userDetail);
    String createToken(UserDetails userDetail);
    String generateRefreshToken(Map<String, Object> extraClaims, UserDetail userDetail);
    String getUserName(String token);

    Map<String, Object> getClaims(String token);
    UUID getUserId(String token);

    boolean isTokenValid(String token, UserDetails userDetails);

    boolean validateToken(String authToken);
}
