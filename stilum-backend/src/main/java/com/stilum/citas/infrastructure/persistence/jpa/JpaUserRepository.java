package com.stilum.citas.infrastructure.persistence.jpa;

import com.stilum.citas.domain.user.User;
import com.stilum.citas.domain.user.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserRepository extends JpaRepository<User, UUID>, UserRepository {

    @Override
    Optional<User> findByEmail(String email);

    @Override
    boolean existsByEmail(String email);

    @Override
    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId")
    List<User> findAllByTenantId(@Param("tenantId") UUID tenantId);
}
