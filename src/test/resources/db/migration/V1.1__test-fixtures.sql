INSERT INTO users (id, username, wallet)
VALUES ('existing-user-id', 'existing-username', NULL);

INSERT INTO users (id, username, wallet)
VALUES ('existing-user-id-1', 'existing-username-1', NULL);

INSERT INTO users (id, username, wallet)
VALUES ('existing-user-id-2', 'existing-username-2', NULL);

INSERT INTO users (id, username, wallet)
VALUES ('existing-user-id-3', 'existing-username-3', NULL);

INSERT INTO wallets (id, name, created_by)
VALUES ('0f41829c-6010-4170-b8d3-49813fb50e30', 'test-wallet', 'existing-user-id');

UPDATE users
SET wallet = '0f41829c-6010-4170-b8d3-49813fb50e30'
where id = 'existing-user-id';

INSERT INTO wallet_invites (id, wallet, invite_code, created_by, valid_to)
VALUES ('2b3ce216-a0d2-4bf2-9a59-fbaeb03635e5',
        '0f41829c-6010-4170-b8d3-49813fb50e30',
        'TESTCODE',
        'existing-user-id',
        now() + interval '1 hour')
