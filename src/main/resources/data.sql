DELETE FROM description_elements;
DELETE FROM project_technology;
DELETE FROM projects;
DELETE FROM architectures;
DELETE FROM technologies;
DELETE FROM change_email_tokens;
DELETE FROM password_reset_tokens;
DELETE FROM register_tokens;
DELETE FROM user_roles;
DELETE FROM users;
ALTER SEQUENCE global_seq RESTART WITH 100000;

INSERT INTO users (email, name, information, password, enabled, avatar_file_name, avatar_file_link)
VALUES ('user@gmail.com','John Doe', 'Some info', '{noop}password', true, 'cool_user.jpg', './content/avatars/user@gmail.com/cool_user.jpg'),
       ('admin@gmail.com','Jack', 'Java developer with 10 years of production experience.', '{noop}admin', true, 'admin.jpg', './content/avatars/admin@gmail.com/admin.jpg'),
       ('user2@gmail.com','Alice Key', null, '{noop}somePassword', true, 'cat.jpg', './content/avatars/user2@gmail.com/cat.jpg'),
       ('userDisabled@gmail.com','Freeman25', null, '{noop}password', false, null, null);

INSERT INTO user_roles (role, user_id)
VALUES ('USER', 100000),
       ('USER', 100001),
       ('ADMIN', 100001),
       ('USER', 100002),
       ('USER', 100003);

INSERT INTO register_tokens (token, expiry_date, email, name, password)
VALUES ('5a99dd09-d23f-44bb-8d41-b6ff44275d01', '2024-08-06 19:35:56', 'some@gmail.com', 'someName', '{noop}somePassword'),
       ('52bde839-9779-4005-b81c-9131c9590d79', '2052-05-24 16:42:03', 'new@gmail.com', 'newName', '{noop}newPassword');

INSERT INTO password_reset_tokens (token, expiry_date, user_id)
VALUES ('5a99dd09-d23f-44bb-8d41-b6ff44275x97', '2052-02-05 12:10:00', 100000),
       ('52bde839-9779-4005-b81c-9131c9590b41', '2022-02-06 19:35:56', 100002),
       ('54ghh534-9778-4005-b81c-9131c9590c63', '2052-04-25 13:48:14', 100003);

INSERT INTO change_email_tokens (token, expiry_date, new_email, user_id)
VALUES ('5a49dd09-g23f-44bb-8d41-b6ff44275s56', '2024-08-05 21:49:01', 'some@gmail.com', 100001),
       ('1a43dx02-x23x-42xx-8r42-x6ff44275y67', '2052-01-22 06:17:32', 'someNew@gmail.com', 100002);

INSERT INTO technologies (name, url, usage, priority, logo_file_name, logo_file_link)
VALUES ('Java', 'https://www.oracle.com/java', 'BACKEND', 'ULTRA', 'java.svg', './content/technologies/java/java.svg'),
       ('Spring Framework', 'https://spring.io', 'BACKEND', 'VERY_HIGH', 'spring_framework.svg', './content/technologies/spring_framework/spring_framework.svg'),
       ('Angular', 'https://angular.dev', 'FRONTEND', 'HIGH', 'angular.svg', './content/technologies/angular/angular.svg'),
       ('Thymeleaf', 'https://www.thymeleaf.org', 'BACKEND', 'MEDIUM', 'thymeleaf.png', './content/technologies/thymeleaf/thymeleaf.png');

INSERT INTO architectures (name, description, logo_file_name, logo_file_link)
VALUES ('Modular Monolith', 'A modular monolith is an architectural pattern that structures the application ' ||
                            'into independent modules or components with well-defined boundaries.', 'modular_monolith.png',
        './content/architectures/modular_monolith/modular_monolith.png'),
       ('Microservices', 'Microservices architecture allow a large application to be separated into smaller ' ||
                         'independent parts, with each part having its own realm of responsibility.', 'microservices.png',
        './content/architectures/microservices/microservices.png');

INSERT INTO projects (name, annotation, visible, priority, created, started, finished, architecture_id, logo_file_name, logo_file_link,
                      docker_compose_file_name, docker_compose_file_link, preview_file_name, preview_file_link,
                      deployment_url, backend_src_url, frontend_src_url, open_api_url, views, user_id)
