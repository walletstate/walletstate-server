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

CREATE TABLE wallet_users
(
    wallet   UUID                     NOT NULL,
    "user"   VARCHAR(255)             NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT wallet_users_wallet_fk FOREIGN KEY (wallet) REFERENCES wallets (id),
    CONSTRAINT wallet_users_user_fk FOREIGN KEY ("user") REFERENCES users (id), -- remove in case of sharding by wallet id
    CONSTRAINT wallet_users_pk PRIMARY KEY (wallet, "user")
);

CREATE INDEX wallet_users_wallet_idx on wallet_users (wallet);

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

CREATE INDEX wallet_invites_wallet_idx on wallet_invites (wallet);

CREATE TYPE group_type AS ENUM ('Accounts', 'Categories', 'Assets');

CREATE TABLE "groups"
(
    id     UUID         NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet UUID         NOT NULL,
    type   group_type   NOT NULL,
    name   VARCHAR(255) NOT NULL,
    idx    INTEGER      NOT NULL             DEFAULT 0,
    CONSTRAINT groups_wallet_fk FOREIGN KEY (wallet) REFERENCES wallets (id)
);

CREATE INDEX groups_wallet_idx on "groups" (wallet);

CREATE TYPE asset_type AS ENUM ('Fiat', 'Crypto', 'Deposit', 'Bond', 'Stock', 'Other');

CREATE TABLE assets
(
    id              UUID          NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet          UUID          NOT NULL,
    "group"         UUID          NOT NULL,
    type            asset_type    NOT NULL,
    ticker          VARCHAR(25)   NOT NULL,
    name            VARCHAR(255)  NOT NULL,
    icon            CHAR(64),
    tags            VARCHAR(50)[] NOT NULL             DEFAULT '{}',
    idx             INTEGER       NOT NULL             DEFAULT 0,
    start_date      TIMESTAMP WITH TIME ZONE           DEFAULT NULL,
    end_date        TIMESTAMP WITH TIME ZONE           DEFAULT NULL,
    lock_duration   INTEGER                            DEFAULT NULL,
    unlock_duration INTEGER                            DEFAULT NULL,
    denominated_in  UUID                               DEFAULT NULL,
    denomination    DECIMAL(36, 18)                    DEFAULT NULL,
    CONSTRAINT assets_wallet_fk FOREIGN KEY (wallet) REFERENCES wallets (id),
    CONSTRAINT assets_group_fk FOREIGN KEY ("group") REFERENCES groups (id),
    CONSTRAINT assets_denominated_in_fk FOREIGN KEY (denominated_in) REFERENCES assets (id)
);

CREATE INDEX assets_wallet_idx on assets (wallet);

CREATE TABLE accounts
(
    id            UUID          NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet        UUID          NOT NULL,
    "group"       UUID          NOT NULL,
    name          VARCHAR(255)  NOT NULL,
    default_asset UUID                               DEFAULT NULL,
    idx           INTEGER       NOT NULL             DEFAULT 0,
    icon          CHAR(64),
    tags          VARCHAR(50)[] NOT NULL             DEFAULT '{}',
    external_id   VARCHAR(255)                       DEFAULT NULL,
    CONSTRAINT accounts_wallet_fk FOREIGN KEY (wallet) REFERENCES wallets (id),
    CONSTRAINT accounts_groups_fk FOREIGN KEY ("group") REFERENCES groups (id),
    CONSTRAINT accounts_assets_fk FOREIGN KEY (default_asset) REFERENCES assets (id)
);

CREATE INDEX accounts_wallet_idx on accounts (wallet);

CREATE TABLE categories
(
    id      UUID          NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet  UUID          NOT NULL,
    "group" UUID          NOT NULL,
    name    VARCHAR(255)  NOT NULL,
    icon    CHAR(64),
    tags    VARCHAR(50)[] NOT NULL             DEFAULT '{}',
    idx     INTEGER       NOT NULL             DEFAULT 0,
    CONSTRAINT categories_wallet_fk FOREIGN KEY (wallet) REFERENCES wallets (id),
    CONSTRAINT categories_group_fk FOREIGN KEY ("group") REFERENCES groups (id)
);

CREATE INDEX categories_wallet_idx on categories (wallet);

CREATE TABLE exchange_rates
(
    id       UUID                     NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet   UUID                     NOT NULL,
    "from"   UUID                     NOT NULL,
    "to"     UUID                     NOT NULL,
    rate     DECIMAL(36, 18)          NOT NULL,
    datetime TIMESTAMP WITH TIME ZONE NOT NULL             DEFAULT NOW(),
    CONSTRAINT groups_wallet_fk FOREIGN KEY (wallet) REFERENCES wallets (id),
    CONSTRAINT exchange_rates_from_fk FOREIGN KEY ("from") REFERENCES assets (id),
    CONSTRAINT exchange_rates_to_fk FOREIGN KEY ("to") REFERENCES assets (id)
);

CREATE INDEX exchange_rates_wallet_idx on exchange_rates (wallet);

CREATE TYPE record_type AS ENUM ('Income', 'Spending', 'Transfer');

CREATE TABLE records
(
    id           UUID                     NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet       UUID                     NOT NULL,
    type         record_type              NOT NULL,
    category     UUID                     NOT NULL,
    datetime     TIMESTAMP WITH TIME ZONE NOT NULL             DEFAULT NOW(),
    description  VARCHAR(255),
    tags         VARCHAR(50)[]            NOT NULL             DEFAULT '{}',
    external_id  VARCHAR(255),
    spent_on     UUID,
    generated_by UUID,
    CONSTRAINT records_wallet_fk FOREIGN KEY (wallet) REFERENCES wallets (id),
    CONSTRAINT records_category_fk FOREIGN KEY (category) REFERENCES categories (id),
    CONSTRAINT records_spent_on_fk FOREIGN KEY (spent_on) REFERENCES assets (id),
    CONSTRAINT records_generated_by_fk FOREIGN KEY (generated_by) REFERENCES assets (id)
);

CREATE INDEX records_wallet_datetime_idx ON records (wallet, datetime);
CREATE INDEX records_wallet_idx ON records (wallet);

CREATE TABLE transactions
(
    record  UUID            NOT NULL,
    account UUID            NOT NULL,
    asset   UUID            NOT NULL,
    wallet  UUID            NOT NULL,
    amount  DECIMAL(36, 18) NOT NULL,
    CONSTRAINT transactions_pk PRIMARY KEY (record, account, asset),
    CONSTRAINT transactions_wallet_fk FOREIGN KEY (wallet) REFERENCES wallets (id),
    CONSTRAINT transactions_record_fk FOREIGN KEY (record) REFERENCES records (id),
    CONSTRAINT transactions_account_fk FOREIGN KEY (account) REFERENCES accounts (id),
    CONSTRAINT transactions_asset_fk FOREIGN KEY (asset) REFERENCES assets (id)
);

CREATE INDEX transactions_wallet_idx on transactions (wallet);

-- Simple implementation to store icons in base64 encoded string.
-- There are no foreign keys to icons and from icons because it is one of the possible implementations of icon storage.
-- This implementation is not optimal but it doesn't require any dependencies to store icons and is sufficient for small load.
CREATE TABLE icons
(
    wallet       UUID                   DEFAULT NULL,
    id           CHAR(64)      NOT NULL,
    content_type VARCHAR(100)  NOT NULL,
    content      TEXT          NOT NULL,
    tags         VARCHAR(50)[] NOT NULL DEFAULT '{}',
    UNIQUE (id, wallet)
);

CREATE INDEX icons_wallet_idx on icons (wallet);
