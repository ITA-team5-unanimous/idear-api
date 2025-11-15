package com.idear.backend.global.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WebDriver 설정
 */
@Slf4j
@Configuration
public class WebDriverConfig {

  private WebDriver driver;

  @Bean
  public WebDriver webDriver() {
    WebDriverManager.chromedriver().setup();

    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless"); // 백그라운드에서 동작
    options.addArguments("--no-sandbox"); // 보안 기능 비활성화
    options.addArguments("--disable-dev-shm-usage"); // 메모리 부족 문제 회피
    options.addArguments("--disable-gpu"); // GPU 비활성화 (헤드리스 모드에서 필요)
    options.addArguments("--window-size=1920,1080"); // 창 크기 설정
    options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"); // 사용자 에이전트 설정
    options.addArguments("--remote-allow-origins=*");  // 원격 출처 허용 (보안 정책 우회)
    options.addArguments("--disable-extensions"); // 확장 프로그램 비활성화

    this.driver = new ChromeDriver(options);
    log.info("WebDriver 초기화 완료");

    return this.driver;
  }

  @PreDestroy
  public void cleanup() {
    if (driver != null) {
      driver.quit();
      log.info("WebDriver 종료");
    }
  }
}
