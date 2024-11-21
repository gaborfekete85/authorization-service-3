package com.infra.authorization.services.security.auth;

import com.infra.authorization.model.UserDetail;
import com.infra.authorization.persistence.entities.ERole;
import com.infra.authorization.persistence.entities.Role;
import com.infra.authorization.persistence.entities.User;
import com.infra.authorization.services.jwt.JWTService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {
    private final JWTService jwtService;
    @Override
    public UserDetails loadUserByUsername(String jwtToken) {
        Map<String, Object> claims = jwtService.getClaims(jwtToken);
        User user = User.builder()
                .id(UUID.fromString(claims.get(Claims.SUBJECT).toString()))
                .email(claims.get("email").toString())
                .roles(((Set<String>) claims.get("roles")).stream().map( x -> new Role(ERole.valueOf(x))).collect(Collectors.toSet()))
                .build();
        return UserDetail.of(user);
    }

}
