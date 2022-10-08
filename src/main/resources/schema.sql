DROP TABLE IF EXISTS users, films, friends, likes, film_genres, MPA,
    genres, directors, film_directors cascade;

CREATE TABLE IF NOT EXISTS USERS (
                                     user_id INT PRIMARY KEY AUTO_INCREMENT,
                                     email VARCHAR NOT NULL,
                                     login VARCHAR NOT NULL,
                                     user_name VARCHAR NOT NULL,
                                     birthday DATE
);

CREATE TABLE IF NOT EXISTS MPA (
                                   mpa_id INT PRIMARY KEY,
                                   mpa_name VARCHAR
);

CREATE TABLE IF NOT EXISTS FILMS (
                                     film_id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                     film_name VARCHAR NOT NULL,
                                     description VARCHAR,
                                     release_date DATE NOT NULL,
                                     duration INTEGER NOT NULL,
                                     mpa_id INTEGER REFERENCES MPA(mpa_id),
                                     rate INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS GENRES (
                                      genre_id INT PRIMARY KEY AUTO_INCREMENT,
                                      genre_name VARCHAR
);

CREATE TABLE IF NOT EXISTS FILM_GENRES (
                                           film_id INT REFERENCES FILMS(film_id) ON DELETE CASCADE,
                                           genre_id INT REFERENCES GENRES(genre_id),
                                           CONSTRAINT fg_pk PRIMARY KEY(film_id, genre_id)
);
CREATE TABLE IF NOT EXISTS LIKES (
                                     user_id INT REFERENCES USERS(user_id) ON DELETE CASCADE,
                                     film_id INT REFERENCES FILMS(film_id) ON DELETE CASCADE,
                                     CONSTRAINT likes_pk PRIMARY KEY (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS FRIENDS (
                                       user_id INT REFERENCES USERS(user_id) ON DELETE CASCADE,
                                       friend_id INT REFERENCES USERS(user_id) ON DELETE CASCADE,
                                       status BOOLEAN,
                                       CONSTRAINT friends_pk PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS DIRECTORS (
                                         id INT PRIMARY KEY AUTO_INCREMENT,
                                         director_name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS FILM_DIRECTORS (
                                              film_id INTEGER REFERENCES FILMS(film_id) ON DELETE CASCADE,
                                              id INTEGER REFERENCES DIRECTORS(id) ON DELETE CASCADE,
                                              CONSTRAINT film_director_pk PRIMARY KEY (film_id, id)
);
//CREATE TABLE IF NOT EXISTS REVIEWS (
//                                       review_id int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
//                                       content VARCHAR,
//                                       is_positive BOOLEAN NOT NULL,
//                                       user_id INT REFERENCES USERS(user_id) ON DELETE CASCADE,
//                                       film_id INT REFERENCES FILMS(film_id) ON DELETE CASCADE,
//                                       useful INT
//);