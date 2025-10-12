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

INSERT INTO genres (name) VALUES
    -- Core Genres
    ('Action'),
    ('Adventure'),
    ('Comedy'),
    ('Drama'),
    ('Horror'),
    ('Thriller'),
    ('Romance'),
    ('Science Fiction'),
    ('Fantasy'),
    ('Mystery'),
    ('Crime'),

    -- Subgenres & Crossovers
    ('Action-Adventure'),
    ('Romantic Comedy'),
    ('Dark Comedy'),
    ('Psychological Thriller'),
    ('Supernatural Thriller'),
    ('Crime Thriller'),
    ('Political Thriller'),
    ('Techno Thriller'),
    ('Epic'),
    ('Noir'),
    ('Neo-Noir'),
    ('Melodrama'),

    -- Family & Animation
    ('Animation'),
    ('Family'),
    ('Children'),
    ('Kids & Family'),
    ('Stop Motion'),
    ('Anime'),

    -- Historical & Realism
    ('Biography'),
    ('History'),
    ('War'),
    ('Historical Drama'),
    ('Period Piece'),
    ('Docudrama'),
    ('Documentary'),
    ('Mockumentary'),
    ('Based on True Story'),

    -- Music & Performance
    ('Music'),
    ('Musical'),
    ('Concert Film'),
    ('Dance'),

    -- Fantasy & Sci-Fi Extensions
    ('Space Opera'),
    ('Cyberpunk'),
    ('Steampunk'),
    ('Post-Apocalyptic'),
    ('Dystopian'),
    ('Time Travel'),
    ('Alien'),
    ('Superhero'),
    ('Paranormal'),
    ('Urban Fantasy'),
    ('Mythology'),

    -- Horror Subgenres
    ('Supernatural Horror'),
    ('Psychological Horror'),
    ('Slasher'),
    ('Zombie'),
    ('Vampire'),
    ('Monster'),
    ('Found Footage'),
    ('Survival Horror'),
    ('Gothic Horror'),

    -- Regional & Cultural
    ('Western'),
    ('Eastern'),
    ('Samurai'),
    ('Martial Arts'),
    ('Bollywood'),
    ('Chinese Cinema'),
    ('European Cinema'),

    -- Niche & Artistic
    ('Art House'),
    ('Experimental'),
    ('Avant-Garde'),
    ('Independent'),
    ('Surreal'),
    ('Short Film'),

    -- Social & Political
    ('Political'),
    ('Social Issues'),
    ('LGBTQ+'),
    ('Feminist'),
    ('Coming-of-Age'),
    ('Religious'),
    ('Satire'),

    -- Sports & Competition
    ('Sport'),
    ('Sports Drama'),
    ('Esports'),

    -- Miscellaneous
    ('Heist'),
    ('Spy'),
    ('Detective'),
    ('Courtroom'),
    ('Disaster'),
    ('Survival'),
    ('Mystery Crime'),
    ('Teen'),
    ('Road Movie'),
    ('Travel'),
    ('Erotic'),
    ('Tragedy'),
    ('Holiday'),
    ('Christmas'),
    ('Found Footage'),
    ('Psychodrama'),
    ('Adventure Comedy'),

    -- TV & Streaming Specific
    ('Reality'),
    ('Talk Show'),
    ('Game Show'),
    ('News'),
    ('Variety'),
    ('Mini-Series'),
    ('Sitcom'),
    ('Soap Opera')

ON CONFLICT (name) DO NOTHING;
