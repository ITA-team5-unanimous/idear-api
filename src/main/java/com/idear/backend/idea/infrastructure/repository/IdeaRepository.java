package com.idear.backend.idea.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.idear.backend.idea.domain.Idea;
import com.idear.backend.idea.domain.IdeaFile;
import com.idear.backend.user.domain.User;

public interface IdeaRepository extends JpaRepository<Idea, Long> {
	@Query("SELECT vf.ideaVersion.idea FROM IdeaVersionFile vf WHERE vf.ideaFile = :ideaFile")
	Optional<Idea> findIdeaByFile(@Param("ideaFile") IdeaFile ideaFile);

	@Query("SELECT i FROM Idea i WHERE i.user = :user ORDER BY i.requestedAt DESC")
	Page<Idea> findByUserOrderByRequestedAtDesc(@Param("user") User user, Pageable pageable);

	@Query("SELECT DISTINCT i FROM Idea i " +
		"JOIN i.versions v " +
		"WHERE i.user = :user " +
		"AND v.versionNumber = (SELECT MAX(v2.versionNumber) FROM IdeaVersion v2 WHERE v2.idea = i) " +
		"AND v.title LIKE %:keyword% " +
		"ORDER BY i.requestedAt DESC")
	Page<Idea> findByUserAndTitleContaining(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);
}
