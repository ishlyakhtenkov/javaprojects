DROP TABLE IF EXISTS description_elements;
DROP TABLE IF EXISTS project_technology;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS architectures;
DROP TABLE IF EXISTS technologies;
DROP TABLE IF EXISTS change_email_tokens;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS register_tokens;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP SEQUENCE IF EXISTS global_seq;

CREATE SEQUENCE global_seq START WITH 100000;

CREATE TABLE users
(
    id         BIGINT       DEFAULT nextval('global_seq') PRIMARY KEY,
    email      VARCHAR(128) NOT NULL,
    name       VARCHAR(32)  NOT NULL,
    password   VARCHAR(128) NOT NULL,
    enabled    BOOL         DEFAULT TRUE NOT NULL,
    registered TIMESTAMP    DEFAULT now() NOT NULL,
    avatar_file_name VARCHAR(128),
    avatar_file_link VARCHAR(512)
);
CREATE UNIQUE INDEX users_unique_email_idx ON users (email);

CREATE TABLE user_roles
(
    user_id BIGINT     NOT NULL,
    role    VARCHAR(9) NOT NULL,
    CONSTRAINT user_roles_unique_idx UNIQUE (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE register_tokens
(
    id          BIGINT       DEFAULT nextval('global_seq') PRIMARY KEY,
    token       VARCHAR(36)  NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    email       VARCHAR(128) NOT NULL,
    name        VARCHAR(32)  NOT NULL,
    password    VARCHAR(128) NOT NULL
);
CREATE UNIQUE INDEX register_tokens_unique_email_idx ON register_tokens (email);

CREATE TABLE password_reset_tokens
(
    id          BIGINT      DEFAULT nextval('global_seq') PRIMARY KEY,
    token       VARCHAR(36) NOT NULL,
    expiry_date TIMESTAMP   NOT NULL,
    user_id     BIGINT      NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX password_reset_tokens_unique_user_idx ON password_reset_tokens (user_id);

CREATE TABLE change_email_tokens
(
    id          BIGINT       DEFAULT nextval('global_seq') PRIMARY KEY,
    token       VARCHAR(36)  NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    new_email   VARCHAR(128) NOT NULL,
    user_id     BIGINT       NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX change_email_tokens_unique_user_idx ON change_email_tokens (user_id);

CREATE TABLE technologies
(
    id             BIGINT       DEFAULT nextval('global_seq') PRIMARY KEY,
    name           VARCHAR(32)  NOT NULL,
    url            VARCHAR(512) NOT NULL,
    usage          VARCHAR(16)  NOT NULL,
    priority       VARCHAR(16)  NOT NULL,
    logo_file_name VARCHAR(128) NOT NULL,
    logo_file_link VARCHAR(512) NOT NULL
);
CREATE UNIQUE INDEX technologies_unique_name_idx ON technologies (name);

CREATE TABLE architectures
(
    id               BIGINT        DEFAULT nextval('global_seq') PRIMARY KEY,
    name             VARCHAR(32)   NOT NULL,
    description      VARCHAR(400)  NOT NULL,
    logo_file_name   VARCHAR(128) NOT NULL,
    logo_file_link   VARCHAR(512) NOT NULL
);
CREATE UNIQUE INDEX architectures_unique_name_idx ON architectures (name);

CREATE TABLE projects
(
    id                       BIGINT       DEFAULT nextval('global_seq') PRIMARY KEY,
    name                     VARCHAR(64)  NOT NULL,
    short_description        VARCHAR(128) NOT NULL,
    enabled                  BOOL         DEFAULT TRUE NOT NULL,
    priority                 VARCHAR(16)  NOT NULL,
    start_date               DATE         NOT NULL,
    end_date                 DATE         NOT NULL,
    architecture_id          BIGINT       NOT NULL,
    logo_file_name           VARCHAR(128) NOT NULL,
    logo_file_link           VARCHAR(512) NOT NULL,
    docker_compose_file_name VARCHAR(128),
    docker_compose_file_link VARCHAR(512),
    card_image_file_name     VARCHAR(128) NOT NULL,
    card_image_file_link     VARCHAR(512) NOT NULL,
    deployment_url           VARCHAR(512),
    backend_src_url          VARCHAR(512) NOT NULL,
    frontend_src_url         VARCHAR(512),
    open_api_url             VARCHAR(512),
    views                    INTEGER      DEFAULT 0 NOT NULL,
    FOREIGN KEY (architecture_id) REFERENCES architectures (id)
);
CREATE UNIQUE INDEX projects_unique_name_idx ON projects (name);

CREATE TABLE project_technology
(
    project_id    BIGINT       NOT NULL,
    technology_id BIGINT       NOT NULL,
    FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    FOREIGN KEY (technology_id) REFERENCES technologies (id)
);
CREATE UNIQUE INDEX project_technology_unique_project_technology_idx ON project_technology (project_id, technology_id);

CREATE TABLE description_elements
(
    id           BIGINT       DEFAULT nextval('global_seq') PRIMARY KEY,
    index        SMALLINT     NOT NULL,
    type         VARCHAR(16)  NOT NULL,
    text         VARCHAR(1024),
    file_name    VARCHAR(128),
    file_link    VARCHAR(512),
    project_id   BIGINT       NOT NULL,
    FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
);
