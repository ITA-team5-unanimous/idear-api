package com.idear.backend.auth.dto.response;

import lombok.Builder;

@Builder
public record TokenResponse(String access, String refresh) { }
