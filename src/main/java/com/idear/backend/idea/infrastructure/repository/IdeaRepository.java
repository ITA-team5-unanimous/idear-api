package com.idear.backend.idea.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.idear.backend.idea.domain.Idea;
import com.idear.backend.idea.domain.IdeaFile;
import com.idear.backend.user.domain.User;

public interface IdeaRepository extends JpaRepository<Idea, Long> {
	List<Idea> findAllByUser(User user);

	@Query("SELECT vf.ideaVersion.idea FROM IdeaVersionFile vf WHERE vf.ideaFile = :ideaFile")
	Optional<Idea> findIdeaByFile(@Param("ideaFile") IdeaFile ideaFile);
}
