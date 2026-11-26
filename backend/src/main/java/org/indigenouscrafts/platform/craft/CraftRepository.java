package org.indigenouscrafts.platform.craft;

import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for crafts. Queries for listing and search come in #33 and #43. */
public interface CraftRepository extends JpaRepository<Craft, Long> {
}