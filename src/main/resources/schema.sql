DROP TABLE T011_FEEDS IF EXISTS;
DROP TABLE T003_LIKES IF EXISTS;
DROP TABLE T004_FRIENDS IF EXISTS;
DROP TABLE T010_REVIEW_FEEDBACKS IF EXISTS;
DROP TABLE T009_REVIEWS IF EXISTS;
DROP TABLE T007_LINKS_T001_T005 IF EXISTS;
DROP TABLE T001_FILMS IF EXISTS;
DROP TABLE T002_USERS IF EXISTS;
DROP TABLE T006_RATINGS IF EXISTS;
DROP TABLE T005_GENRES IF EXISTS;


CREATE TABLE t001_films (
  t001_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  t001_name varchar NOT NULL,
  t001_description varchar,
  t001_release_date date,
  t001_duration integer NOT NULL,
  t006_id integer
);

CREATE TABLE t002_users (
  t002_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  t002_email varchar NOT NULL,
  t002_login varchar NOT NULL,
  t002_name varchar NOT NULL,
  t002_birthday date
);

CREATE TABLE t003_likes (
  t003_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  t001_id integer NOT NULL,
  t002_id integer NOT NULL
);

CREATE TABLE t004_friends (
  t004_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  t002_target_id integer NOT NULL,
  t002_friend_id integer NOT NULL,
  t004_confirmed boolean NOT NULL
);

CREATE TABLE t005_genres (
  t005_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  t005_name varchar NOT NULL
);

CREATE TABLE t006_ratings (
  t006_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  t006_code varchar UNIQUE NOT NULL,
  t006_description varchar
);

CREATE TABLE t007_links_t001_t005 (
  t007_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  t001_id integer NOT NULL,
  t005_id integer NOT NULL
);

CREATE TABLE t009_reviews (
  t009_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  t009_content varchar,
  t009_is_positive boolean,
  t002_id integer NOT NULL,
  t001_id integer NOT NULL
);

CREATE TABLE t010_review_feedbacks (
  t010_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  t009_id integer NOT NULL,
  t002_id integer NOT NULL,
  t010_value integer
);

CREATE TABLE t011_feeds (
  t011_timestamp integer,
  t011_user_id INTEGER REFERENCES t002_users (t002_id),
  t011_event_type varchar,
  t011_operation varchar,
  t011_event_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  t011_entity_id integer NOT NULL
);

COMMENT ON TABLE t001_films IS 'Фильмы';

COMMENT ON COLUMN t001_films.t001_id IS 'ИД';

COMMENT ON COLUMN t001_films.t001_name IS 'Название';

COMMENT ON COLUMN t001_films.t001_description IS 'Описание';

COMMENT ON COLUMN t001_films.t001_release_date IS 'Дата выхода';

COMMENT ON COLUMN t001_films.t001_duration IS 'Продолжительность в минутах';

COMMENT ON COLUMN t001_films.t006_id IS 'ИД рейтинга';

COMMENT ON TABLE t002_users IS 'Пользователи';

COMMENT ON COLUMN t002_users.t002_id IS 'ИД';

COMMENT ON COLUMN t002_users.t002_email IS 'Электронная почта';

COMMENT ON COLUMN t002_users.t002_login IS 'Логин';

COMMENT ON COLUMN t002_users.t002_name IS 'Имя';

COMMENT ON COLUMN t002_users.t002_birthday IS 'Дата рождения';

COMMENT ON TABLE t003_likes IS 'Лайки';

COMMENT ON COLUMN t003_likes.t003_id IS 'ИД';

COMMENT ON COLUMN t003_likes.t001_id IS 'ИД фильма';

COMMENT ON COLUMN t003_likes.t002_id IS 'ИД пользователя';

COMMENT ON TABLE t004_friends IS 'Друзья';

COMMENT ON COLUMN t004_friends.t004_id IS 'ИД';

COMMENT ON COLUMN t004_friends.t002_target_id IS 'ИД инициатора';

COMMENT ON COLUMN t004_friends.t002_friend_id IS 'ИД друга';

COMMENT ON COLUMN t004_friends.t004_confirmed IS 'Подтверждение';

