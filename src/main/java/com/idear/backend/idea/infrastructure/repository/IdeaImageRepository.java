package com.idear.backend.idea.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idear.backend.idea.domain.IdeaImage;

public interface IdeaImageRepository extends JpaRepository<IdeaImage, Long> {
}
