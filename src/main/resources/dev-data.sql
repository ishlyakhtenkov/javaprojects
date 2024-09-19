INSERT INTO technologies (name, url, usage, priority, logo_file_name, logo_file_link)
VALUES ('Apache Tomcat', 'https://tomcat.com', 'BACKEND', 'HIGH', 'apache_tomcat.svg', './content/technologies/apache_tomcat/apache_tomcat.svg'), --100026
       ('Apache Maven', 'https://maven.com', 'BACKEND', 'HIGH', 'apache_maven.svg', './content/technologies/apache_maven/apache_maven.svg'), --100027
       ('Bootstrap 5', 'https://bootstrap.com', 'FRONTEND', 'HIGH', 'bootstrap5.svg', './content/technologies/bootstrap5/bootstrap5.svg'), --100028
       ('Elastic Search', 'https://elk.com', 'BACKEND', 'MEDIUM', 'elastic.svg', './content/technologies/elastic/elastic.svg'), --100029
       ('Hibernate', 'https://hibernate.com', 'BACKEND', 'VERY_HIGH', 'hibernate.svg', './content/technologies/hibernate/hibernate.svg'), --100030
       ('HTML 5', 'https://html5.com', 'FRONTEND', 'MEDIUM', 'html5.svg', './content/technologies/html5/html5.svg'), --100031
       ('Javascript', 'https://javascript.com', 'FRONTEND', 'MEDIUM', 'javascript.svg', './content/technologies/javascript/javascript.svg'), --100032
       ('jQuery', 'https://jQuery.com', 'FRONTEND', 'MEDIUM', 'jquery.svg', './content/technologies/jquery/jquery.svg'), --100033
       ('JUnit 5', 'https://junit.com', 'BACKEND', 'MEDIUM', 'junit.svg', './content/technologies/junit/junit.svg'), --100034
       ('OpenAPI 3', 'https://openapi.com', 'BACKEND', 'LOW', 'openapi.svg', './content/technologies/openapi/openapi.svg'), --100035
       ('PostgreSQL', 'https://postgresql.com', 'BACKEND', 'MEDIUM', 'postgresql.svg', './content/technologies/postgresql/postgresql.svg'), --100036
       ('Rabbit MQ', 'https://rabbitmq.com', 'BACKEND', 'MEDIUM', 'rabbitmq.svg', './content/technologies/rabbitmq/rabbitmq.svg'), --100037
       ('Spring Boot', 'https://springboot.com', 'BACKEND', 'VERY_HIGH', 'spring_boot.svg', './content/technologies/spring_boot/spring_boot.svg'), --100038
       ('JSON Web Token', 'https://jwt.com', 'BACKEND', 'VERY_HIGH', 'jwt.svg', './content/technologies/jwt/jwt.svg'), --100039
       ('REST API', 'https://rest-api.com', 'BACKEND', 'LOW', 'rest_api.svg', './content/technologies/rest_api/rest_api.svg'), --100040
       ('Spring Security', 'https://spring-security.com', 'BACKEND', 'VERY_HIGH', 'spring_security.svg', './content/technologies/spring_security/spring_security.svg'), --100041
       ('Typescript', 'https://typescript.com', 'FRONTEND', 'LOW', 'typescript.svg', './content/technologies/typescript/typescript.svg'), --100042
       ('Spring Data JPA', 'https://sping-data.com', 'BACKEND', 'VERY_HIGH', 'spring_data_jpa.svg', './content/technologies/spring_data_jpa/spring_data_jpa.svg'); --100043


INSERT INTO architectures (name, description)
VALUES ('Layered Monolith', 'A layered architecture is a way of organizing the components of a software system into distinct layers, each of which performs a specific task or set of tasks.'); --100044

INSERT INTO projects (name, short_description, enabled, priority, start_date, end_date, architecture_id, logo_file_name, logo_file_link,
                      docker_compose_file_name, docker_compose_file_link, card_image_file_name, card_image_file_link,
                      deployment_url, backend_src_url, frontend_src_url, open_api_url)
VALUES ('Учет альбомов КД', 'Приложение используется работниками архива документов для учета альбомов конструкторской документации.',
        true, 'HIGH', '2021-06-12', '2021-07-01', 100044, 'album_accounting_logo.png', './content/projects/album_accounting/logo/album_accounting_logo.png',
        'docker-compose.yaml', './content/projects/album_accounting/docker/docker-compose.yaml',
        'album_accounting_card_img.png','./content/projects/album_accounting/card_img/album_accounting_card_img.png',
        'https://projector.ru/albumaccounting', 'https://github.com/ishlyakhtenkov/albumaccounting',
        'https://github.com/ishlyakhtenkov/angular-albumaccounting', 'https://projector.ru/albumaccounting/swagger-ui.html'); --100045

INSERT INTO project_technology (project_id, technology_id)
VALUES (100045, 100011),
       (100045, 100012),
       (100045, 100013),
       (100045, 100026),
       (100045, 100027),
       (100045, 100028),
       (100045, 100030),
       (100045, 100031),
       (100045, 100034),
       (100045, 100035),
       (100045, 100036),
       (100045, 100038),
       (100045, 100039),
       (100045, 100040),
       (100045, 100041),
       (100045, 100042),
       (100045, 100043);

