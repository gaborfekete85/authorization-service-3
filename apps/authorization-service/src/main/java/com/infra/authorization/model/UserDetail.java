package com.infra.authorization.model;

import com.infra.authorization.persistence.entities.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Data
public class UserDetail implements UserDetails, OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;
    public UserDetail() {
        user = new User();
        this.attributes = Map.of();
    }
    public UserDetail(User user) {
        this.user = user;
        this.attributes = Map.of();
    }

    public UserDetail(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    public static UserDetail of(final User user) {
        return new UserDetail(user);
    }

    public static UserDetail of(final User user, Map<String, Object> attributes) {
        return new UserDetail(user, attributes);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Objects.isNull(user.getRoles()) ? Collections.EMPTY_LIST :
                user.getRoles().stream().map( x -> new SimpleGrantedAuthority(x.getName().name())).toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.getEmailVerified();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getEmailVerified();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.getEmailVerified();
    }

    @Override
    public boolean isEnabled() {
        return user.getEmailVerified();
    }

    @Override
    public String getName() {
        return user.getName();
    }
}
