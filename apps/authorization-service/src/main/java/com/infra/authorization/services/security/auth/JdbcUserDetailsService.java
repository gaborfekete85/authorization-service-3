package com.infra.authorization.services.security.auth;

import com.infra.authorization.model.UserDetail;
import com.infra.authorization.services.UserService;
import com.infra.authorization.services.jwt.JWTService;
import com.infra.authorization.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JdbcUserDetailsService implements UserDetailsService {

    private final UserService userService;
    @Override
    public UserDetails loadUserByUsername(String userName) {
        if(ValidationUtil.isValidUUID(userName)) {
            return UserDetail.of(userService.findById(UUID.fromString(userName)));
        }

        if(ValidationUtil.isValidEmail(userName)) {
            return UserDetail.of(userService.findByEmail(userName));
        }
        throw new IllegalArgumentException("Invalid Username: " + userName);
    }

}
