package com.idear.backend.contest.repository;

import com.idear.backend.contest.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

  /**
   * 특정 유저가 특정 공모전을 찜했는지 확인
   */
  boolean existsByUserIdAndContestId(Long userId, Long contestId);

  /**
   * 특정 유저의 특정 공모전 찜 조회
   */
  Optional<Bookmark> findByUserIdAndContestId(Long userId, Long contestId);

  /**
   * 특정 유저가 찜한 공모전 ID 목록 조회 (효율적인 IN 쿼리용)
   */
  @Query("SELECT b.contestId FROM Bookmark b WHERE b.userId = :userId AND b.contestId IN :contestIds")
  Set<Long> findBookmarkedContestIds(@Param("userId") Long userId, @Param("contestIds") List<Long> contestIds);
}