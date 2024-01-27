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

CREATE TYPE group_type AS ENUM ('accounts', 'categories');

CREATE TABLE "groups"
(
    id             UUID         NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet         UUID         NOT NULL,
    type           group_type   NOT NULL,
    name           VARCHAR(255) NOT NULL,
    ordering_index INTEGER      NOT NULL             DEFAULT 0,
    CONSTRAINT groups_wallet_fk FOREIGN KEY (wallet) REFERENCES wallets (id)
);

CREATE TABLE accounts
(
    id             UUID          NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    "group"        UUID          NOT NULL,
    name           VARCHAR(255)  NOT NULL,
    ordering_index INTEGER       NOT NULL             DEFAULT 0,
    icon           CHAR(64),
    tags           VARCHAR(50)[] NOT NULL             DEFAULT '{}',
    CONSTRAINT accounts_groups_fk FOREIGN KEY ("group") REFERENCES groups (id)
);

CREATE TABLE categories
(
    id             UUID          NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    "group"        UUID          NOT NULL,
    name           VARCHAR(255)  NOT NULL,
    icon           CHAR(64),
    tags           VARCHAR(50)[] NOT NULL             DEFAULT '{}',
    ordering_index INTEGER       NOT NULL             DEFAULT 0,
    CONSTRAINT categories_group_fk FOREIGN KEY ("group") REFERENCES groups (id)
);

CREATE TYPE asset_type AS ENUM ('fiat', 'crypto', 'deposit', 'bond', 'stock', 'other');

CREATE TABLE assets
(
    id             UUID          NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet         UUID          NOT NULL,
    type           asset_type    NOT NULL,
    ticker         VARCHAR(25)   NOT NULL,
    name           VARCHAR(255)  NOT NULL,
    icon           CHAR(64),
    tags           VARCHAR(50)[] NOT NULL             DEFAULT '{}',
    start_date     TIMESTAMP WITH TIME ZONE           DEFAULT NULL,
    end_date       TIMESTAMP WITH TIME ZONE           DEFAULT NULL,
    denominated_in UUID                               DEFAULT NULL,
    denomination   DECIMAL(36, 18)                    DEFAULT NULL,
    CONSTRAINT assets_wallet_fk FOREIGN KEY (wallet) REFERENCES wallets (id),
    CONSTRAINT assets_denominated_in_fk FOREIGN KEY (denominated_in) REFERENCES assets (id),
    CONSTRAINT assets_wallet_ticker_uq UNIQUE (wallet, ticker)
);

CREATE TABLE exchange_rates
(
    id       UUID                     NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    "from"   UUID                     NOT NULL,
    "to"     UUID                     NOT NULL,
    rate     DECIMAL(36, 18)          NOT NULL,
    datetime TIMESTAMP WITH TIME ZONE NOT NULL             DEFAULT NOW(),
    CONSTRAINT exchange_rates_from_fk FOREIGN KEY ("from") REFERENCES assets (id),
    CONSTRAINT exchange_rates_to_fk FOREIGN KEY ("to") REFERENCES assets (id)
);

CREATE TYPE transaction_type AS ENUM ('income', 'spending', 'transfer');

CREATE TABLE transactions
(
    id           UUID                     NOT NULL DEFAULT uuid_generate_v4(),
    account      UUID                     NOT NULL,
    asset        UUID                     NOT NULL,
    amount       DECIMAL(36, 18)          NOT NULL,
    type         transaction_type         NOT NULL,
    category     UUID                     NOT NULL,
    datetime     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    description  VARCHAR(255),
    tags         VARCHAR(50)[]            NOT NULL DEFAULT '{}',
    external_id  VARCHAR(255),
    spent_on     UUID,
    generated_by UUID,
    CONSTRAINT transactions_pk PRIMARY KEY (id, account, asset),
    CONSTRAINT transactions_account_fk FOREIGN KEY (account) REFERENCES accounts (id),
    CONSTRAINT transactions_asset_fk FOREIGN KEY (asset) REFERENCES assets (id),
    CONSTRAINT transactions_category_fk FOREIGN KEY (category) REFERENCES categories (id),
    CONSTRAINT transactions_spent_on_fk FOREIGN KEY (spent_on) REFERENCES assets (id),
    CONSTRAINT transactions_generated_by_fk FOREIGN KEY (generated_by) REFERENCES assets (id)
);

-- Simple implementation to store icons in base64 encoded string.
-- There are no foreign keys to icons and from icons because it is one of the possible implementations of icon storage.
-- This implementation is not optimal but it doesn't require any dependencies to store icons and is sufficient for small load.
CREATE TABLE icons
(
    wallet  UUID     NOT NULL,
    id      CHAR(64) NOT NULL,
    content TEXT     NOT NULL,
    CONSTRAINT icons_pk PRIMARY KEY (wallet, id)
);
