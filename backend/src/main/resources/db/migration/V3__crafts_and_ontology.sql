-- Crafts and their controlled vocabulary (#31).
-- Design: wiki pages "Database-Schema" and "Craft-Ontology".
-- Images, videos and sources come in V4 (#35); locations on the map in V6 (#39).

-- ---------------------------------------------------------------------------
-- A craft entry: what it is, its story, and how big it is.
-- ---------------------------------------------------------------------------
CREATE TABLE craft (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created_by            BIGINT       NOT NULL REFERENCES app_user (id),
    -- "Kind of": a variant points to the craft it belongs to (Hatip ebrusu -> Ebru).
    parent_craft_id       BIGINT       REFERENCES craft (id) ON DELETE SET NULL,
    name                  VARCHAR(120) NOT NULL,
    local_name            VARCHAR(120),
    -- Other names, only used by search.
    alt_names             TEXT,
    description           TEXT         NOT NULL,
    cultural_relevance    TEXT,
    history               TEXT,
    heritage_recognition  VARCHAR(200),
    distinctive_features  TEXT,
    transmission          TEXT,
    dye_making            TEXT,
    -- Size: the shape and units are stored once here, the numbers in craft_dimension.
    shape                 VARCHAR(10),
    length_unit           VARCHAR(2),
    weight_unit           VARCHAR(2),
    size_note             VARCHAR(200),
    -- Only PUBLISHED crafts are visible to others. REMOVED = taken down by an admin.
    status                VARCHAR(10)  NOT NULL DEFAULT 'PUBLISHED',
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT craft_shape_check       CHECK (shape IN ('FLAT', 'OBJECT_3D')),
    CONSTRAINT craft_length_unit_check CHECK (length_unit IN ('MM', 'CM', 'M', 'IN')),
    CONSTRAINT craft_weight_unit_check CHECK (weight_unit IN ('G', 'KG', 'LB')),
    CONSTRAINT craft_status_check      CHECK (status IN ('DRAFT', 'PUBLISHED', 'REMOVED')),
    CONSTRAINT craft_not_own_parent    CHECK (parent_craft_id <> id)
);

CREATE INDEX craft_created_by_idx     ON craft (created_by);
CREATE INDEX craft_parent_idx         ON craft (parent_craft_id);
CREATE INDEX craft_status_created_idx ON craft (status, created_at DESC);

-- One row per measured dimension; value_to is set when the size is a range.
CREATE TABLE craft_dimension (
    craft_id    BIGINT        NOT NULL REFERENCES craft (id) ON DELETE CASCADE,
    dimension   VARCHAR(10)   NOT NULL,
    value_from  NUMERIC(10,2) NOT NULL,
    value_to    NUMERIC(10,2),

    PRIMARY KEY (craft_id, dimension),
    CONSTRAINT craft_dimension_check CHECK (dimension IN ('WIDTH', 'HEIGHT', 'DEPTH', 'DIAMETER', 'WEIGHT')),
    CONSTRAINT craft_dimension_positive CHECK (value_from >= 0),
    CONSTRAINT craft_dimension_range    CHECK (value_to IS NULL OR value_to >= value_from)
);

-- Ordered production steps. The optional photo column (image_id) is added in V4,
-- because the craft_image table does not exist yet.
CREATE TABLE production_step (
    id        BIGINT  GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    craft_id  BIGINT  NOT NULL REFERENCES craft (id) ON DELETE CASCADE,
    step_no   INT     NOT NULL,
    text      TEXT    NOT NULL,

    CONSTRAINT production_step_no_positive CHECK (step_no > 0),
    CONSTRAINT production_step_unique_no   UNIQUE (craft_id, step_no)
);

-- ---------------------------------------------------------------------------
-- Controlled vocabulary: all seven vocabularies share one table.
-- Terms are seeded and managed by admins (#55); users only pick them.
-- ---------------------------------------------------------------------------
CREATE TABLE ontology_term (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vocabulary   VARCHAR(10)  NOT NULL,
    slug         VARCHAR(80)  NOT NULL UNIQUE,
    label        VARCHAR(100) NOT NULL,
    local_name   VARCHAR(100),
    alt_labels   TEXT,
    definition   TEXT,
    -- Tree: null for top-level terms. The backend checks that the parent
    -- belongs to the same vocabulary.
    parent_id    BIGINT       REFERENCES ontology_term (id),
    wikidata_id  VARCHAR(20),
    color_hex    CHAR(7),
    year_from    INT,
    year_to      INT,
    sort_order   INT          NOT NULL DEFAULT 0,

    CONSTRAINT ontology_term_vocabulary_check CHECK (vocabulary IN
        ('CATEGORY', 'MATERIAL', 'TECHNIQUE', 'TOOL', 'COLOR', 'REGION', 'PERIOD')),
    CONSTRAINT ontology_term_color_hex_check CHECK (color_hex ~ '^#[0-9A-Fa-f]{6}$'),
    CONSTRAINT ontology_term_years_check     CHECK (year_to IS NULL OR year_from IS NULL OR year_to >= year_from),
    CONSTRAINT ontology_term_not_own_parent  CHECK (parent_id <> id)
);

CREATE INDEX ontology_term_vocabulary_idx ON ontology_term (vocabulary, sort_order);
CREATE INDEX ontology_term_parent_idx     ON ontology_term (parent_id);

-- Links a craft to its category, materials, techniques, tools, colors, regions and periods.
CREATE TABLE craft_term (
    craft_id  BIGINT      NOT NULL REFERENCES craft (id) ON DELETE CASCADE,
    term_id   BIGINT      NOT NULL REFERENCES ontology_term (id),
    -- Only for REGION terms: ORIGIN, PRACTICED or SPREAD.
    role      VARCHAR(10),

    PRIMARY KEY (craft_id, term_id),
    CONSTRAINT craft_term_role_check CHECK (role IN ('ORIGIN', 'PRACTICED', 'SPREAD'))
);

-- "Which crafts use this material?" -- used by the search filters (#43).
CREATE INDEX craft_term_term_idx ON craft_term (term_id);

-- ---------------------------------------------------------------------------
-- Free keywords that any user can add (unlike ontology terms).
-- ---------------------------------------------------------------------------
CREATE TABLE tag (
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  VARCHAR(50) NOT NULL UNIQUE,

    CONSTRAINT tag_name_lowercase CHECK (name = lower(name))
);

CREATE TABLE craft_tag (
    craft_id  BIGINT NOT NULL REFERENCES craft (id) ON DELETE CASCADE,
    tag_id    BIGINT NOT NULL REFERENCES tag (id) ON DELETE CASCADE,

    PRIMARY KEY (craft_id, tag_id)
);

CREATE INDEX craft_tag_tag_idx ON craft_tag (tag_id);