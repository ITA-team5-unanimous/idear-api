package com.idear.backend.contest.crawler.parser;

import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
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

      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

      // 첫 페이지는 URL로 직접 이동
      if (page == 1) {
        String pageUrl = buildListUrl(1);
        driver.get(pageUrl);

        // 페이지 로딩 대기
        wait.until(ExpectedConditions.presenceOfElementLocated(
          By.cssSelector("section[class*='ActivityList'][class*='desktop']")
        ));

        // 페이지네이션 로딩 대기
        wait.until(ExpectedConditions.presenceOfElementLocated(
          By.cssSelector("button.button-page-number")
        ));

        // 실제 콘텐츠 링크가 로드될 때까지 대기
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
          By.cssSelector("a[href*='/activity/']"), 0
        ));

        log.info("1페이지 로드 완료");

      } else {
        // 2페이지 이상: 먼저 목록 페이지로 돌아간 후 버튼 클릭
        log.info("목록 페이지로 복귀 중...");
        String listUrl = buildListUrl(1);  // 1페이지로 먼저 이동
        driver.get(listUrl);

        wait.until(ExpectedConditions.presenceOfElementLocated(
          By.cssSelector("button.button-page-number")
        ));

        // 페이지네이션이 완전히 렌더링될 때까지 대기
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
          By.cssSelector("button.button-page-number"), 0
        ));

        log.info("목록 페이지 복귀 완료, {}페이지 버튼 클릭 시도", page);
        clickPaginationButton(page);
      }

      // 스크롤을 여러 번 시도
      scrollToBottomMultipleTimes();

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

        // 스크롤 후 요소가 안정될 때까지 대기
        wait.until(ExpectedConditions.elementToBeClickable(targetButton));

        js.executeScript("arguments[0].click();", targetButton);
        log.info("{}페이지 버튼 클릭 완료", targetPage);

        // 새 콘텐츠 로딩 대기
        wait.until(ExpectedConditions.presenceOfElementLocated(
          By.cssSelector("section[class*='ActivityList'][class*='desktop']")
        ));

        // URL이 변경되었는지 확인
        wait.until(driver -> driver.getCurrentUrl().contains("page=" + targetPage));

      } else {
        log.error("페이지 버튼을 찾을 수 없습니다: {}", targetPage);
        throw CustomException.of(ErrorCode.PAGE_PARSING_FAILED, "페이지 버튼을 찾을 수 없습니다: " + targetPage);
      }

    } catch (CustomException e) {
      throw e; // CustomException은 그대로 전파
    } catch (Exception e) {
      log.error("페이지네이션 버튼 클릭 실패", e);
      throw CustomException.of(ErrorCode.PAGE_PARSING_FAILED, "페이지 이동 실패: " + targetPage);
    }
  }

  /**
   * 여러 번 스크롤 시도 (동적 로딩 대응)
   */
  private void scrollToBottomMultipleTimes() {
    try {
      JavascriptExecutor js = (JavascriptExecutor) driver;
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

      // 총 2번 스크롤 시도
      for (int round = 0; round < 2; round++) {
        log.debug("스크롤 라운드 {}/2", round + 1);

        Long lastHeight = (Long) js.executeScript("return document.body.scrollHeight");

        // 천천히 스크롤 다운
        for (int i = 0; i < 5; i++) {
          js.executeScript("window.scrollBy(0, 500);");

          // 각 스크롤 후 짧은 대기 (DOM 업데이트 시간 확보)
          try {
            wait.until(driver ->
              (Long) js.executeScript("return document.readyState === 'complete'")
            );
          } catch (Exception e) {
            // readyState 체크 실패해도 계속 진행
          }
        }

        // 맨 아래로
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // 새 콘텐츠 로딩 대기 (높이 변화 또는 타임아웃)
        try {
          wait.until(driver -> {
            Long newHeight = (Long) js.executeScript("return document.body.scrollHeight");
            return !newHeight.equals(lastHeight);
          });
        } catch (Exception e) {
          // 높이 변화 없으면 다음 라운드로
        }

        Long newHeight = (Long) js.executeScript("return document.body.scrollHeight");

        log.debug("높이 변화: {} → {}", lastHeight, newHeight);

        if (newHeight.equals(lastHeight)) {
          log.debug("더 이상 콘텐츠가 로드되지 않음, 스크롤 종료");
          break;
        }
      }

      // 맨 위로 스크롤 (전체 콘텐츠 확인용)
      js.executeScript("window.scrollTo(0, 0);");

      // 다시 맨 아래로
      js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

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

      return result;

    } catch (Exception e) {
      log.error("URL 추출 실패", e);
      return Collections.emptyList();
    }
  }

  /**
   * 새 공모전 탐색용: 첫 페이지부터 순회하며 URL 추출
   * 페이지 제한 없음 (전체 페이지 순회 가능)
   */
  public void changeOrderByLatest() {
    try {
      log.info("정렬 기준 변경: 최신순");

      // 먼저 목록 페이지로 이동
      String listUrl = buildListUrl(1);
      driver.get(listUrl);

      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

      // 페이지 로딩 대기
      wait.until(ExpectedConditions.presenceOfElementLocated(
        By.cssSelector("section[class*='ActivityList'][class*='desktop']")
      ));

      // orderby-box 또는 orderby-menu-container 찾기
      WebElement menuContainer = null;

      try {
        menuContainer = wait.until(
          ExpectedConditions.elementToBeClickable(By.cssSelector("div.orderby-box"))
        );
        log.debug("orderby-box 발견");
      } catch (Exception e1) {
        try {
          menuContainer = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("div.orderby-menu-container"))
          );
          log.debug("orderby-menu-container 발견");
        } catch (Exception e2) {
          log.error("정렬 메뉴 컨테이너를 찾을 수 없음");
          throw e2;
        }
      }

      // JavaScript로 클릭
      JavascriptExecutor js = (JavascriptExecutor) driver;
      js.executeScript("arguments[0].scrollIntoView({block: 'center'});", menuContainer);

      // 요소가 클릭 가능할 때까지 대기
      wait.until(ExpectedConditions.elementToBeClickable(menuContainer));

      js.executeScript("arguments[0].click();", menuContainer);

      log.debug("정렬 메뉴 열림");

      // 드롭다운 메뉴가 나타날 때까지 대기
      wait.until(ExpectedConditions.presenceOfElementLocated(
        By.cssSelector("div.orderby-menu-item")
      ));

      // '최신순' 찾아서 클릭
      List<WebElement> menuItems = driver.findElements(
        By.cssSelector("div.orderby-menu-item")
      );

      log.debug("총 {}개의 정렬 옵션 발견", menuItems.size());

      boolean found = false;
      for (WebElement item : menuItems) {
        String text = item.getText();
        log.debug("정렬 옵션: {}", text);

        if (text.contains("최신순")) {
          js.executeScript("arguments[0].click();", item);
          log.info("'최신순' 클릭 완료");
          found = true;
          break;
        }
      }

      if (!found) {
        log.error("'최신순' 옵션을 찾을 수 없음");
        throw CustomException.of(ErrorCode.PAGE_PARSING_FAILED, "'최신순' 정렬 옵션을 찾을 수 없습니다");
      }

      // 페이지가 새로고침되거나 콘텐츠가 업데이트될 때까지 대기
      wait.until(ExpectedConditions.presenceOfElementLocated(
        By.cssSelector("section[class*='ActivityList'][class*='desktop']")
      ));

    } catch (CustomException e) {
      throw e; // CustomException은 그대로 전파
    } catch (Exception e) {
      log.error("정렬 기준 변경 실패", e);
      throw CustomException.of(ErrorCode.PAGE_PARSING_FAILED, "정렬 기준 변경 실패: 최신순");
    }
  }

  /**
   * 정렬 기준 변경 (최근 스크랩 증가 수)
   * 개선: 페이지 먼저 로드, 여러 셀렉터 시도, JavaScript 클릭
   */
  public void changeOrderByRecentScrap() {
    try {
      log.info("정렬 기준 변경: 최근 스크랩 증가 수");

      // 먼저 목록 페이지로 이동 (정렬 메뉴가 있는 페이지)
      String listUrl = buildListUrl(1);
      driver.get(listUrl);

      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

      // 페이지 로딩 대기
      wait.until(ExpectedConditions.presenceOfElementLocated(
        By.cssSelector("section[class*='ActivityList'][class*='desktop']")
      ));

      Thread.sleep(2000); // 페이지 완전 로딩 대기

      // orderby-box 또는 orderby-menu-container 찾기
      WebElement menuContainer = null;

      try {
        // 먼저 orderby-box 시도
        menuContainer = wait.until(
          ExpectedConditions.elementToBeClickable(By.cssSelector("div.orderby-box"))
        );
        log.debug("orderby-box 발견");
      } catch (Exception e1) {
        // orderby-menu-container 시도
        try {
          menuContainer = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("div.orderby-menu-container"))
          );
          log.debug("orderby-menu-container 발견");
        } catch (Exception e2) {
          log.error("정렬 메뉴 컨테이너를 찾을 수 없음");
          throw e2;
        }
      }

      // 메뉴 컨테이너 클릭 (JavaScript 사용)
      JavascriptExecutor js = (JavascriptExecutor) driver;
      js.executeScript("arguments[0].scrollIntoView({block: 'center'});", menuContainer);

      // 요소가 클릭 가능할 때까지 대기
      wait.until(ExpectedConditions.elementToBeClickable(menuContainer));

      js.executeScript("arguments[0].click();", menuContainer);

      log.debug("정렬 메뉴 열림");

      // 드롭다운 메뉴가 나타날 때까지 대기
      wait.until(ExpectedConditions.presenceOfElementLocated(
        By.cssSelector("div.orderby-menu-item")
      ));

      // orderby-menu-list 내의 '최근 스크랩 증가순' 찾아서 클릭
      List<WebElement> menuItems = driver.findElements(
        By.cssSelector("div.orderby-menu-item")
      );

      log.debug("총 {}개의 정렬 옵션 발견", menuItems.size());

      boolean found = false;
      for (WebElement item : menuItems) {
        String text = item.getText();
        log.debug("정렬 옵션: {}", text);

        if (text.contains("최근 스크랩 증가") || text.contains("스크랩 증가")) {
          js.executeScript("arguments[0].click();", item);
          log.info("'최근 스크랩 증가순' 클릭 완료");
          found = true;
          break;
        }
      }

      if (!found) {
        log.error("'최근 스크랩 증가순' 옵션을 찾을 수 없음");
        throw CustomException.of(ErrorCode.PAGE_PARSING_FAILED, "'최근 스크랩 증가순' 정렬 옵션을 찾을 수 없습니다");
      }

      // 페이지가 새로고침되거나 콘텐츠가 업데이트될 때까지 대기
      wait.until(ExpectedConditions.presenceOfElementLocated(
        By.cssSelector("section[class*='ActivityList'][class*='desktop']")
      ));

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("정렬 기준 변경 실패", e);
      throw CustomException.of(ErrorCode.PAGE_PARSING_FAILED, "정렬 기준 변경 실패: 최근 스크랩 증가순");
    }
  }

}