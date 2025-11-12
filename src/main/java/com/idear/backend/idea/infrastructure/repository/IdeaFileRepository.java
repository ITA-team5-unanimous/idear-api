package com.idear.backend.idea.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idear.backend.idea.domain.IdeaFile;

public interface IdeaFileRepository extends JpaRepository<IdeaFile, Long> {
}
