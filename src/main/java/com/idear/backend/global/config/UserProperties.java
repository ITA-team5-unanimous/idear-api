package com.idear.backend.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "user")
public class UserProperties {

  private Profile profile = new Profile();
  private Email email = new Email();

  @Getter
  @Setter
  public static class Profile {
    private String defaultImageUrl;
    private long maxImageSize;
    private List<String> allowedExtensions;
  }

  @Getter
  @Setter
  public static class Email {
    private Verification verification = new Verification();

    @Getter
    @Setter
    public static class Verification {
      private long codeExpirationMinutes;
      private long verifiedExpirationMinutes;
    }
  }
}
