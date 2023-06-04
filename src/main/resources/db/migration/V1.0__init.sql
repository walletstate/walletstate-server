CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users
(
    id         VARCHAR(255)             NOT NULL PRIMARY KEY,
    username   VARCHAR(255)             NOT NULL,
    namespace  UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE namespaces
(
    id         UUID                     NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    name       VARCHAR(255)             NOT NULL,
    created_by VARCHAR(255)             NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL             DEFAULT NOW(),
    CONSTRAINT namespaces_created_by_fk FOREIGN KEY (created_by) REFERENCES users (id)
);

ALTER TABLE users
    ADD CONSTRAINT users_namespace_fk FOREIGN KEY (namespace) REFERENCES namespaces (id);

create table namespace_invites
(
    id          UUID                     NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    namespace   UUID                     NOT NULL,
    invite_code VARCHAR(24)              NOT NULL,
    created_by  VARCHAR(255)             NOT NULL,
    valid_to    TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL             DEFAULT NOW(),
    CONSTRAINT invites_namespace_fk FOREIGN KEY (namespace) REFERENCES namespaces (id),
    CONSTRAINT invites_created_by_fk FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE accounts
(
    id         UUID                     NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    namespace  UUID                     NOT NULL,
    name       VARCHAR(255)             NOT NULL,
    created_by VARCHAR(255)             NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL             DEFAULT NOW(),
    CONSTRAINT accounts_namespace_fk FOREIGN KEY (namespace) REFERENCES namespaces (id),
    CONSTRAINT accounts_created_by_fk FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE categories
(
    id         UUID                     NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    namespace  UUID                     NOT NULL,
    name       VARCHAR(255)             NOT NULL,
    created_by VARCHAR(255)             NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL             DEFAULT NOW(),
    CONSTRAINT categories_namespace_fk FOREIGN KEY (namespace) REFERENCES namespaces (id),
    CONSTRAINT categories_created_by_fk FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE records
(
    id          UUID                     NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    account     UUID                     NOT NULL,
    amount      DECIMAL                  NOT NULL,
    type        varchar(35)              NOT NULL,
    category    UUID                     NOT NULL,
    description TEXT,
    time        TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by  VARCHAR(255)             NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL             DEFAULT NOW(),
    CONSTRAINT records_account_fk FOREIGN KEY (account) REFERENCES accounts (id),
    CONSTRAINT records_category_fk FOREIGN KEY (category) REFERENCES categories (id),
    CONSTRAINT records_created_by_fk FOREIGN KEY (created_by) REFERENCES users (id)
);
