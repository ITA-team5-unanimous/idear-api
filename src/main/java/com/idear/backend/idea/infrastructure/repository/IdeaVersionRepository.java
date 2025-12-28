package com.idear.backend.idea.infrastructure.repository;

import com.idear.backend.idea.domain.Idea;
import com.idear.backend.idea.domain.IdeaVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IdeaVersionRepository extends JpaRepository<IdeaVersion, Long> {

	List<IdeaVersion> findAllByIdeaOrderByVersionNumberDesc(Idea idea);

	@Query("SELECT MAX(v.versionNumber) FROM IdeaVersion v WHERE v.idea = :idea")
	Optional<Integer> findMaxVersionNumberByIdea(@Param("idea") Idea idea);

	@Query("SELECT v FROM IdeaVersion v " +
		   "WHERE v.idea = :idea " +
		   "ORDER BY v.versionNumber DESC " +
		   "LIMIT 1")
	Optional<IdeaVersion> findLatestByIdea(@Param("idea") Idea idea);

	@Query("SELECT DISTINCT v FROM IdeaVersion v " +
		   "LEFT JOIN FETCH v.versionFiles vf " +
		   "LEFT JOIN FETCH vf.ideaFile " +
		   "LEFT JOIN FETCH v.versionImages vi " +
		   "LEFT JOIN FETCH vi.ideaImage " +
		   "WHERE v.idea = :idea " +
		   "ORDER BY v.versionNumber DESC " +
		   "LIMIT 1")
	Optional<IdeaVersion> findLatestByIdeaWithFilesAndImages(@Param("idea") Idea idea);
}
