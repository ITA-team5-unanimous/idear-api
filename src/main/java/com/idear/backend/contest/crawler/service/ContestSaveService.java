package com.idear.backend.contest.crawler.service;

import com.idear.backend.contest.crawler.parser.ContestDetailParser;
import com.idear.backend.contest.domain.Contest;
import com.idear.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ConditionalOnProperty(name = "idear.crawler.enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
@Service
public class ContestSaveService {

  private final ContestPersistenceService contestPersistenceService;
  private final ContestDetailParser detailParser;

  private static final int BATCH_SIZE = 50;

  /**
   * 공모전 저장 (중복 체크 포함)
   */
  public boolean saveContestIfNotExists(String linkareerUrl, Set<String> processedUrls) {
    // 메모리 중복 체크
    if (processedUrls.contains(linkareerUrl)) {
      log.debug("이미 처리한 URL: {}", linkareerUrl);
      return false;
    }

    try {
      // 상세 정보 크롤링
      Contest contest = detailParser.parseDetailPage(linkareerUrl);

      // 저장
      return contestPersistenceService.saveIfNotDuplicate(contest, linkareerUrl, processedUrls);

    } catch (CustomException e) {
      log.error("크롤링 실패: {}", linkareerUrl, e);
      processedUrls.add(linkareerUrl); // 실패한 URL 재시도 방지
      return false;
    }
  }

  /**
   * 공모전 배치 저장 (여러 건을 한 번에)
   */
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

        if (!contestPersistenceService.isDuplicate(contest)) {
          contestBatch.add(contest);
        }

        processedUrls.add(url);

        if (contestBatch.size() >= BATCH_SIZE) {
          savedCount += contestPersistenceService.saveBatchWithRetry(contestBatch);
          contestBatch.clear();
        }
      } catch (CustomException e) {
        log.error("크롤링 실패: {}", url, e);
        processedUrls.add(url);
      }
    }

    // 남은 데이터 저장
    if (!contestBatch.isEmpty()) {
      savedCount += contestPersistenceService.saveBatchWithRetry(contestBatch);
    }

    return savedCount;
  }
}