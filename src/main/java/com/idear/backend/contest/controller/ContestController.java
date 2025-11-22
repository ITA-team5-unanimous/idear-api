package com.idear.backend.contest.controller;

import com.idear.backend.contest.application.ContestService;
import com.idear.backend.contest.dto.response.*;
import com.idear.backend.global.ApiResponse;
import com.idear.backend.global.annotation.ValidatedUser;
import com.idear.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contests")
@RequiredArgsConstructor
public class ContestController {

  private final ContestService contestService;

  /**
   * 인기 공모전 5개 조회
   * GET /contests/popular
   */
  @GetMapping("/popular")
  public ResponseEntity<ApiResponse<List<PopularContestResponse>>> getPopularContests() {
    List<PopularContestResponse> response = contestService.getPopularContests();
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * 공모전 텍스트 검색 (제목 기준, 최신순)
   * GET /contests/search?keyword=디자인
   */
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<Page<ContestListResponse>>> searchContests(
    @RequestParam String keyword,
    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
    @ValidatedUser User user
  ) {
    Page<ContestListResponse> response = contestService.searchContests(keyword, pageable, user.getId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * 공모전 리스트 조회 (정렬: 최신순, 인기순, 마감임박순)
   * GET /contests?sortBy=latest
   * GET /contests?sortBy=popular
   * GET /contests?sortBy=deadline
   */
  @GetMapping
  public ResponseEntity<ApiResponse<Page<ContestListResponse>>> getContests(
    @RequestParam(defaultValue = "latest") String sortBy,
    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
    @ValidatedUser User user
  ) {
    Page<ContestListResponse> response = contestService.getContests(sortBy, pageable, user.getId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * 공모전 상세페이지 조회
   * GET /contests/{contestId}
   */
  @GetMapping("/{contestId}")
  public ResponseEntity<ApiResponse<ContestDetailResponse>> getContestDetail(
    @PathVariable Long contestId,
    @ValidatedUser User user
  ) {
    ContestDetailResponse response = contestService.getContestDetail(contestId, user.getId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * 아이디어 상세페이지용 공모전 간단 정보 조회
   * GET /contests/{contestId}/simple
   */
  @GetMapping("/{contestId}/simple")
  public ResponseEntity<ApiResponse<ContestIdeaDetailResponse>> getContestSimpleInfo(
    @PathVariable Long contestId
  ) {
    ContestIdeaDetailResponse response = contestService.getContestSimpleInfo(contestId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * 찜한 공모전 조회
   * GET /contests/bookmarks
   */
  @GetMapping("/bookmarks")
  public ResponseEntity<ApiResponse<Page<ContestListResponse>>> getBookmarkedContests(
    @ValidatedUser User user,
    @PageableDefault(size = 20) Pageable pageable
  ) {
    Page<ContestListResponse> response = contestService.getBookmarkedContests(user.getId(), pageable);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * 공모전 찜하기
   * POST /contests/{contestId}/bookmark
   */
  @PostMapping("/{contestId}/bookmark")
  public ResponseEntity<ApiResponse<Void>> bookmarkContest(
    @PathVariable Long contestId,
    @ValidatedUser User user
  ) {
    contestService.bookmarkContest(contestId, user.getId());
    return ResponseEntity.ok(ApiResponse.success());
  }

  /**
   * 공모전 찜 취소하기
   * DELETE /contests/{contestId}/bookmark
   */
  @DeleteMapping("/{contestId}/bookmark")
  public ResponseEntity<ApiResponse<Void>> unbookmarkContest(
    @PathVariable Long contestId,
    @ValidatedUser User user
  ) {
    contestService.unbookmarkContest(contestId, user.getId());
    return ResponseEntity.ok(ApiResponse.success());
  }
}