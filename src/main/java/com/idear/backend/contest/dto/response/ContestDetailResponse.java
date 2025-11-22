package com.idear.backend.contest.dto.response;

import com.idear.backend.contest.domain.Contest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ContestDetailResponse {
  private Long contestId;
  private String title;
  private String host;
  private String category;
  private String imageUrl;
  private LocalDate startDate;
  private LocalDate deadline;
  private Long dDay;
  private String reward;
  private String description;
  private String homepageUrl;
  private boolean isBookmarked;

  public static ContestDetailResponse from(Contest contest, boolean isBookmarked) {
    return ContestDetailResponse.builder()
      .contestId(contest.getContestId())
      .title(contest.getTitle())
      .host(contest.getHost())
      .category(contest.getCategory())
      .imageUrl(contest.getImageUrl())
      .startDate(contest.getStartDate())
      .deadline(contest.getDeadline())
      .dDay(contest.getDDay())
      .reward(contest.getReward())
      .description(contest.getDescription())
      .homepageUrl(contest.getHomepageUrl())
      .isBookmarked(isBookmarked)
      .build();
  }
}