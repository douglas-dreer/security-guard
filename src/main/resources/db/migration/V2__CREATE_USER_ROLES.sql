CREATE TABLE IF NOT EXISTS public.user_roles (
                                   user_id BIGINT NOT NULL,
                                   role    VARCHAR(255) NOT NULL,
                                   CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role),
                                   CONSTRAINT user_roles_user_id_fkey
                                       FOREIGN KEY (user_id)
                                           REFERENCES public.users (id)
                                           ON DELETE CASCADE
);
