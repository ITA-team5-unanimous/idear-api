package com.idear.backend.contest.crawler;

import com.idear.backend.contest.crawler.parser.LinkareerPageParser;
import com.idear.backend.contest.crawler.service.ContestSaveService;
import com.idear.backend.contest.domain.Contest;
import com.idear.backend.contest.repository.ContestRepository;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Service
public class LinkareerCrawler {

  private final LinkareerPageParser pageParser;
  private final ContestSaveService saveService;
  private final ContestRepository contestRepository;

  private static final int MAX_PAGES = 3; // 크롤링할 최대 페이지 수(초기 백필용, 테스트 3페이지)
  private static final int INCREMENTAL_PAGES = 2; // 일일 업데이트 시 크롤링할 페이지 수(테스트 2페이지)
  private static final long CRAWL_INTERVAL_MS = 100;

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
      throw new CustomException(ErrorCode.CRAWLING_FAILED, "초기 백필 실패");
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

      // 2. 새 공모전 추가 (최근 3페이지)
      int newCount = crawlNewContests();
      log.info("새 공모전 {}개 추가", newCount);

      // 3. 조회수 업데이트
      updateAllViewCounts();

      log.info("=== 일일 업데이트 완료 ===");

    } catch (Exception e) {
      log.error("일일 업데이트 실패", e);
      throw new CustomException(ErrorCode.CRAWLING_FAILED, "일일 업데이트 실패");
    }
  }

  /**
   * 새 공모전 크롤링 (최근 2페이지, 최신순)
   * 중복 URL 발견 시 즉시 중단 (최신순이므로 그 이후는 모두 기존 데이터)
   */
  private int crawlNewContests() {
    Set<String> processedUrls = new HashSet<>();
    int totalSaved = 0;

    for (int page = 1; page <= INCREMENTAL_PAGES; page++) {
      List<String> urls = pageParser.parseListPage(page);

      if (urls.isEmpty()) {
        log.info("{}페이지에 데이터 없음", page);
        break;
      }

      // 최신순으로 처리하면서 중복 발견 시 즉시 중단
      for (String url : urls) {
        try {
          // DB 중복 체크 - 상세 크롤링 전에 먼저 확인
          if (contestRepository.existsByLinkareerUrl(url)) {
            log.info("기존 공모전 발견 ({}페이지), 전체 크롤링 중단: {}", page, url);
            break;
          }

          Thread.sleep(CRAWL_INTERVAL_MS);

          // 중복이 아니면 상세 크롤링 & 저장
          if (saveService.saveContestIfNotExists(url, processedUrls)) {
            totalSaved++;
          }

        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new CustomException(ErrorCode.CRAWLING_FAILED);
        }
      }
    }

    return totalSaved;
  }

  /**
   * 조회수 업데이트 (진행 중인 공모전 전체)
   */
  private void updateAllViewCounts() {
    List<Contest> contests = contestRepository.findAllActiveContests(LocalDate.now());
    log.info("진행 중인 공모전 {}개의 조회수 업데이트 시작", contests.size());

    int success = 0;
    int fail = 0;

    for (int i = 0; i < contests.size(); i++) {
      Contest contest = contests.get(i);

      try {
        Thread.sleep(CRAWL_INTERVAL_MS * 5);

        if (saveService.updateViewCount(contest.getLinkareerUrl())) {
          success++;
        } else {
          fail++;
        }

        // 진행 상황 로그 (10개마다 또는 마지막)
        if ((i + 1) % 10 == 0 || i == contests.size() - 1) {
          log.info("진행 상황: {}/{} (성공: {}, 실패: {})",
            i + 1, contests.size(), success, fail);
        }

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("조회수 업데이트 중단", e);
        throw new CustomException(ErrorCode.CRAWLING_FAILED);
      }
    }

    log.info("조회수 업데이트 완료 (성공: {}, 실패: {})", success, fail);
  }

  /**
   * URL 목록 처리
   */
  private int processUrls(List<String> urls, Set<String> processedUrls) {
    int saved = 0;

    for (String url : urls) {
      try {
        Thread.sleep(CRAWL_INTERVAL_MS);

        if (saveService.saveContestIfNotExists(url, processedUrls)) {
          saved++;
        }

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new CustomException(ErrorCode.CRAWLING_FAILED);
      }
    }

    return saved;
  }

  public long countContests() {
    return contestRepository.count();
  }
}