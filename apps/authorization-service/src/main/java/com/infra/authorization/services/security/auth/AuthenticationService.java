package com.infra.authorization.services.security.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.infra.authorization.controller.dto.RefreshTokenRequest;
import com.infra.authorization.controller.dto.SignInResponse;
import com.infra.authorization.model.UserDetail;
import com.infra.authorization.persistence.entities.ERole;
import com.infra.authorization.persistence.entities.Role;
import com.infra.authorization.persistence.entities.User;
import com.infra.authorization.persistence.repository.RoleRepository;
import com.infra.authorization.persistence.repository.UserRepository;
import com.infra.authorization.services.UserService;
import com.infra.authorization.services.jwt.JWTService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public SignInResponse signin(UserDetail userDetail) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userDetail.getUsername(),
                        userDetail.getUser().getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtService.createToken((UserDetails) authentication.getPrincipal());
        String userName = jwtService.getUserName(token);
        String refreshToken = jwtService.generateRefreshToken(Map.of(), userDetail);

        return SignInResponse.builder().tokenType("Bearer").accessToken(token).refreshToken(refreshToken).build();
    }

    public SignInResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        User user = userService.findById(jwtService.getUserId(refreshTokenRequest.getRefreshToken()));
        UserDetail userDetail = UserDetail.of(user);
        if(jwtService.isTokenValid(refreshTokenRequest.getRefreshToken(), userDetail)) {
            String token = jwtService.createToken(userDetail);
            return SignInResponse.builder().tokenType("Bearer").accessToken(token).refreshToken(refreshTokenRequest.getRefreshToken()).build();
        }
        return null;
    }
    @Transactional
    public User signup(UserDetail userDetail) throws BadRequestException, FirebaseAuthException {
        if(userRepository.existsByEmail(userDetail.getUsername())) {
            throw new RuntimeException("Email address already in use.");
        }
        User result = persistUser(userDetail);
//        sendEmailVerificationMail(user);
//        registerFirebaseUser(result, signUpRequest.getPassword());
        return result;
    }

    private static void sendEmailVerificationMail(User user) {
//        String[] to = { user.getEmail() };
//        String subject = applicationName + " - Email verification";
//        String body = String.format("<h1>Welcome to %s</h1><br/>Please click on the link below to confirm your email address: <br/><a href=%s/api/auth/confirm/%s>Confirm your email address</a><br/><br/>Enjoy your Jurney<br/>%s team", applicationName, publicEndpoint, verificationCode, applicationName);
//        sendMail(to, subject, body);
    }

    private User persistUser(UserDetail userDetail) {
        User user = userDetail.getUser();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRoles = roleRepository.findByName(ERole.ROLE_USER);
        user.setRoles(Set.of(userRoles));
        User result = userRepository.save(user);
        return result;
    }

    private void registerFirebaseUser(User user, String password) throws FirebaseAuthException {
        UserRecord.CreateRequest ur = new UserRecord.CreateRequest();
        ur.setEmail(user.getEmail());
        ur.setEmailVerified(false);
        ur.setPassword(password);
        ur.setDisabled(false);
        ur.setDisplayName(user.getName());
        UserRecord response = FirebaseAuth.getInstance().createUser(ur);

        Map insert = new HashMap() {{
            put("name", response.getDisplayName());
            put("email", response.getEmail());
            put("username", response.getEmail());
            put("image", response.getPhotoUrl());
            put("followingCount", 0);
            put("followersCount", 0);
        }};
//        persistFirebaseUser(response.getUid(), user);
        //AuthorizationApplication.fireStore.collection("users").document(response.getUid()).set(insert);
    }

    private void persistFirebaseUser(String uid, User user) {
        Map insert = new HashMap() {{
            put("name", user.getName());
            put("email", user.getEmail());
            put("username", user.getEmail());
            put("image", user.getImageUrl());
            put("followingCount", 0);
            put("followersCount", 0);
        }};
//        AuthorizationServiceApplication.fireStore.collection("users").document(uid).set(insert);
    }
}
