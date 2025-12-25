package com.idear.backend.idea.infrastructure.repository;

import com.idear.backend.idea.domain.IdeaFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdeaFileRepository extends JpaRepository<IdeaFile, Long> {
    Optional<IdeaFile> findIdeaFileByCommit(String commit);
}
