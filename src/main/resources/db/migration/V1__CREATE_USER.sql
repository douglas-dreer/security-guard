CREATE TABLE IF NOT EXISTS public.users (
                              id BIGSERIAL PRIMARY KEY,
                              account_non_expired     BOOLEAN,
                              account_non_locked      BOOLEAN,
                              credentials_non_expired BOOLEAN,
                              enabled                 BOOLEAN,
                              created_at              TIMESTAMP,
                              last_login              TIMESTAMP,
                              password_reset_expires  TIMESTAMP,
                              updated_at              TIMESTAMP,
                              email                   VARCHAR(255) NOT NULL,
                              password                VARCHAR(255) NOT NULL,
                              password_reset_token    VARCHAR(255),
                              username                VARCHAR(255) NOT NULL
);
