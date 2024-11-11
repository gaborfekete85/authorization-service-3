package com.infra.authorization.controller.dto;

import com.infra.authorization.persistence.entities.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SignUpResponse {
    private final boolean success;
    private final String message;
    private final User user;
}
