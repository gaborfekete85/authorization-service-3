package com.infra.authorization.services.jwt;

import com.infra.authorization.model.UserDetail;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface JWTService {
    String generateToken(UserDetail userDetail);
    String createToken(UserDetail userDetail);
    String generateRefreshToken(Map<String, Object> extraClaims, UserDetail userDetail);
    String getUserName(String token);

    boolean isTokenValid(String token, UserDetails userDetails);

    boolean validateToken(String authToken);
}
