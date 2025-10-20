INSERT INTO users (username, password_hash) VALUES
                                                ('Maria', '$2a$10$rH3.6a8bLcP9qF2wY8zZ.eQ1T7kR5mN9pB2vC8xW3zD4yV6bA7nM.KdW9W1qQ1qQ1qQ1qQ1qQ1qQ1qQ1qQ'),
                                                ('Parker', '$2a$10$N9qo8uLOickgx2ZMRZoMye.KdW9W1qQ1qQ1qQ1qQ1qQ1qQ1qQ1qQ');

INSERT INTO media (type, title, description, release_year, age_restriction, creator_id) VALUES
                                                                                            ('movie', 'Ready Player One', 'Ready Player One is a science fiction story set in a 2045 dystopia, where people escape their bleak reality in a virtual universe called the OASIS', 2018, 13, 1),
                                                                                            ('game', 'GRIS', 'Gris is a hopeful young girl lost in her own world, dealing with a painful experience in her life. Her journey through sorrow is manifested in her dress.', 2018, 7, 1),
                                                                                            ('series', 'Disjointed', 'After decades of advocating for legalized cannabis usage, Ruth Whitefeather Feldman (Kathy Bates) employs her newly graduated son and a team of young budtenders to help run her Los Angeles cannabis dispensary.[', 2017, 18, 2);
INSERT INTO media_genres (media_id, genre_id)
SELECT 1, genre_id FROM genres WHERE name IN ('Science Fiction','Drama','Adventure');

INSERT INTO media_genres (media_id, genre_id)
SELECT 2, genre_id FROM genres WHERE name IN ('Adventure','Jump n Run');

INSERT INTO media_genres (media_id, genre_id)
SELECT 3, genre_id FROM genres WHERE name IN ('Sitcom','Comedy');

INSERT INTO ratings (media_id, user_id, stars, comment, confirmed) VALUES
                                                                       (1, 1, 5, 'Mind-blowing concept and amazing visuals! Stephen Spielberg at his best.', true),
                                                                       (1, 2, 4, 'Great movie but a bit confusing at times. The concept is brilliant.', true),
                                                                       (2, 1, 5, 'One of the best games ever made! The storytelling and character development are phenomenal.', true),
                                                                       (2, 2, 5, 'Incredible world with so much to explore. The story is overwhelming!', true),
                                                                       (3, 1, 4, 'Love the vibe and funny elements. Debbie is my favorite!', true),
                                                                       (3, 2, 5, 'Perfect blend of comedy & reality. Can''t wait for the next season!', true);

INSERT INTO rating_likes (user_id, rating_id) VALUES
                                                  (2, 1), -- Parker likes Maria's RPO review
                                                  (1, 2), -- Maria likes Parker's RPO review
                                                  (2, 3), -- Parker likes Maria's GRIS review
                                                  (1, 4), -- Maria likes Parker's GRIS review
                                                  (1, 6); -- Maria likes Parker's Disjointed review

INSERT INTO favorites (user_id, media_id) VALUES
                                              (1, 1), -- Maria favorites RPO
                                              (1, 2), -- Maria favorites GRIS
                                              (2, 3), -- Parker favorites Disjointed
                                              (2, 1); -- Parker also favorites RPO

INSERT INTO tokens (token, user_id) VALUES
                                        ('Maria-mrpToken', 1),
                                        ('Parker-mrpToken', 2);

SELECT '=== SAMPLE DATA INSERTED SUCCESSFULLY ===' as status;

SELECT 'Users:' as info;
SELECT id, username, created_at FROM users;

SELECT 'Media:' as info;
SELECT m.id, m.type, m.title, m.release_year, m.age_restriction, u.username as creator
FROM media m
         JOIN users u ON m.creator_id = u.id;

SELECT 'Media with genres:' as info;
SELECT m.title, m.type, array_agg(g.name) as genres
FROM media m
         JOIN media_genres mg ON m.id = mg.media_id
         JOIN genres g ON mg.genre_id = g.genre_id
GROUP BY m.id, m.title, m.type
ORDER BY m.id;

SELECT 'Ratings:' as info;
SELECT m.title, u.username, r.stars, r.comment, r.confirmed
FROM ratings r
         JOIN media m ON r.media_id = m.id
         JOIN users u ON r.user_id = u.id
ORDER BY m.title, u.username;

SELECT 'Rating likes:' as info;
SELECT u1.username as liker, u2.username as reviewer, m.title
FROM rating_likes rl
         JOIN ratings r ON rl.rating_id = r.id
         JOIN users u1 ON rl.user_id = u1.id
         JOIN users u2 ON r.user_id = u2.id
         JOIN media m ON r.media_id = m.id;

SELECT 'Favorites:' as info;
SELECT u.username, m.title, m.type
FROM favorites f
         JOIN users u ON f.user_id = u.id
         JOIN media m ON f.media_id = m.id
ORDER BY u.username;

SELECT 'Tokens:' as info;
SELECT u.username, t.token
FROM tokens t
         JOIN users u ON t.user_id = u.id;