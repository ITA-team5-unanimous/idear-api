package com.idear.backend.contest.crawler.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class LinkareerPageParser {

  private final WebDriver driver;

  private static final String BASE_URL = "https://linkareer.com";

  public List<String> parseListPage(int page) {
    try {
      log.info("{}페이지 로드 시작", page);

      // 첫 페이지는 URL로 직접 이동
      if (page == 1) {
        String pageUrl = buildListUrl(1);
        driver.get(pageUrl);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // 페이지 로딩 대기
        wait.until(ExpectedConditions.presenceOfElementLocated(
          By.cssSelector("section[class*='ActivityList'][class*='desktop']")
        ));

        // 페이지네이션 로딩 대기
        wait.until(ExpectedConditions.presenceOfElementLocated(
          By.cssSelector("button.button-page-number")
        ));

        Thread.sleep(2000);
        log.info("1페이지 로드 완료");

      } else {
        // 2페이지 이상: 먼저 목록 페이지로 돌아간 후 버튼 클릭
        log.info("목록 페이지로 복귀 중...");
        String listUrl = buildListUrl(1);  // 1페이지로 먼저 이동
        driver.get(listUrl);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(
          By.cssSelector("button.button-page-number")
        ));
        Thread.sleep(2000);

        log.info("목록 페이지 복귀 완료, {}페이지 버튼 클릭 시도", page);
        clickPaginationButton(page);
      }

      // 스크롤을 여러 번 시도
      scrollToBottomMultipleTimes();

      // 마지막 대기
      Thread.sleep(3000);

      return extractUrls(page);

    } catch (Exception e) {
      log.error("{}페이지 크롤링 실패", page, e);
      return Collections.emptyList();
    }
  }

  private String buildListUrl(int page) {
    return BASE_URL +
      "/list/contest" +
      "?filterType=CATEGORY" +
      "&orderBy_direction=DESC" +
      "&orderBy_field=CREATED_AT" +
      "&page=" + page;
  }

  private void clickPaginationButton(int targetPage) {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

      Thread.sleep(1000);

      List<WebElement> pageButtons = driver.findElements(
        By.cssSelector("button.button-page-number")
      );

      log.debug("총 {}개의 페이지 버튼 발견", pageButtons.size());

      WebElement targetButton = null;
      for (WebElement button : pageButtons) {
        try {
          WebElement span = button.findElement(By.cssSelector("span.MuiButton-label"));
          String text = span.getText().trim();

          if (text.equals(String.valueOf(targetPage))) {
            targetButton = button;
            log.info("{}페이지 버튼 찾음", targetPage);
            break;
          }
        } catch (Exception e) {
          continue;
        }
      }

      if (targetButton != null) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // 버튼을 화면 중앙으로 스크롤
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", targetButton);
        Thread.sleep(500);

        js.executeScript("arguments[0].click();", targetButton);
        log.info("{}페이지 버튼 클릭 완료", targetPage);

        // 페이지 전환 충분히 대기
        Thread.sleep(3000);

        // 새 콘텐츠 로딩 대기
        wait.until(ExpectedConditions.presenceOfElementLocated(
          By.cssSelector("section[class*='ActivityList'][class*='desktop']")
        ));

      } else {
        throw new RuntimeException("페이지 버튼을 찾을 수 없습니다: " + targetPage);
      }

    } catch (Exception e) {
      log.error("페이지네이션 버튼 클릭 실패", e);
      throw new RuntimeException("페이지 이동 실패: " + targetPage, e);
    }
  }

  /**
   * 여러 번 스크롤 시도 (동적 로딩 대응)
   */
  private void scrollToBottomMultipleTimes() {
    try {
      JavascriptExecutor js = (JavascriptExecutor) driver;

      // 총 3번 스크롤 시도
      for (int round = 0; round < 3; round++) {
        log.debug("스크롤 라운드 {}/3", round + 1);

        Long lastHeight = (Long) js.executeScript("return document.body.scrollHeight");

        // 천천히 스크롤 다운
        for (int i = 0; i < 5; i++) {
          js.executeScript("window.scrollBy(0, 500);");
          Thread.sleep(300);
        }

        // 맨 아래로
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        Thread.sleep(2000);

        Long newHeight = (Long) js.executeScript("return document.body.scrollHeight");

        log.debug("높이 변화: {} → {}", lastHeight, newHeight);

        if (newHeight.equals(lastHeight)) {
          log.debug("더 이상 콘텐츠가 로드되지 않음, 스크롤 종료");
          break;
        }
      }

      // 맨 위로 스크롤 (전체 콘텐츠 확인용)
      js.executeScript("window.scrollTo(0, 0);");
      Thread.sleep(1000);

      // 다시 맨 아래로
      js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
      Thread.sleep(1000);

      log.debug("스크롤 완료");

    } catch (Exception e) {
      log.warn("스크롤 중 오류 발생", e);
    }
  }

  private List<String> extractUrls(int page) {
    try {
      String pageSource = driver.getPageSource();
      Document doc = Jsoup.parse(pageSource);

      Element activityListSection = doc.selectFirst("section[class*='ActivityList'][class*='desktop']");

      if (activityListSection == null) {
        log.warn("공모전 목록 section을 찾을 수 없습니다.");
        return Collections.emptyList();
      }

      // 배너 제외 (div.list-body만 대상)
      Element listBody = activityListSection.selectFirst("div.list-body");

      Elements links;
      if (listBody != null) {
        log.debug("list-body 영역 발견");
        links = listBody.select("a[href*='/activity/']");
      } else {
        log.debug("list-body 없음, 전체 section에서 추출");
        links = activityListSection.select("a[href*='/activity/']");
      }

      log.debug("총 {}개의 링크 발견", links.size());

      Set<String> urlSet = new LinkedHashSet<>();

      for (Element link : links) {
        String href = link.attr("href");

        // /activity/숫자 형식만 추출
        if (href.matches("^/activity/\\d+.*")) {
          String cleanUrl = href.split("\\?")[0];
          String fullUrl = BASE_URL + cleanUrl;
          urlSet.add(fullUrl);

        } else if (href.matches("https://linkareer\\.com/activity/\\d+.*")) {
          String cleanUrl = href.split("\\?")[0];
          urlSet.add(cleanUrl);
        }
      }

      List<String> result = new ArrayList<>(urlSet);

      log.info("{}페이지에서 {}개의 공모전 URL 발견", page, result.size());

      if (!result.isEmpty()) {
        log.debug("   첫 번째: {}", result.get(0));
        if (result.size() > 1) {
          log.debug("   마지막: {}", result.get(result.size() - 1));
        }
      }

      return result;

    } catch (Exception e) {
      log.error("URL 추출 실패", e);
      return Collections.emptyList();
    }
  }
}