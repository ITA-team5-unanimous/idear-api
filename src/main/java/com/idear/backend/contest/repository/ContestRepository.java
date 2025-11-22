package com.idear.backend.contest.repository;

import com.idear.backend.contest.domain.Contest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {

  Optional<Contest> findByLinkareerUrl(String linkareerUrl);

  boolean existsByHomepageUrl(String homepageUrl);

  boolean existsByLinkareerUrl(String linkareerUrl);

  /**
   * 마감 안 된 공모전 전체 조회 (최신순)
   */
  @Query("SELECT c FROM Contest c WHERE c.deadline >= :today ORDER BY c.createdAt DESC")
  Page<Contest> findAllActiveContests(@Param("today") LocalDate today, Pageable pageable);

  /**
   * 마감된 공모전 삭제
   */
  @Modifying
  @Query("DELETE FROM Contest c WHERE c.deadline < :today")
  int deleteClosedContests(@Param("today") LocalDate today);

  /**
   * 제목 키워드 검색 (최신순)
   */
  @Query("SELECT c FROM Contest c WHERE c.deadline >= :today AND c.title LIKE %:keyword% ORDER BY c.createdAt DESC")
  Page<Contest> searchByKeyword(@Param("today") LocalDate today, @Param("keyword") String keyword, Pageable pageable);

  /**
   * 인기순 정렬 (찜 수 기준)
   */
  @Query("SELECT c FROM Contest c " +
    "LEFT JOIN Bookmark b ON c.contestId = b.contestId " +
    "WHERE c.deadline >= :today " +
    "GROUP BY c.contestId " +
    "ORDER BY COUNT(b.bookmarkId) DESC, c.createdAt DESC")
  Page<Contest> findAllByPopular(@Param("today") LocalDate today, Pageable pageable);

  /**
   * 마감임박순 정렬
   */
  @Query("SELECT c FROM Contest c WHERE c.deadline >= :today ORDER BY c.deadline ASC")
  Page<Contest> findAllByDeadlineSoon(@Param("today") LocalDate today, Pageable pageable);

  /**
   * 찜수 기준 상위 N개 조회 (인기 공모전용)
   */
  @Query("SELECT c FROM Contest c " +
    "LEFT JOIN Bookmark b ON c.contestId = b.contestId " +
    "WHERE c.deadline >= :today " +
    "GROUP BY c.contestId " +
    "ORDER BY COUNT(b.bookmarkId) DESC, c.createdAt DESC")
  List<Contest> findTopByBookmarkCount(@Param("today") LocalDate today, Pageable pageable);

  /**
   * 특정 유저가 찜한 공모전 ID 목록 조회
   */
  @Query("SELECT c FROM Contest c " +
    "JOIN Bookmark b ON c.contestId = b.contestId " +
    "WHERE b.userId = :userId " +
    "ORDER BY b.createdAt DESC")
  Page<Contest> findBookmarkedContestsByUserId(@Param("userId") Long userId, Pageable pageable);
}