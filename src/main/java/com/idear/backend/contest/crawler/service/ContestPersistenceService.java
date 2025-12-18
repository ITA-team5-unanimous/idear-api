package com.idear.backend.contest.crawler.service;

import com.idear.backend.contest.domain.Contest;
import com.idear.backend.contest.repository.ContestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

// ContestPersistenceService.java (새로 생성)
@Slf4j
@RequiredArgsConstructor
@Service
public class ContestPersistenceService {

  private final ContestRepository contestRepository;

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

  @Transactional
  public int saveBatchWithRetry(List<Contest> contestBatch) {
    try {
      contestRepository.saveAll(contestBatch);
      log.info("배치 저장 완료: {}건", contestBatch.size());
      return contestBatch.size();
    } catch (Exception e) {
      log.error("배치 저장 실패, 개별 저장으로 폴백", e);
      return saveIndividually(contestBatch);
    }
  }

  private int saveIndividually(List<Contest> contests) {
    int saved = 0;
    for (Contest contest : contests) {
      try {
        contestRepository.save(contest);
        saved++;
      } catch (Exception e) {
        log.error("개별 저장 실패: {}", contest.getTitle(), e);
      }
    }
    return saved;
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
