package com.idear.backend.contest.crawler.service;

import com.idear.backend.contest.crawler.parser.ContestDetailParser;
import com.idear.backend.contest.domain.Contest;
import com.idear.backend.contest.repository.ContestRepository;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class ContestSaveService {

  private final ContestRepository contestRepository;
  private final ContestDetailParser detailParser;

  private static final long CRAWL_DELAY_MS = 2000;

  /**
   * 공모전 저장 (중복 체크 포함)
   */
  @Transactional
  public boolean saveContestIfNotExists(String linkareerUrl, Set<String> processedUrls) {
    // 메모리 중복 체크
    if (processedUrls.contains(linkareerUrl)) {
      log.debug("이미 처리한 URL: {}", linkareerUrl);
      return false;
    }

    try {
      // 상세 정보 크롤링
      Contest contest = detailParser.parseDetailPage(linkareerUrl);

      // DB 중복 체크
      if (isDuplicate(contest)) {
        processedUrls.add(linkareerUrl);
        return false;
      }

      // 저장
      contestRepository.save(contest);
      processedUrls.add(linkareerUrl);
      log.info("공모전 저장: {}", contest.getTitle());

      Thread.sleep(CRAWL_DELAY_MS);
      return true;

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("크롤링 중단: {}", linkareerUrl, e);
      throw CustomException.of(ErrorCode.CRAWLING_FAILED);
    } catch (CustomException e) {
      log.error("크롤링 실패: {}", linkareerUrl, e);
      processedUrls.add(linkareerUrl); // 실패한 URL 재시도 방지
      return false;
    }
  }

  /**
   * 마감된 공모전 삭제
   */
  @Transactional
  public int deleteClosedContests() {
    LocalDate today = LocalDate.now();
    int deleted = contestRepository.deleteClosedContests(today);

    if (deleted > 0) {
      log.info("마감된 공모전 {}개 삭제", deleted);
    }

    return deleted;
  }

  /**
   * 중복 체크 (homepageUrl 기준)
   */
  private boolean isDuplicate(Contest contest) {
    String homepageUrl = contest.getHomepageUrl();

    if (homepageUrl != null && contestRepository.existsByHomepageUrl(homepageUrl)) {
      log.debug("중복된 공모전 (homepageUrl): {}", homepageUrl);
      return true;
    }

    return false;
  }
}