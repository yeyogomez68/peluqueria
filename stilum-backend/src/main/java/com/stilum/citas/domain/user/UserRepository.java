package com.stilum.citas.domain.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    List<User> findAllByTenantId(UUID tenantId);

    User save(User user);

    boolean existsByEmail(String email);
}
