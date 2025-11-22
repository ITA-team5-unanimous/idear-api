package com.idear.backend.contest.dto.response;

import com.idear.backend.contest.domain.Contest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ContestIdeaDetailResponse {
  private Long contestId;
  private String title;
  private LocalDate startDate;
  private LocalDate deadline;
  private String homepageUrl;
  private String imageUrl;

  public static ContestIdeaDetailResponse from(Contest contest) {
    return ContestIdeaDetailResponse.builder()
      .contestId(contest.getContestId())
      .title(contest.getTitle())
      .startDate(contest.getStartDate())
      .deadline(contest.getDeadline())
      .homepageUrl(contest.getHomepageUrl())
      .imageUrl(contest.getImageUrl())
      .build();
  }
}