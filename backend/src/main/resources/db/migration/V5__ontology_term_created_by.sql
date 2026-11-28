-- #55: users can add vocabulary terms directly, without approval (decision 28 Nov 2026).
-- We record who added each term so an admin can find and remove wrong ones.
-- Seeded terms (V4) keep NULL = added by the system.
-- If the user account is deleted, the term stays and created_by becomes NULL.
ALTER TABLE ontology_term
    ADD COLUMN created_by BIGINT REFERENCES app_user (id) ON DELETE SET NULL;

CREATE INDEX ontology_term_created_by_idx ON ontology_term (created_by);