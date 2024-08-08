DELETE FROM register_tokens;
DELETE FROM user_roles;
DELETE FROM users;
ALTER SEQUENCE global_seq RESTART WITH 100000;

INSERT INTO users (email, name, password, enabled)
VALUES ('user@gmail.com','John Doe', '{noop}password', true),
       ('admin@gmail.com','Jack', '{noop}admin', true),
       ('userDisabled@gmail.com','Freeman25', '{noop}password', false);

INSERT INTO user_roles (role, user_id)
VALUES ('USER', 100000),
       ('USER', 100001),
       ('ADMIN', 100001),
       ('USER', 100002);

INSERT INTO register_tokens (token, expiry_date, email, name, password)
VALUES ('5a99dd09-d23f-44bb-8d41-b6ff44275d01', '2024-08-06 19:35:56', 'some@gmail.com', 'someName', '{noop}somePassword'),
       ('52bde839-9779-4005-b81c-9131c9590d79', '2052-05-24 16:42:03', 'new@gmail.com', 'newName', '{noop}newPassword');