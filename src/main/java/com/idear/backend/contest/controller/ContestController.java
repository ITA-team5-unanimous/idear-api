package com.idear.backend.contest.controller;

import com.idear.backend.contest.application.ContestService;
import com.idear.backend.contest.dto.response.*;
import com.idear.backend.global.ApiResponse;
import com.idear.backend.global.annotation.ValidatedUser;
import com.idear.backend.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Contest", description = "공모전 조회 및 찜하기 API")
@RestController
@RequestMapping("/contests")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "idear.crawler", name = "enabled", havingValue = "false", matchIfMissing = true)
public class ContestController {

  private final ContestService contestService;

  @Operation(
    summary = "인기 공모전 조회",
    description = "조회수 기준 상위 인기 공모전 5개를 조회합니다."
  )
  @GetMapping("/popular")
  public ResponseEntity<ApiResponse<List<PopularContestResponse>>> getPopularContests() {
    List<PopularContestResponse> response = contestService.getPopularContests();
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(
    summary = "공모전 검색",
    description = "키워드로 공모전 제목을 검색합니다. 최신순으로 정렬되며 페이징을 지원합니다."
  )
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<Page<ContestListResponse>>> searchContests(
    @Parameter(description = "검색 키워드", required = true, example = "디자인")
    @RequestParam String keyword,
    @Parameter(description = "페이징 정보 (size=20, sort=createdAt,desc)", hidden = true)
    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
    @Parameter(hidden = true) @ValidatedUser User user
  ) {
    Page<ContestListResponse> response = contestService.searchContests(keyword, pageable, user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(
    summary = "공모전 목록 조회",
    description = "공모전 목록을 조회합니다. 정렬 옵션: latest(최신순), popular(인기순), deadline(마감임박순)"
  )
  @GetMapping
  public ResponseEntity<ApiResponse<Page<ContestListResponse>>> getContests(
    @Parameter(description = "정렬 기준 (latest, popular, deadline)", example = "latest")
    @RequestParam(defaultValue = "latest") String sortBy,
    @Parameter(description = "페이징 정보 (size=20, sort=createdAt,desc)", hidden = true)
    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
    @Parameter(hidden = true) @ValidatedUser User user
  ) {
    Page<ContestListResponse> response = contestService.getContests(sortBy, pageable, user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(
    summary = "공모전 상세 조회",
    description = "특정 공모전의 상세 정보를 조회합니다."
  )
  @GetMapping("/{contestId}")
  public ResponseEntity<ApiResponse<ContestDetailResponse>> getContestDetail(
    @Parameter(description = "공모전 ID", required = true, example = "1")
    @PathVariable Long contestId,
    @Parameter(hidden = true) @ValidatedUser User user
  ) {
    ContestDetailResponse response = contestService.getContestDetail(contestId, user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(
    summary = "공모전 간단 정보 조회",
    description = "아이디어 상세 페이지에서 사용할 공모전 간단 정보를 조회합니다."
  )
  @GetMapping("/{contestId}/simple")
  public ResponseEntity<ApiResponse<ContestIdeaDetailResponse>> getContestSimpleInfo(
    @Parameter(description = "공모전 ID", required = true, example = "1")
    @PathVariable Long contestId
  ) {
    ContestIdeaDetailResponse response = contestService.getContestSimpleInfo(contestId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(
    summary = "찜한 공모전 조회",
    description = "현재 로그인한 사용자가 찜한 공모전 목록을 조회합니다."
  )
  @GetMapping("/bookmarks")
  public ResponseEntity<ApiResponse<Page<ContestListResponse>>> getBookmarkedContests(
    @Parameter(hidden = true) @ValidatedUser User user,
    @Parameter(description = "페이징 정보 (size=20)", hidden = true)
    @PageableDefault(size = 20) Pageable pageable
  ) {
    Page<ContestListResponse> response = contestService.getBookmarkedContests(user.getUserId(), pageable);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(
    summary = "공모전 찜하기",
    description = "특정 공모전을 찜 목록에 추가합니다."
  )
  @PostMapping("/{contestId}/bookmark")
  public ResponseEntity<ApiResponse<Void>> bookmarkContest(
    @Parameter(description = "공모전 ID", required = true, example = "1")
    @PathVariable Long contestId,
    @Parameter(hidden = true) @ValidatedUser User user
  ) {
    contestService.bookmarkContest(contestId, user.getUserId());
    return ResponseEntity.ok(ApiResponse.success());
  }

  @Operation(
    summary = "공모전 찜 취소",
    description = "특정 공모전을 찜 목록에서 제거합니다."
  )
  @DeleteMapping("/{contestId}/bookmark")
  public ResponseEntity<ApiResponse<Void>> unbookmarkContest(
    @Parameter(description = "공모전 ID", required = true, example = "1")
    @PathVariable Long contestId,
    @Parameter(hidden = true) @ValidatedUser User user
  ) {
    contestService.unbookmarkContest(contestId, user.getUserId());
    return ResponseEntity.ok(ApiResponse.success());
  }
}