INSERT INTO description_elements (index, type, text, file_name, file_link, project_id)
VALUES (0, 'PARAGRAPH', 'Данное приложение используется работниками архива для учета альбомов конструкторской документации, ' ||
                        'поступающих в архив на хранение, и выдаваемых разработчикам для личного пользования, а также позволяет' ||
                        ' определять текущее местонахождение требующегося альбома.', null, null, 100045),
       (1, 'PARAGRAPH', 'Под альбомом в данном контексте понимается некоторое количество взаимосвязанных конструкторских документов, ' ||
                     'распечатанных на бумаге и переплетенных в книгу. Каждый альбом имеет децимальный номер, равный децимальному ' ||
                     'номеру главного документа в альбоме, а также "штамп"- специальное обозначение, определяющее производственную ' ||
                     'область применения альбома.', null, null, 100045),
       (2, 'PARAGRAPH', 'В приложении используется три вида пользовательских ролей: анонимные пользователи, работники архива и ' ||
                     'администраторы.', null, null, 100045),
       (3, 'TITLE', 'Информация об альбомах', null, null, 100045),
       (4, 'PARAGRAPH', 'Анонимным пользователям доступна возможность просматривать информацию об имеющихся в архиве альбомах ' ||
                        'конструкторской документации, а также осуществлять поиск альбомов по их децимальным номерам. ' ||
                        'Поддерживается поиск альбомов по имени текущего держателя, что удобно использовать при необходимости ' ||
                        'проверки какие альбомы числятся за тем или иным разработчиком.', null, null, 100045),
       (5, 'IMAGE', null, 'album-list-anon-user.png','./content/projects/album_accounting/description/images/album-list-anon-user.png', 100045),
       (6, 'TITLE', 'Работники архива', null, null, 100045),
       (7, 'PARAGRAPH', 'Интерфейс приложения расширяется для пользователей с ролью "Работник архива".', null, null, 100045),
       (8, 'PARAGRAPH', 'Помимо базовой информации об альбомах, а также благодаря тому, что все альбомы в архиве упорядочены ' ||
                        'по возрастанию их децимальных номеров, работникам архива предоставляются сведения о том, ' ||
                        'на каком месте (номер стеллажа/номер полки) должен находится тот или иной альбом.', null, null, 100045),
       (9, 'IMAGE', null, 'album-list-arch-user.png','./content/projects/album_accounting/description/images/album-list-arch-user.png', 100045),
       (10, 'PARAGRAPH', 'Кроме просмотра данных об уже имеющихся в архиве альбомах работники архива имеют право добавлять ' ||
                         'в приложение данные о новых альбомах, а также редактировать информацию об уже имеющихся, в том числе ' ||
                         'указывать отдел и разработчика, которому был выдан альбом. При необходимости работник архива может ' ||
                         'удалять данные об альбомах.', null, null, 100045),
       (11, 'IMAGE', null, 'album_management.png','./content/projects/album_accounting/description/images/album_management.png', 100045),
       (12, 'PARAGRAPH', 'Помимо действий с альбомами работники архива могут просматривать информацию о разработчиках ' ||
                         'предприятия, выбирая интересующий отдел. Им доступна возможность добавлять новые данные о ' ||
                         'разработчиках, редактировать и удалять имеющиеся. В дальнейшем эта информация используется ' ||
                         'работниками архива для выдачи альбомов разработчикам.', null, null, 100045),
       (13, 'IMAGE', null, 'employee-list.png','./content/projects/album_accounting/description/images/employee-list.png', 100045),
       (14, 'TITLE', 'Профиль', null, null, 100045),
       (15, 'PARAGRAPH', 'Любому авторизованному пользователю приложения доступна возможность просмотреть свой профиль, ' ||
                         'а также сменить текущий пароль.', null, null, 100045),
       (16, 'IMAGE', null, 'profile.png','./content/projects/album_accounting/description/images/profile.png', 100045),
       (17, 'TITLE', 'Задачи администраторов', null, null, 100045),
       (18, 'PARAGRAPH', 'У администраторов имеются все возможности, перечисленные для работников архива. Кроме того ' ||
                         'одной из их задач является работа по вводу данных касательно отделов предприятия. Администраторы ' ||
                         'имеют возможность просматривать информацию об отделах, вносить в приложение данные о новых ' ||
                         'отделах, редактировать и удалять информацию об уже имеющихся.', null, null, 100045),
       (19, 'IMAGE', null, 'department-list.png','./content/projects/album_accounting/description/images/department-list.png', 100045),
       (20, 'PARAGRAPH', 'Однако основной задачей администраторов является менеджмент пользователей приложения.', null, null, 100045),
       (21, 'PARAGRAPH', 'Администраторы имеют возможность просматривать информацию о пользователях с опцией поиска по ' ||
                         'имени или адресу электронной почты.', null, null, 100045),
       (22, 'IMAGE', null, 'user-list.png','./content/projects/album_accounting/description/images/user-list.png', 100045),
       (23, 'PARAGRAPH', 'Они имеют право создавать новых пользователей, назначая им соответствующие права доступа, ' ||
                         'редактировать информацию об уже имеющихся пользователях, блокировать и удалять их учетные записи. ' ||
                         'Им доступна возможность изменять пароли пользователей, что удобно использовать, если ' ||
                         'пользователь не может вспомнить свой текущий пароль.', null, null, 100045),
       (24, 'IMAGE', null, 'admin-management.png','./content/projects/album_accounting/description/images/admin-management.png', 100045);




















