package com.idear.backend.contest.crawler;

import com.idear.backend.contest.crawler.parser.LinkareerPageParser;
import com.idear.backend.contest.crawler.service.ContestSaveService;
import com.idear.backend.contest.domain.Contest;
import com.idear.backend.contest.repository.ContestRepository;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ConditionalOnProperty(name = "idear.crawler.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
@Service
public class LinkareerCrawler {

  private final LinkareerPageParser pageParser;
  private final ContestSaveService saveService;
  private final ContestRepository contestRepository;

  private static final int MAX_PAGES = 3; // 크롤링할 최대 페이지 수(초기 백필용, 테스트 3페이지)
  private static final int TOP_N_POPULAR = 10; // 인기 공모전 개수

  /**
   * 초기 백필 (최초 1회 실행)
   */
  @Transactional
  public void initialBackfill() {
    if (contestRepository.count() >= 10) {
      log.info("이미 데이터가 존재하여 초기 백필 스킵");
      return;
    }

    log.info("=== 초기 백필 시작 ===");

    int totalSaved = 0;
    Set<String> processedUrls = new HashSet<>();

    try {
      for (int page = 1; page <= MAX_PAGES; page++) {
        List<String> urls = pageParser.parseListPage(page);

        if (urls.isEmpty()) {
          log.info("{}페이지에 데이터 없음, 백필 종료", page);
          break;
        }

        int pageSaved = processUrls(urls, processedUrls);
        totalSaved += pageSaved;

        // 전체가 중복이면 종료
        if (pageSaved == 0) {
          log.info("{}페이지 전체 중복, 백필 종료", page);
          break;
        }
      }

      log.info("=== 초기 백필 완료 (총 {}개 저장) ===", totalSaved);

    } catch (Exception e) {
      log.error("초기 백필 실패", e);
      throw CustomException.of(ErrorCode.CRAWLING_FAILED, "초기 백필 실패");
    }
  }

  /**
   * 일일 업데이트
   */
  @Transactional
  public void dailyUpdate() {
    log.info("=== 일일 업데이트 시작 ===");

    try {
      // 1. 마감된 공모전 삭제
      saveService.deleteClosedContests();

      // 2. 새 공모전 추가 (페이지 제한 없이, 중복 발견 시 즉시 중단)
      int newCount = crawlNewContests();
      log.info("새 공모전 {}개 추가", newCount);

      // 3. 인기 공모전 추출
      List<Contest> popularContests = extractPopularContests();
      log.info("인기 공모전 {}개 추출 완료", popularContests.size());

      log.info("=== 일일 업데이트 완료 ===");

    } catch (Exception e) {
      log.error("일일 업데이트 실패", e);
      throw CustomException.of(ErrorCode.CRAWLING_FAILED, "일일 업데이트 실패");
    }
  }

  /**
   * 새 공모전 크롤링 (페이지 제한 없음, 중복 발견 시 즉시 중단)
   */
  private int crawlNewContests() {
    log.info("=== 새 공모전 크롤링 시작 (최신순) ===");

    Set<String> processedUrls = new HashSet<>();
    int totalSaved = 0;
    int page = 1;

    while (true) {
      log.info("{}페이지 크롤링 시작", page);

      List<String> urls = pageParser.parseListPage(page);

      if (urls.isEmpty()) {
        log.info("{}페이지에 데이터 없음, 크롤링 종료", page);
        break;
      }

      // URL을 하나씩 처리하면서 중복 발견 시 즉시 return으로 전체 종료
      for (String url : urls) {
        // DB 중복 체크 - 상세 크롤링 전에 먼저 확인
        if (contestRepository.existsByLinkareerUrl(url)) {
          log.info("기존 공모전 발견 ({}페이지), 전체 크롤링 중단: {}", page, url);
          return totalSaved; // 즉시 return으로 메서드 종료
        }

        // 중복이 아니면 상세 크롤링 & 저장
        if (saveService.saveContestIfNotExists(url, processedUrls)) {
          totalSaved++;
        }
      }

      page++;

      // 안전장치: 최대 10페이지까지만 크롤링
      if (page > 10) {
        log.warn("최대 페이지 수(10) 도달, 크롤링 종료");
        break;
      }
    }

    log.info("=== 새 공모전 크롤링 완료 (총 {}개 저장) ===", totalSaved);
    return totalSaved;
  }

  /**
   * 인기 공모전 추출 (최근 스크랩 증가 수 기준 상위 10개)
   */
  @Transactional(readOnly = true)
  public List<Contest> extractPopularContests() {
    log.info("=== 인기 공모전 추출 시작 ===");

    try {
      // 1. 정렬 기준을 '최근 스크랩 증가 수'로 변경
      pageParser.changeOrderByRecentScrap();

      // 2. 변경된 정렬 기준으로 첫 페이지의 URL 추출 (parseListPage 사용)
      List<String> topUrls = pageParser.parseListPage(1);

      // 상위 10개만 사용
      if (topUrls.size() > TOP_N_POPULAR) {
        topUrls = topUrls.subList(0, TOP_N_POPULAR);
      }

      log.info("상위 {}개 URL 추출 완료", topUrls.size());

      // 3. DB에서 해당 URL의 Contest 조회
      List<Contest> popularContests = new ArrayList<>();

      for (String url : topUrls) {
        try {
          Contest contest = contestRepository.findByLinkareerUrl(url)
            .orElseThrow(() -> CustomException.of(ErrorCode.CONTEST_NOT_FOUND));
          popularContests.add(contest);
          log.debug("인기 공모전: {}", contest.getTitle());
        } catch (Exception e) {
          log.warn("DB에서 찾을 수 없는 URL (스킵): {}", url);
          // 찾지 못해도 계속 진행
        }
      }

      // 4. 정렬 기준을 다시 '최신순'으로 복원
      pageParser.changeOrderByLatest();

      log.info("=== 인기 공모전 추출 완료 ({}개) ===", popularContests.size());
      return popularContests;

    } catch (Exception e) {
      log.error("인기 공모전 추출 실패", e);

      // 실패해도 정렬 기준은 복원 시도
      try {
        pageParser.changeOrderByLatest();
      } catch (Exception ex) {
        log.error("정렬 기준 복원 실패", ex);
      }

      return new ArrayList<>();
    }
  }

  /**
   * URL 목록 처리
   */
  private int processUrls(List<String> urls, Set<String> processedUrls) {
    int saved = 0;

    for (String url : urls) {
      if (saveService.saveContestIfNotExists(url, processedUrls)) {
        saved++;
      }
    }

    return saved;
  }

  public long countContests() {
    return contestRepository.count();
  }
}