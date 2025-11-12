package com.idear.backend.contest.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@ConditionalOnProperty(name = "idear.crawler.enabled", havingValue = "true")
@Slf4j
@Component
@RequiredArgsConstructor
public class LinkareerCrawlScheduler {

  private final LinkareerCrawler linkareerCrawler;
  private final AtomicBoolean initialBackfillCompleted = new AtomicBoolean(false);

  /**
   * 애플리케이션 시작 시 초기 백필 실행 (1회만)
   * 실제 운영에서는 DB 플래그로 관리 추천
   */
  @EventListener(ApplicationReadyEvent.class)
  public void runInitialBackfillOnStartup() {
    long cnt = linkareerCrawler.countContests();
    if (cnt >= 10) {
      log.info("[초기 백필] 기존 데이터 {}건 >= 10 → 스킵", cnt);
      initialBackfillCompleted.set(true);  // 이미 데이터 있으면 완료로 표시
      return;
    }

    if (!initialBackfillCompleted.get()) {
      log.info("=== 애플리케이션 시작: 초기 백필 시작 ===");

      try {
        linkareerCrawler.initialBackfill();
        initialBackfillCompleted.set(true);
        log.info("=== 초기 백필 완료 ===");
      } catch (Exception e) {
        log.error("초기 백필 실패. 다음 스케줄에서 재시도합니다.", e);
      }
    }
  }

  /**
   * 매일 오전 12시에 일일 업데이트 실행
   * 1. 마감된 공모전 삭제
   * 2. 새 공모전 추가
   * 3. 인기 공모전 추출
   */
  @Scheduled(cron = "0 0 0 * * *")
  public void scheduledDailyUpdate() {
    // 초기 백필이 완료되지 않았으면 스킵
    if (!initialBackfillCompleted.get()) {
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