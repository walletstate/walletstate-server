CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users
(
    id         VARCHAR(255)             NOT NULL PRIMARY KEY,
    username   VARCHAR(255)             NOT NULL,
    wallet     UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE wallets
(
    id         UUID                     NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    name       VARCHAR(255)             NOT NULL,
    created_by VARCHAR(255)             NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL             DEFAULT NOW(),
    CONSTRAINT wallets_created_by_fk FOREIGN KEY (created_by) REFERENCES users (id)
);

ALTER TABLE users
    ADD CONSTRAINT users_wallet_fk FOREIGN KEY (wallet) REFERENCES wallets (id);

CREATE TABLE wallet_invites
(
    id          UUID                     NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet      UUID                     NOT NULL,
    invite_code VARCHAR(24)              NOT NULL,
    created_by  VARCHAR(255)             NOT NULL,
    valid_to    TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL             DEFAULT NOW(),
    CONSTRAINT invites_wallet_fk FOREIGN KEY (wallet) REFERENCES wallets (id),
    CONSTRAINT invites_created_by_fk FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE accounts_groups
(
    id             UUID                     NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet         UUID                     NOT NULL,
    name           VARCHAR(255)             NOT NULL,
    ordering_index INTEGER                  NOT NULL             DEFAULT 0,
    created_by     VARCHAR(255)             NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL             DEFAULT NOW(),
    CONSTRAINT accounts_groups_wallet_fk FOREIGN KEY (wallet) REFERENCES wallets (id),
    CONSTRAINT accounts_groups_created_by_fk FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE accounts
(
    id             UUID                     NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    "group"        UUID                     NOT NULL,
    name           VARCHAR(255)             NOT NULL,
    ordering_index INTEGER                  NOT NULL             DEFAULT 0,
    icon           TEXT,
    created_by     VARCHAR(255)             NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL             DEFAULT NOW(),
    CONSTRAINT accounts_accounts_groups_fk FOREIGN KEY ("group") REFERENCES accounts_groups (id),
    CONSTRAINT accounts_created_by_fk FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE categories
(
    id         UUID                     NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet     UUID                     NOT NULL,
    name       VARCHAR(255)             NOT NULL,
    created_by VARCHAR(255)             NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL             DEFAULT NOW(),
    CONSTRAINT categories_wallet_fk FOREIGN KEY (wallet) REFERENCES wallets (id),
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