VALUES ('Restaurant aggregator', 'The app offers users to get information about restaurants and vote for their favorite one.',
        true, 'ULTRA', '2021-07-02 11:24:54', '2021-03-24', '2021-05-02', 100015, 'restaurant_aggregator_logo.png', './content/projects/user@gmail.com/restaurant_aggregator/logo/restaurant_aggregator_logo.png',
        'docker-compose.yaml', './content/projects/user@gmail.com/restaurant_aggregator/docker/docker-compose.yaml',
        'restaurant_aggregator_preview.png','./content/projects/user@gmail.com/restaurant_aggregator/preview/restaurant_aggregator_preview.png',
        'https://projector.ru/restaurant-aggregator', 'https://github.com/ishlyakhtenkov/votingsystem',
        'https://github.com/ishlyakhtenkov/angular-votingsystem', 'https://projector.ru/restaurant-aggregator/swagger-ui.html', 12, 100000),

       ('Skill aggregator', 'The app creates a list of required key skills for a user-specified profession.', true, 'VERY_HIGH', '2022-09-27 21:15:11', '2022-07-17', '2022-09-23',
        100015, 'skill_aggregator_logo.png', './content/projects/admin@gmail.com/skill_aggregator/logo/skill_aggregator_logo.png', 'docker-compose.yaml',
        './content/projects/admin@gmail.com/skill_aggregator/docker/docker-compose.yaml', 'skill_aggregator_preview.png',
        './content/projects/admin@gmail.com/skill_aggregator/preview/skill_aggregator_preview.png', 'https://projector.ru/skill-aggregator',
        'https://github.com/ishlyakhtenkov/skillaggregator', null, null, 21, 100001),

       ('Copy maker', 'The app creates copies of electronic documents by analyzing selected invoices and documentation inventories.',
        false, 'MEDIUM', '2023-01-05 13:55:21', '2022-10-11', '2022-12-29', 100015, 'copy_maker_logo.png', './content/projects/user@gmail.com/copy_maker/logo/copy_maker_logo.png', null,
        null, 'copy_maker_preview.png', './content/projects/user@gmail.com/copy_maker/preview/copy_maker_preview.png', null,
        'https://github.com/ishlyakhtenkov/doccopymaker', null, null, 7, 100000);

INSERT INTO project_technology (project_id, technology_id)
VALUES (100017, 100011),
       (100017, 100012),
       (100017, 100013),
       (100018, 100011),
       (100018, 100012),
       (100018, 100013),
       (100019, 100011),
       (100019, 100012);

INSERT INTO description_elements (index, type, text, file_name, file_link, project_id)
VALUES (0, 'TITLE', 'App description', null, null, 100017),
       (1, 'PARAGRAPH', 'This application allows users to receive information about restaurants and their daily lunch menus, as well as vote for their favorite restaurant once a day.', null, null, 100017),
       (2, 'IMAGE', null, 'restaurant_aggregator_schema.png','./content/projects/user@gmail.com/restaurant_aggregator/description/images/restaurant_aggregator_schema.png', 100017),
       (3, 'TITLE', 'Registration, profile', null, null, 100017),
       (4, 'PARAGRAPH', 'Users can register for the app by filling in their account details on the registration page.', null, null, 100017),
       (5, 'IMAGE', null, 'registration_and_profile.png','./content/projects/user@gmail.com/restaurant_aggregator/description/images/registration_and_profile.png', 100017);

INSERT INTO likes (object_id, user_id, object_type)
VALUES (100017, 100000, 'PROJECT'),
       (100017, 100001, 'PROJECT'),
       (100017, 100002, 'PROJECT'),
       (100017, 100003, 'PROJECT'),
       (100018, 100000, 'PROJECT'),
       (100018, 100001, 'PROJECT');

INSERT INTO comments (project_id, user_id, parent_id, text, created, updated, deleted)
VALUES (100017, 100001, null, 'admin 1st comment', '2024-09-11 11:44:56', null, false),
       (100017, 100001, null, 'admin 2nd comment', '2024-09-11 12:35:44', '2024-09-11 13:21:32', false),
       (100017, 100000, null, 'user 1st comment', '2024-09-11 11:55:37', null, false),
       (100017, 100000, 100032, 'user 2nd comment for admin 1st comment', '2024-09-11 11:57:23', null, false),
       (100017, 100000, 100035, 'user 3rd comment for its user 2nd comment', '2024-09-11 12:14:13', null, false),
       (100017, 100000, 100032, 'user 4th comment deleted', '2024-09-11 13:18:53', null, true),
       (100018, 100001, null, 'admin comment for project 2', '2024-09-11 14:15:39', null, false);

INSERT INTO likes (object_id, user_id, object_type)
VALUES (100032, 100001, 'COMMENT'),
       (100032, 100000, 'COMMENT'),
       (100032, 100002, 'COMMENT'),
       (100035, 100001, 'COMMENT'),
       (100035, 100002, 'COMMENT'),
       (100038, 100000, 'COMMENT');