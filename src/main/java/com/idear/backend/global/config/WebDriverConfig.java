package com.idear.backend.global.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * WebDriver 설정
 */
@ConditionalOnProperty(name = "idear.crawler.enabled", havingValue = "true")
@Slf4j
@Configuration
public class WebDriverConfig {

  private WebDriver driver;

  @Bean
  public WebDriver webDriver() {
    WebDriverManager.chromedriver().setup();

    ChromeOptions options = new ChromeOptions();
    // 기본 headless 설정
    options.addArguments("--headless=new");  // 새로운 headless 모드 (더 가벼움)
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--disable-gpu");
    options.addArguments("--disable-software-rasterizer");
    options.addArguments("--single-process");

    options.addArguments("--window-size=1920,1080");
    options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

    // 메모리 최적화 옵션들
    options.addArguments("--disable-extensions");
    options.addArguments("--disable-logging");
    options.addArguments("--disable-permissions-api");
    options.addArguments("--disable-notifications");
    options.addArguments("--disable-offer-store-unmasked-wallet-cards");
    options.addArguments("--disable-speech-api");
    options.addArguments("--disable-background-networking");
    options.addArguments("--disable-background-timer-throttling");
    options.addArguments("--disable-backgrounding-occluded-windows");
    options.addArguments("--disable-breakpad");
    options.addArguments("--disable-component-extensions-with-background-pages");
    options.addArguments("--disable-features=TranslateUI,BlinkGenPropertyTrees");
    options.addArguments("--disable-ipc-flooding-protection");
    options.addArguments("--disable-renderer-backgrounding");
    options.addArguments("--enable-features=NetworkService,NetworkServiceInProcess");
    options.addArguments("--force-color-profile=srgb");
    options.addArguments("--hide-scrollbars");
    options.addArguments("--metrics-recording-only");
    options.addArguments("--mute-audio");
    options.addArguments("--blink-settings=imagesEnabled=false");

    // 캐시 비활성화
    options.addArguments("--disk-cache-size=0");
    options.addArguments("--media-cache-size=0");
    options.addArguments("--aggressive-cache-discard");
    options.addArguments("--disable-application-cache");
    options.addArguments("--disable-cache");
    options.addArguments("--disable-offline-load-stale-cache");

    this.driver = new ChromeDriver(options);

    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(120));
    driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(90));
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));

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
