package com.idear.backend.idea.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idear.backend.idea.domain.Idea;

public interface IdeaRepository extends JpaRepository<Idea, Long> {
}
