package com.infra.authorization.controller.dto;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
