-- First migration: enable PostGIS for craft locations and map queries.
-- Tables are added in later migrations (see #21 and #26).
CREATE EXTENSION IF NOT EXISTS postgis;
