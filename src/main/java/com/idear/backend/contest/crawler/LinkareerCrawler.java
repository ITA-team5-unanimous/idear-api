package com.idear.backend.contest.crawler;

import com.idear.backend.contest.crawler.parser.LinkareerPageParser;
import com.idear.backend.contest.crawler.service.ContestPersistenceService;
import com.idear.backend.contest.crawler.service.ContestSaveService;
import com.idear.backend.contest.repository.ContestRepository;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ConditionalOnProperty(name = "idear.crawler.enabled", havingValue = "true")
@Slf4j
@Service
@RequiredArgsConstructor
public class LinkareerCrawler {

  private final LinkareerPageParser pageParser;
  private final ContestSaveService contestSaveService;
  private final ContestRepository contestRepository;
  private final ContestPersistenceService contestPersistenceService;

  private static final int MAX_PAGES = 2; // 크롤링할 최대 페이지 수 (초기 백필용)

  /**
   * 초기 백필 (최초 1회 실행)
   */
  public void initialBackfill() {
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

        // 페이지별로 트랜잭션 분리하여 저장
        int pageSaved = contestSaveService.saveBatch(urls, processedUrls);
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
  public void dailyUpdate() {
    log.info("=== 일일 업데이트 시작 ===");

    try {
      // 1. 마감된 공모전 삭제
      contestPersistenceService.deleteClosedContests(LocalDate.now());

      // 2. 새 공모전 추가 (페이지 제한 없이, 중복 발견 시 즉시 중단)
      int newCount = crawlNewContests();
      log.info("새 공모전 {}개 추가", newCount);

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
        if (contestSaveService.saveContestIfNotExists(url, processedUrls)) {
          totalSaved++;
        }
      }

      page++;
    }

    log.info("=== 새 공모전 크롤링 완료 (총 {}개 저장) ===", totalSaved);
    return totalSaved;
  }

  public long countContests() {
    return contestRepository.count();
  }
}