package com.infra.authorization.services.security.auth;

import com.infra.authorization.model.UserDetail;
import com.infra.authorization.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JdbcUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) {
        return UserDetail.of(userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found")));
    }
}
