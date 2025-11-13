package com.idear.backend.idea.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idear.backend.idea.domain.Idea;
import com.idear.backend.user.domain.User;

public interface IdeaRepository extends JpaRepository<Idea, Long> {
	List<Idea> findAllByUser(User user);
}
