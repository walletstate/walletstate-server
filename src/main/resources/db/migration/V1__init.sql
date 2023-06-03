CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users
(
    id         VARCHAR(255) NOT NULL PRIMARY KEY,
    username   VARCHAR(255) NOT NULL,
    namespace  UUID,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE namespaces
(
    id         UUID         NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    name       TEXT         NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP                         DEFAULT NOW()
);

create table namespace_invites
(
    id           UUID         NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    namespace_id UUID         NOT NULL,
    invite_code  VARCHAR(24)  NOT NULL,
    created_by   VARCHAR(255) NOT NULL,
    valid_to     TIMESTAMP    NOT NULL,
    created_at   TIMESTAMP                         DEFAULT NOW()
);
