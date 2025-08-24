-- Чистим все таблицы
TRUNCATE TABLE review_like CASCADE;
TRUNCATE TABLE review CASCADE;
TRUNCATE TABLE film_directors CASCADE;
TRUNCATE TABLE directors CASCADE;
TRUNCATE TABLE film_genre CASCADE;
TRUNCATE TABLE likes CASCADE;
TRUNCATE TABLE friendship CASCADE;
TRUNCATE TABLE films CASCADE;
TRUNCATE TABLE users CASCADE;

-- Сбрасываем IDENTITY (чтобы id всегда начинались с 1)
ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1;
ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1;
ALTER TABLE directors ALTER COLUMN director_id RESTART WITH 1;
ALTER TABLE review ALTER COLUMN review_id RESTART WITH 1;

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

