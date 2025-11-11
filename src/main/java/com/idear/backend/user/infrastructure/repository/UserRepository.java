package com.idear.backend.user.infrastructure.repository;

import com.idear.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderInfo(String providerInfo);
}
