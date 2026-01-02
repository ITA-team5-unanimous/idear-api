package com.idear.backend.idea.infrastructure.repository;

import com.idear.backend.idea.domain.IdeaVersionTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdeaVersionTagRepository extends JpaRepository<IdeaVersionTag, Long> {
}
