CREATE TABLE users(
                      id            SERIAL PRIMARY KEY,
                      username      VARCHAR(50) UNIQUE NOT NULL,
                      password_hash VARCHAR(255)       NOT NULL,
                      created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE genres(
                       genre_id SERIAL PRIMARY KEY,
                       name     VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE media(
                      id              SERIAL PRIMARY KEY,
                      type            VARCHAR(20)  NOT NULL CHECK (type IN ('movie', 'series', 'game')),
                      title           VARCHAR(255) NOT NULL,
                      description     TEXT,
                      release_year    INT,
                      age_restriction INT,
                      creator_id      INT REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE ratings(
                        id         SERIAL PRIMARY KEY,
                        media_id   INT REFERENCES media (id) ON DELETE CASCADE,
                        user_id    INT REFERENCES users (id) ON DELETE CASCADE,
                        stars      INT CHECK (stars BETWEEN 1 AND 5),
                        comment    TEXT,
                        confirmed  BOOLEAN   DEFAULT FALSE,
                        created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE rating_likes(
                             user_id   INT REFERENCES users (id) ON DELETE CASCADE,
                             rating_id INT REFERENCES ratings (id) ON DELETE CASCADE,
                             PRIMARY KEY (user_id, rating_id)
);

CREATE TABLE favorites(
                          user_id  INT REFERENCES users (id) ON DELETE CASCADE,
                          media_id INT REFERENCES media (id) ON DELETE CASCADE,
                          PRIMARY KEY (user_id, media_id)
);

CREATE TABLE media_genres(
                             media_id INT REFERENCES media (id) ON DELETE CASCADE,
                             genre_id INT REFERENCES genres (genre_id) ON DELETE CASCADE,
                             PRIMARY KEY (media_id, genre_id)
);

CREATE TABLE tokens (
                        id SERIAL PRIMARY KEY,
                        token VARCHAR(255) UNIQUE NOT NULL,
                        user_id INT REFERENCES users(id),
                        created_at TIMESTAMP DEFAULT NOW()
);