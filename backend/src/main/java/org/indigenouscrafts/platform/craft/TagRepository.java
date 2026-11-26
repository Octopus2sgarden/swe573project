package org.indigenouscrafts.platform.craft;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for tags. Names are stored in lowercase, see {@link Tag#normalize}. */
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);
}