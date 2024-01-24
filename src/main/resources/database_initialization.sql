create table users
(
    id BIGSERIAL,
    first_name varchar(255) NOT NULL ,
    last_name varchar(255) NOT NULL,
    age INTEGER NOT NULL,

    CONSTRAINT users_PK PRIMARY KEY(id)
);
create table notes
(
    id BIGSERIAL,
    title varchar(255) NOT NULL ,
    body varchar(255) NOT NULL,
    user_id BIGINT NOT NULL,

    CONSTRAINT notes_PK PRIMARY KEY(id),
    CONSTRAINT notes_users_FK FOREIGN KEY(user_id) REFERENCES users
);
INSERT INTO users(first_name, last_name, money)
VALUES ('aaa@', 'aaa', 100),
       ('bbb@', 'bbb', 110),
       ('ccc@', 'ccc', 120),
       ('ddd@', 'ddd', 130),
       ('eee@', 'eee', 140),
       ('fff@', 'fff', 150);
INSERT INTO notes(title, body, user_id)
VALUES ('Title_1', 'Body_1', 1),
       ('Title_2', 'Body_2', 1),
       ('Title_3', 'Body_3', 2),
       ('Title_4', 'Body_4', 2),
       ('Title_5', 'Body_5', 3),
       ('Title_6', 'Body_6', 3),
       ('Title_7', 'Body_7', 4),
       ('Title_8', 'Body_8', 5),
       ('Title_9', 'Body_9', 6);