COMMENT ON TABLE t005_genres IS 'Жанры';

COMMENT ON COLUMN t005_genres.t005_id IS 'ИД';

COMMENT ON COLUMN t005_genres.t005_name IS 'Название';

COMMENT ON TABLE t006_ratings IS 'Рейтинги';

COMMENT ON COLUMN t006_ratings.t006_id IS 'ИД';

COMMENT ON COLUMN t006_ratings.t006_code IS 'Код';

COMMENT ON COLUMN t006_ratings.t006_description IS 'Описание';

COMMENT ON TABLE t007_links_t001_t005 IS 'Соответствие фильмов и жанров';

COMMENT ON COLUMN t007_links_t001_t005.t007_id IS 'ИД';

COMMENT ON COLUMN t007_links_t001_t005.t001_id IS 'ИД фильма';

COMMENT ON COLUMN t007_links_t001_t005.t005_id IS 'ИД жанра';

COMMENT ON TABLE t009_reviews IS 'Отзывы';

COMMENT ON COLUMN t009_reviews.t009_id IS 'ИД';

COMMENT ON COLUMN t009_reviews.t009_content IS 'Содержание';

COMMENT ON COLUMN t009_reviews.t009_is_positive IS 'Тип отзыва';

COMMENT ON COLUMN t009_reviews.t002_id IS 'ИД пользователя';

COMMENT ON COLUMN t009_reviews.t001_id IS 'ИД фильма';

COMMENT ON TABLE t010_review_feedbacks IS 'Оценки на отзыв';

COMMENT ON COLUMN t010_review_feedbacks.t010_id IS 'ИД';

COMMENT ON COLUMN t010_review_feedbacks.t009_id IS 'ИД отзыва';

COMMENT ON COLUMN t010_review_feedbacks.t002_id IS 'ИД пользователя';

COMMENT ON COLUMN t010_review_feedbacks.t010_value IS 'Оценка';

ALTER TABLE t001_films ADD CONSTRAINT fk_t001_t006 FOREIGN KEY (t006_id) REFERENCES t006_ratings (t006_id) ON DELETE SET NULL;

ALTER TABLE t003_likes ADD CONSTRAINT fk_t003_t001 FOREIGN KEY (t001_id) REFERENCES t001_films (t001_id) ON DELETE CASCADE;

ALTER TABLE t003_likes ADD CONSTRAINT fk_t003_t002 FOREIGN KEY (t002_id) REFERENCES t002_users (t002_id) ON DELETE CASCADE;

ALTER TABLE t004_friends ADD CONSTRAINT fk_t004_t002_target FOREIGN KEY (t002_target_id) REFERENCES t002_users (t002_id) ON DELETE CASCADE;

ALTER TABLE t004_friends ADD CONSTRAINT fk_t004_t002_friend FOREIGN KEY (t002_friend_id) REFERENCES t002_users (t002_id) ON DELETE CASCADE;

ALTER TABLE t007_links_t001_t005 ADD CONSTRAINT fk_t008_t001 FOREIGN KEY (t001_id) REFERENCES t001_films (t001_id) ON DELETE CASCADE;

ALTER TABLE t007_links_t001_t005 ADD CONSTRAINT fk_t008_t005 FOREIGN KEY (t005_id) REFERENCES t005_genres (t005_id);

ALTER TABLE t009_reviews ADD CONSTRAINT fk_t009_t002 FOREIGN KEY (t002_id) REFERENCES t002_users (t002_id) ON DELETE CASCADE;

ALTER TABLE t009_reviews ADD CONSTRAINT fk_t009_t001 FOREIGN KEY (t001_id) REFERENCES t001_films (t001_id) ON DELETE CASCADE;

ALTER TABLE t010_review_feedbacks ADD CONSTRAINT fk_t010_t009 FOREIGN KEY (t009_id) REFERENCES t009_reviews (t009_id) ON DELETE CASCADE;

ALTER TABLE t010_review_feedbacks ADD CONSTRAINT fk_t010_t002 FOREIGN KEY (t002_id) REFERENCES t002_users (t002_id) ON DELETE CASCADE;