package com.idear.backend.contest.crawler.service;

import com.idear.backend.contest.domain.Contest;
import com.idear.backend.contest.repository.ContestRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Contest 배치 저장 전용 서비스
 */
@ConditionalOnProperty(name = "idear.crawler.enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
@Service
public class ContestBatchSaver {

  private final ContestRepository contestRepository;
  private final EntityManager entityManager;

  private static final int BATCH_SIZE = 50;

  /**
   * JDBC Batch Insert를 사용한 배치 저장
   */
  @Transactional
  public void saveAllInBatch(List<Contest> contests) {
    for (int i = 0; i < contests.size(); i++) {
      entityManager.persist(contests.get(i));

      // BATCH_SIZE마다 flush & clear
      if ((i + 1) % BATCH_SIZE == 0) {
        entityManager.flush();
        entityManager.clear();
      }
    }

    // 남은 데이터 처리
    entityManager.flush();
    entityManager.clear();
  }

  /**
   * 개별 저장 (새 트랜잭션)
   * 배치 저장 실패 시 폴백용
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public int saveIndividually(List<Contest> contests) {
    int saved = 0;
    for (Contest contest : contests) {
      try {
        contestRepository.save(contest);
        saved++;
        log.info("개별 저장 성공: {}", contest.getTitle());
      } catch (Exception e) {
        log.error("개별 저장 실패: {}", contest.getTitle(), e);
      }
    }
    return saved;
  }
}