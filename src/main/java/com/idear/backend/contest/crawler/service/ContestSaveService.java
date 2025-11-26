package com.idear.backend.contest.crawler.service;

import com.idear.backend.contest.crawler.parser.ContestDetailParser;
import com.idear.backend.contest.domain.Contest;
import com.idear.backend.contest.repository.ContestRepository;
import com.idear.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class ContestSaveService {

  private final ContestRepository contestRepository;
  private final ContestDetailParser detailParser;

  private static final int BATCH_SIZE = 50;

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

      return true;

    } catch (CustomException e) {
      log.error("크롤링 실패: {}", linkareerUrl, e);
      processedUrls.add(linkareerUrl); // 실패한 URL 재시도 방지
      return false;
    }
  }

  /**
   * 공모전 배치 저장 (여러 건을 한 번에)
   */
  @Transactional
  public int saveBatch(List<String> urls, Set<String> processedUrls) {
    List<Contest> contestBatch = new ArrayList<>();
    int savedCount = 0;

    for (String url : urls) {
      // 메모리 중복 체크
      if (processedUrls.contains(url)) {
        log.debug("이미 처리한 URL: {}", url);
        continue;
      }

      try {
        // 상세 정보 크롤링
        Contest contest = detailParser.parseDetailPage(url);

        // DB 중복 체크
        if (!isDuplicate(contest)) {
          contestBatch.add(contest);

          // 배치 크기에 도달하면 저장
          if (contestBatch.size() >= BATCH_SIZE) {
            contestRepository.saveAll(contestBatch);
            savedCount += contestBatch.size();
            log.info("배치 저장 완료: {}건", contestBatch.size());
            contestBatch.clear();
          }
        }

        processedUrls.add(url);

      } catch (CustomException e) {
        log.error("크롤링 실패: {}", url, e);
        processedUrls.add(url);
      }
    }

    // 남은 데이터 저장
    if (!contestBatch.isEmpty()) {
      savedCount += saveBatchWithRetry(contestBatch);
    }

    return savedCount;
  }

  private int saveBatchWithRetry(List<Contest> contestBatch) {
    try {
      contestRepository.saveAll(contestBatch);
      log.info("배치 저장 완료: {}건", contestBatch.size());
      return contestBatch.size();

    } catch (Exception e) {
      log.error("배치 저장 실패, 개별 저장으로 폴백: {}건", contestBatch.size(), e);
      return saveIndividually(contestBatch);
    }
  }

  /**
   * 개별 저장 (폴백 전략)
   */
  private int saveIndividually(List<Contest> contests) {
    int saved = 0;

    for (Contest contest : contests) {
      try {
        contestRepository.save(contest);
        saved++;
        log.debug("개별 저장 성공: {}", contest.getTitle());
      } catch (Exception e) {
        log.error("개별 저장 실패: {} - {}", contest.getTitle(), e.getMessage());
      }
    }

    log.info("개별 저장 폴백 완료: {}개 중 {}개 성공", contests.size(), saved);
    return saved;
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