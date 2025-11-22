package com.idear.backend.contest.dto.response;

import com.idear.backend.contest.domain.Contest;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContestListResponse {
  private Long contestId;
  private String title;
  private String category;
  private String host;
  private String imageUrl;
  private Long dDay;
  private boolean isBookmarked;

  public static ContestListResponse from(Contest contest, boolean isBookmarked) {
    return ContestListResponse.builder()
      .contestId(contest.getContestId())
      .title(contest.getTitle())
      .category(contest.getCategory())
      .host(contest.getHost())
      .imageUrl(contest.getImageUrl())
      .dDay(contest.getDDay())
      .isBookmarked(isBookmarked)
      .build();
  }
}