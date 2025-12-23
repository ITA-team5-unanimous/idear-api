package com.idear.backend.contest.crawler.service;

import com.idear.backend.contest.domain.Contest;
import com.idear.backend.contest.repository.ContestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@ConditionalOnProperty(name = "idear.crawler.enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
@Service
public class ContestPersistenceService {

  private final ContestRepository contestRepository;
  private final ContestBatchSaver batchSaver;

  @Transactional
  public boolean saveIfNotDuplicate(Contest contest, String url, Set<String> processedUrls) {
    if (isDuplicate(contest)) {
      processedUrls.add(url);
      return false;
    }

    contestRepository.save(contest);
    processedUrls.add(url);
    log.info("공모전 저장: {}", contest.getTitle());
    return true;
  }

  /**
   * 배치 저장 with 폴백 로직
   */
  public int saveBatchWithRetry(List<Contest> contestBatch) {
    if (contestBatch.isEmpty()) {
      return 0;
    }

    try {
      // 1차 시도: JDBC Batch Insert
      batchSaver.saveAllInBatch(contestBatch);
      log.info("배치 저장 완료: {}건", contestBatch.size());
      return contestBatch.size();
    } catch (Exception e) {
      log.error("배치 저장 실패, 개별 저장으로 폴백", e);
      // 2차 시도: 개별 저장 (새 트랜잭션에서 실행)
      return batchSaver.saveIndividually(contestBatch);
    }
  }

  @Transactional
  public int deleteClosedContests(LocalDate date) {
    return contestRepository.deleteClosedContests(date);
  }

  public boolean isDuplicate(Contest contest) {
    String homepageUrl = contest.getHomepageUrl();
    return homepageUrl != null && contestRepository.existsByHomepageUrl(homepageUrl);
  }
}
