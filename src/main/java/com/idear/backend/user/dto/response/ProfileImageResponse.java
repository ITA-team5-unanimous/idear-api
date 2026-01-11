package com.idear.backend.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileImageResponse {
  private String profileImageUrl;

  public static ProfileImageResponse of(String profileImageUrl) {
    return new ProfileImageResponse(profileImageUrl);
  }
}
