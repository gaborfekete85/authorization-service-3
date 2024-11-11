package com.infra.authorization.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignInResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
}
