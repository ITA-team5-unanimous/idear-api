package com.idear.backend.contest.controller;

import com.idear.backend.contest.domain.Contest;
import com.idear.backend.contest.dto.DDayResponse;
import com.idear.backend.contest.repository.ContestRepository;
import com.idear.backend.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test/contests")
public class ContestTestController {

  private final ContestRepository contestRepository;

  /**
   * 오늘 기준 진행중인 공모전의 D-day 목록 (테스트용)
   * GET /api/test/contests/active/dday
   */
  @GetMapping("/active/dday")
  public ApiResponse<List<DDayResponse>> getActiveContestsDDay() {
    List<Contest> active = contestRepository.findAllActiveContests(LocalDate.now());
    List<DDayResponse> result = active.stream()
      .map(DDayResponse::from)
      .toList();
    return ApiResponse.success(result);
  }
}
