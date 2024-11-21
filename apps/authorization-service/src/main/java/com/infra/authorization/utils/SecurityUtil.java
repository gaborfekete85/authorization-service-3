package com.infra.authorization.utils;

import com.infra.authorization.model.UserDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.util.UUID;


public class SecurityUtil {

    public static final String ROLE_PREFIX = "ROLE";
    public static String getUserEmail(final Principal principal) {
        return ((UserDetail) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUsername();
    }
    public static UUID getUserId(final Principal principal) {
        return ((UserDetail) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUser().getId();
    }
}