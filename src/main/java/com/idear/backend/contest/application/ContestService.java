package com.idear.backend.contest.application;

import com.idear.backend.contest.domain.Bookmark;
import com.idear.backend.contest.repository.BookmarkRepository;
import com.idear.backend.contest.domain.Contest;
import com.idear.backend.contest.dto.response.*;
import com.idear.backend.contest.repository.ContestRepository;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ConditionalOnProperty(prefix = "idear.crawler", name = "enabled", havingValue = "false", matchIfMissing = true)
public class ContestService {

  private final ContestRepository contestRepository;
  private final BookmarkRepository bookmarkRepository;

  /**
   * 인기 공모전 5개 조회 (찜수 기준)
   */
  public List<PopularContestResponse> getPopularContests() {
    LocalDate today = LocalDate.now();
    Pageable pageable = PageRequest.of(0, 5);

    List<Contest> popularContests = contestRepository.findTopByBookmarkCount(today, pageable);

    return popularContests.stream()
      .map(PopularContestResponse::from)
      .collect(Collectors.toList());
  }

  /**
   * 공모전 텍스트 검색 (제목 기준, 최신순)
   */
  public Page<ContestListResponse> searchContests(String keyword, Pageable pageable, Long userId) {
    LocalDate today = LocalDate.now();
    Page<Contest> contests = contestRepository.searchByKeyword(today, keyword, pageable);

    // 찜 여부 확인
    List<Long> contestIds = contests.getContent().stream()
      .map(Contest::getContestId)
      .collect(Collectors.toList());

    Set<Long> bookmarkedIds = bookmarkRepository.findBookmarkedContestIds(userId, contestIds);

    return contests.map(contest ->
      ContestListResponse.from(contest, bookmarkedIds.contains(contest.getContestId()))
    );
  }

  /**
   * 공모전 리스트 조회 (정렬: 최신순, 인기순, 마감임박순)
   */
  public Page<ContestListResponse> getContests(String sortBy, Pageable pageable, Long userId) {
    LocalDate today = LocalDate.now();
    Page<Contest> contests = switch (sortBy) {
      case "popular" -> contestRepository.findAllByPopular(today, pageable);
      case "deadline" -> contestRepository.findAllByDeadlineSoon(today, pageable);
      default -> contestRepository.findAllActiveContests(today, pageable);
    };

    // 찜 여부 확인
    List<Long> contestIds = contests.getContent().stream()
      .map(Contest::getContestId)
      .collect(Collectors.toList());

    Set<Long> bookmarkedIds = bookmarkRepository.findBookmarkedContestIds(userId, contestIds);

    return contests.map(contest ->
      ContestListResponse.from(contest, bookmarkedIds.contains(contest.getContestId()))
    );
  }

  /**
   * 공모전 상세페이지 조회
   */
  public ContestDetailResponse getContestDetail(Long contestId, Long userId) {
    Contest contest = contestRepository.findById(contestId)
      .orElseThrow(() -> CustomException.of(ErrorCode.CONTEST_NOT_FOUND));

    boolean isBookmarked = bookmarkRepository.existsByUserIdAndContestId(userId, contestId);
    return ContestDetailResponse.from(contest, isBookmarked);
  }

  /**
   * 아이디어 상세페이지용 공모전 간단 정보 조회
   */
  public ContestIdeaDetailResponse getContestSimpleInfo(Long contestId) {
    Contest contest = contestRepository.findById(contestId)
      .orElseThrow(() -> CustomException.of(ErrorCode.CONTEST_NOT_FOUND));

    return ContestIdeaDetailResponse.from(contest);
  }

  /**
   * 찜한 공모전 조회
   */
  public Page<ContestListResponse> getBookmarkedContests(Long userId, Pageable pageable) {
    Page<Contest> contests = contestRepository.findBookmarkedContestsByUserId(userId, pageable);

    return contests.map(contest -> ContestListResponse.from(contest, true));
  }

  /**
   * 공모전 찜하기
   */
  @Transactional
  public void bookmarkContest(Long contestId, Long userId) {
    // Contest 존재 확인
    if (!contestRepository.existsById(contestId)) {
      throw CustomException.of(ErrorCode.CONTEST_NOT_FOUND);
    }

    // 이미 찜한 경우 예외 처리
    if (bookmarkRepository.existsByUserIdAndContestId(userId, contestId)) {
      throw CustomException.of(ErrorCode.INVALID_INPUT, "이미 찜한 공모전입니다.");
    }

    Bookmark bookmark = Bookmark.of(userId, contestId);
    bookmarkRepository.save(bookmark);
  }

  /**
   * 공모전 찜 취소하기
   */
  @Transactional
  public void unbookmarkContest(Long contestId, Long userId) {
    Bookmark bookmark = bookmarkRepository.findByUserIdAndContestId(userId, contestId)
      .orElseThrow(() -> CustomException.of(ErrorCode.INVALID_INPUT, "찜하지 않은 공모전입니다."));

    bookmarkRepository.delete(bookmark);
  }
}