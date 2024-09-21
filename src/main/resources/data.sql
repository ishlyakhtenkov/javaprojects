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

INSERT INTO users (email, name, password, enabled)
VALUES ('user@gmail.com','John Doe', '{noop}password', true),
       ('admin@gmail.com','Jack', '{noop}admin', true),
       ('user2@gmail.com','Alice Key', '{noop}somePassword', true),
       ('userDisabled@gmail.com','Freeman25', '{noop}password', false);

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

INSERT INTO architectures (name, description, file_name, file_link)
VALUES ('Modular Monolith', 'A modular monolith is an architectural pattern that structures the application ' ||
                            'into independent modules or components with well-defined boundaries.', 'modular_monolith.png',
        './content/architectures/modular_monolith/modular_monolith.png'),
       ('Microservices', 'Microservices architecture allow a large application to be separated into smaller ' ||
                         'independent parts, with each part having its own realm of responsibility.', 'microservices.png',
        './content/architectures/microservices/microservices.png');

INSERT INTO projects (name, short_description, enabled, priority, start_date, end_date, architecture_id, logo_file_name, logo_file_link,
                      docker_compose_file_name, docker_compose_file_link, card_image_file_name, card_image_file_link,
                      deployment_url, backend_src_url, frontend_src_url, open_api_url)
VALUES ('Restaurant aggregator', 'The app offers users to get information about restaurants and vote for their favorite one.',
        true, 'ULTRA', '2021-03-24', '2021-05-02', 100015, 'restaurant_aggregator_logo.png', './content/projects/restaurant_aggregator/logo/restaurant_aggregator_logo.png',
        'docker-compose.yaml', './content/projects/restaurant_aggregator/docker/docker-compose.yaml',
        'restaurant_aggregator_card_img.png','./content/projects/restaurant_aggregator/card_img/restaurant_aggregator_card_img.png',
        'https://projector.ru/restaurant-aggregator', 'https://github.com/ishlyakhtenkov/votingsystem',
        'https://github.com/ishlyakhtenkov/angular-votingsystem', 'https://projector.ru/restaurant-aggregator/swagger-ui.html'),

       ('Skill aggregator', 'The app creates a list of required key skills for a user-specified profession.', true, 'VERY_HIGH', '2022-07-17', '2022-09-23',
        100015, 'skill_aggregator_logo.png', './content/projects/skill_aggregator/logo/skill_aggregator_logo.png', 'docker-compose.yaml',
        './content/projects/skill_aggregator/docker/docker-compose.yaml', 'skill_aggregator_card_img.png',
        './content/projects/skill_aggregator/card_img/skill_aggregator_card_img.png', 'https://projector.ru/skill-aggregator',
        'https://github.com/ishlyakhtenkov/skillaggregator', null, null),

       ('Copy maker', 'The app creates copies of electronic documents by analyzing selected invoices and documentation inventories.',
        false, 'MEDIUM', '2022-10-11', '2022-12-29', 100015, 'copy_maker_logo.png', './content/projects/copy_maker/logo/copy_maker_logo.png', null,
        null, 'copy_maker_card_img.png', './content/projects/copy_maker/card_img/copy_maker_card_img.png', null,
        'https://github.com/ishlyakhtenkov/doccopymaker', null, null);

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
       (2, 'IMAGE', null, 'restaurant_aggregator_schema.png','./content/projects/restaurant_aggregator/description/images/restaurant_aggregator_schema.png', 100017),
       (3, 'TITLE', 'Registration, profile', null, null, 100017),
       (4, 'PARAGRAPH', 'Users can register for the app by filling in their account details on the registration page.', null, null, 100017),
       (5, 'IMAGE', null, 'registration_and_profile.png','./content/projects/restaurant_aggregator/description/images/registration_and_profile.png', 100017);
