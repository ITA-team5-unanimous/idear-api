package com.idear.backend.global.dto;

import com.idear.backend.user.domain.UserRole;
import lombok.Builder;

@Builder
public record UserInfo(Long id, String name, String email, String providerInfo, UserRole role) { }
