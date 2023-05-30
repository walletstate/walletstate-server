CREATE DATABASE walletstate;

\c walletstate;

create table users
(
    id         varchar(255) not null primary key,
    username   varchar(255) not null,
    namespace  uuid,
    created_at timestamp default now()
);

create table namespaces
(
    id         uuid         not null primary key,
    name       text         not null,
    created_by varchar(255) not null,
    created_at timestamp default now()
);

create table namespace_invites
(
    id           uuid         not null primary key,
    namespace_id uuid         not null,
    invite_code  varchar(24)  not null,
    created_by   varchar(255) not null,
    valid_to     timestamp    not null
);
