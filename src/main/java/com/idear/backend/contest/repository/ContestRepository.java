package com.idear.backend.contest.repository;

import com.idear.backend.contest.domain.Contest;
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

  @Query("SELECT c FROM Contest c WHERE c.deadline >= :today")
  List<Contest> findAllActiveContests(@Param("today") LocalDate today);

  @Modifying
  @Query("DELETE FROM Contest c WHERE c.deadline < :today")
  int deleteClosedContests(@Param("today") LocalDate today);
}