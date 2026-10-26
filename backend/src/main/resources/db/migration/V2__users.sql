-- Users and email verification (#26, #54).
-- Design: wiki page "Database-Schema".

CREATE TABLE app_user (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username              VARCHAR(30)  NOT NULL,
    email                 VARCHAR(254) NOT NULL,
    -- BCrypt hash, never the plain password. Empty for Google-only accounts.
    password_hash         VARCHAR(100),
    auth_provider         VARCHAR(10)  NOT NULL DEFAULT 'LOCAL',
    -- The Google account id, only for auth_provider = 'GOOGLE'.
    provider_subject      VARCHAR(255),
    role                  VARCHAR(10)  NOT NULL DEFAULT 'USER',
    status                VARCHAR(10)  NOT NULL DEFAULT 'PENDING',
    display_name          VARCHAR(60),
    bio                   TEXT,
    profile_picture_key   VARCHAR(255),
    location_text         VARCHAR(100),
    email_notifications   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT app_user_auth_provider_check CHECK (auth_provider IN ('LOCAL', 'GOOGLE')),
    CONSTRAINT app_user_role_check          CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT app_user_status_check        CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED')),
    -- A local account must have a password; a Google account must have a Google id.
    CONSTRAINT app_user_credentials_check CHECK (
        (auth_provider = 'LOCAL'  AND password_hash IS NOT NULL) OR
        (auth_provider = 'GOOGLE' AND provider_subject IS NOT NULL)
    )
);

-- Usernames and emails are unique regardless of upper/lower case
-- ("Seda" and "seda" are the same user).
CREATE UNIQUE INDEX app_user_username_uk ON app_user (lower(username));
CREATE UNIQUE INDEX app_user_email_uk    ON app_user (lower(email));
CREATE UNIQUE INDEX app_user_provider_uk ON app_user (auth_provider, provider_subject)
    WHERE provider_subject IS NOT NULL;

CREATE TABLE email_verification_token (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    -- Only a hash of the token is stored, so a leaked database reveals no usable links.
    token_hash  VARCHAR(64)  NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    used_at     TIMESTAMPTZ
);

CREATE INDEX email_verification_token_user_idx ON email_verification_token (user_id);
