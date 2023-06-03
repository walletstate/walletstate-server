INSERT INTO users (id, username, namespace)
VALUES ('existing-user-id', 'existing-username', NULL);

INSERT INTO users (id, username, namespace)
VALUES ('existing-user-id-1', 'existing-username-1', NULL);

INSERT INTO users (id, username, namespace)
VALUES ('existing-user-id-2', 'existing-username-2', NULL);

INSERT INTO users (id, username, namespace)
VALUES ('existing-user-id-3', 'existing-username-3', NULL);

INSERT INTO namespaces (id, name, created_by)
VALUES ('0f41829c-6010-4170-b8d3-49813fb50e30', 'test-namespace', 'existing-user-id');

UPDATE users
SET namespace = '0f41829c-6010-4170-b8d3-49813fb50e30'
where id = 'existing-user-id';

INSERT INTO namespace_invites (id, namespace_id, invite_code, created_by, valid_to)
VALUES ('2b3ce216-a0d2-4bf2-9a59-fbaeb03635e5',
        '0f41829c-6010-4170-b8d3-49813fb50e30',
        'TESTCODE',
        'existing-user-id',
        now() + interval '1 hour')
