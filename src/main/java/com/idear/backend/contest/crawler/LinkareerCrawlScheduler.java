package com.idear.backend.contest.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 개발 세미나용 코드 리뷰 주석
 * - Linkareer 크롤링 스케줄러
 */
@ConditionalOnProperty(name = "idear.crawler.enabled", havingValue = "true")
@Slf4j
@Component
@RequiredArgsConstructor
public class LinkareerCrawlScheduler {

  private final LinkareerCrawler linkareerCrawler;

  /**
   * 애플리케이션 시작 시 초기 백필 실행 (1회만)
   * DB 데이터 개수로 백필 완료 여부 판단
   */
  @EventListener(ApplicationReadyEvent.class)
  public void runInitialBackfillOnStartup() {
    long cnt = linkareerCrawler.countContests();

    if (cnt >= 10) {
      log.info("[초기 백필] 기존 데이터 {}건 >= 10 → 스킵", cnt);
      return;
    }

    log.info("=== 애플리케이션 시작: 초기 백필 시작 ===");

    try {
      linkareerCrawler.initialBackfill();
      log.info("=== 초기 백필 완료 ===");
    } catch (Exception e) {
      log.error("초기 백필 실패. 다음 스케줄에서 재시도합니다.", e);
    }
  }

  /**
   * 매일 오전 12시에 일일 업데이트 실행
   * 1. 마감된 공모전 삭제
   * 2. 새 공모전 추가
   */
  @Scheduled(cron = "0 0 0 * * *")
  public void scheduledDailyUpdate() {
    // 초기 백필이 완료되지 않았으면 스킵
    long cnt = linkareerCrawler.countContests();

    if (cnt < 10) {
      log.debug("초기 백필 진행 중 - 스케줄 실행 스킵");
      return;
    }

    log.info("=== 스케줄 실행: 일일 업데이트 시작 ===");

    try {
      linkareerCrawler.dailyUpdate();
      log.info("=== 일일 업데이트 완료 ===");
    } catch (Exception e) {
      log.error("일일 업데이트 실패", e);
    }
  }
}