MERGE INTO genre (genre_id, genre_name) KEY(genre_id)
VALUES
  (1, 'Комедия'),
  (2, 'Драма'),
  (3, 'Мультфильм'),
  (4, 'Триллер'),
  (5, 'Документальный'),
  (6, 'Боевик');

MERGE INTO mpa (mpa_id, mpa_name) KEY(mpa_id)
VALUES
    (1, 'G'),
    (2, 'PG'),
    (3, 'PG-13'),
    (4, 'R'),
    (5, 'NC-17');

INSERT INTO users (id, email, login, name, birthday)
VALUES
    (1, 'user@mail.com', 'user_login', 'Test User', '1990-01-01');

INSERT INTO users (email, login, name, birthday)
VALUES
    ('user2@mail.com', 'user_login2', 'Second User', '1991-02-02'),
    ('user3@mail.com', 'user_login3', 'Third User', '1992-03-03');
