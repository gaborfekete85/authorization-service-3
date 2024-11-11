package com.infra.authorization.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.infra.authorization.controller.dto.*;
import com.infra.authorization.mapper.UserDetailMapper;
import com.infra.authorization.model.UserDetail;
import com.infra.authorization.persistence.entities.User;
import com.infra.authorization.services.security.auth.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    @GetMapping("/public/hello")
    public String sayHello() {
        return "Hello, World";
    }

    private final UserDetailMapper userDetailMapper;
    @PostMapping("/public/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignUpRequest signUpRequest) throws FirebaseAuthException, BadRequestException {
        log.info("SignUp: {}", signUpRequest.getEmail());
        if(StringUtils.isBlank(signUpRequest.getFirstName()) || StringUtils.isBlank(signUpRequest.getLastName())) {
            String[] parts = signUpRequest.getName().split(" ");
            if(parts.length == 0) {
                signUpRequest.setFirstName("Anonymous");
                signUpRequest.setLastName(UUID.randomUUID().toString());
            }
            if(parts.length == 1) {
                signUpRequest.setFirstName(parts[0]);
                signUpRequest.setLastName(UUID.randomUUID().toString());
            }
            if(parts.length > 1) {
                signUpRequest.setFirstName(parts[0]);
                signUpRequest.setLastName(String.join(" ", Arrays.asList(parts).subList(1, parts.length)));
            }
        }
        User userToSignUp = userDetailMapper.mapFromSignUpRequestToDomain(signUpRequest);
        User signedUpUser = authenticationService.signup(UserDetail.of(userToSignUp));
        return ResponseEntity.ok()
                .body(new SignUpResponse(true, "User successfully registered. ", signedUpUser));
    }

    @PostMapping("/public/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) throws FirebaseAuthException, BadRequestException {
        log.info("Refresh token");
        SignInResponse response = authenticationService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody SignInRequest signInRequest) {
        log.info("Log in: {}", signInRequest.getEmail());
        User userToLogin = userDetailMapper.mapFromSignInRequestToDomain(signInRequest);
        SignInResponse response = authenticationService.signin(UserDetail.of(userToLogin));
        return ResponseEntity.ok(response);
    }

}
