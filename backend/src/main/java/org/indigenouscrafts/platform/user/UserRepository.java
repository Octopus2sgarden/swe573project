package org.indigenouscrafts.platform.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Database access for users. Spring Data writes the SQL from the method names.
 * Lookups ignore upper/lower case, matching the unique indexes in V2__users.sql.
 */
public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    Optional<AppUser> findByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);
}
