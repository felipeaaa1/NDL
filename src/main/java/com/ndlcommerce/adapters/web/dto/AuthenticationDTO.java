package com.ndlcommerce.adapters.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthenticationDTO(
    @Schema(example = "admin") String login, @Schema(example = "Password") String password) {}
