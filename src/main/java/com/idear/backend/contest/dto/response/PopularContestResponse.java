package com.idear.backend.contest.dto.response;

import com.idear.backend.contest.domain.Contest;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PopularContestResponse {
  private Long contestId;
  private String title;
  private String imageUrl;

  public static PopularContestResponse from(Contest contest) {
    return PopularContestResponse.builder()
      .contestId(contest.getContestId())
      .title(contest.getTitle())
      .imageUrl(contest.getImageUrl())
      .build();
  }
}