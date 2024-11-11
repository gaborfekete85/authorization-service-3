package com.infra.authorization.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
@Data
public class SignUpRequest {
    @NotBlank
    private String name;

//    @NotBlank
    private String firstName;

//    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private String phone;
}
