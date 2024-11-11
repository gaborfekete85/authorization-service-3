package com.infra.authorization.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignInRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

}
