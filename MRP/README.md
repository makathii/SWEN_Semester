# Media Ratings Platform

## GITHUB

https://github.com/makathii/SWEN_Semester

## QUICK START

1. Run: `bash setup.sh`
2. Start Server class in IDE: `at.technikum_wien.presentation.server.Server`
3. Test API: [http://localhost:8080](http://localhost:8080)

---

## WHAT'S INCLUDED

- **Database:** PostgreSQL in Docker with schema & sample data
- **Authentication:** User registration, login & persistent token-based auth
- **Media Management:** Full CRUD operations with ownership validation
- **Ratings System:** Ratings, confirmation workflow & rating likes
- **Favorites:** User-specific media fevorites
- **Leaderboards & Analytics:** Activity, top-rated media, trending genres
- **Search & Filter:** Advanced media search & filtering
- **Sample Data:** 2 users, 3 media items, ratings, favorites

---

## IMPLEMENTED ENDPOINTS

### LEADERBOARD

| Method | Endpoint                           | Description       |
|--------|------------------------------------|-------------------|
| GET    | `/api/leaderboard/top-users`       | Top users         |
| GET    | `/api/leaderboard/top-rated`       | Top rated Media   |
| GET    | `/api/leaderboard/most-liked`      | Most liked Rating |
| GET    | `/api/leaderboard/trending-genres` | Trending Genres   |

### AUTHENTICATION

| Method | Endpoint              | Description       |
|--------|-----------------------|-------------------|
| POST   | `/api/users/register` | Register new user |
| POST   | `/api/users/login`    | Login user        |

### MEDIA MANAGEMENT

| Method | Endpoint          | Description                        |
|--------|-------------------|------------------------------------|
| GET    | `/api/media`      | Get all media (with search/filter) |
| POST   | `/api/media`      | Create media (requires auth)       |
| GET    | `/api/media/{id}` | Get media by ID                    |
| PUT    | `/api/media/{id}` | Update media (creator only)        |
| DELETE | `/api/media/{id}` | Delete media (creator only)        |

### RATINGS

| Method | Endpoint                    | Description                |
|--------|-----------------------------|----------------------------|
| POST   | `/api/media/{id}/rate`      | Rate media                 |
| PUT    | `/api/ratings/{id}`         | Update rating (owner only) |
| DELETE | `/api/ratings/{id}`         | Delete rating (owner only) |
| GET    | `/api/ratings/{id}`         | Get rating                 |
| POST   | `/api/ratings/{id}/confirm` | Confirm rating comment     |
| POST   | `/api/ratings/{id}/like`    | Like rating                |

### FAVORITES

| Method | Endpoint                    | Description                 |
|--------|-----------------------------|-----------------------------|
| POST   | `/api/media/{id}/favorite`  | Add media to favorites      |
| DELETE | `/api/media/{id}/favorite`  | Remove media from favorites |
| GET    | `/api/media/{id}/favorites` | Get user's favorite media   |

### USER PROFILE & DATA
| Method | Endpoint                  | Description                   |
|--------|---------------------------|-------------------------------|
| GET    | `/api/users/{id}/profile` | Get user profile & statistics |
| PUT    | `/api/users/{id}/profile` | Update user profile           |
| GET    | `/api/users/{id}/ratings` | Get all ratings by user       | 

### RECOMMENDATIONS
| Method | Endpoint                  | Description                   |
|--------|---------------------------|-------------------------------|
| GET    | `/api/users/{id}/recommendations` | Genre-based recommendations |
| GET    | `/api/users/{id}/recommendations?type=content` | Content-based recommendations           |
| GET    | `/api/users/{id}/recommendations?limit=5` | Limit number of recommendations       | 



### SEARCH & FILTER EXAMPLE

```
GET /api/media?title=inception&genre=sci-fi&mediaType=movie&releaseYear=2010
```

---

## SAMPLE DATA

**Users**

- `Maria` (token: `Maria-mrpToken`)
- `Parker` (token: `Parker-mrpToken`)

**Media**

- *Ready Player One* (movie) - Sci-Fi / Action / Thriller
- *GRIS* (game) - Fantasy / Adventure
- *Disjointed* (series) - Sci-Fi / Horror / Drama

---

## ARCHITECTURE

**Database/Repositories**

- `FavoriteRepository`, `GenreRepository`, `LeaderboardRepository`, `MediaRepository`, `RatingRepository`, `TokenRepository`, `UserRepository`

**Handlers**

- `FavoriteHandler`, `UserHandler`, `MediaHandler`, `RatingHandler`

**Services**

- `FavoriteService`, `UserService`, `MediaService`, `RatingService`, `LeaderboardService`, `RecommendationService`

**Models**

- all entities, enums, interfaces

**Tests**
- all Services tested (113 tests total)

---

## DATABASE SCHEMA

Tables:

- `users`, `media`, `ratings`, `genres`, `media_genres`
- `favorites`, `rating_likes`, `tokens`

---

## SETUP FILES

- `setup.sh` — Database setup (Docker + PostgreSQL)
- `cleanup.sh` — Cleanup script
- `docker-compose.yml` — Database configuration
- `database/schema.sql` — Database schema with 100+ genres
- `database/sample_data.sql` — Sample data

---

## TESTING

Use updated Postman collection: **MRP_Postman_Collection.json**

---

## NOTES FOR EXAMINER

- Database setup is fully automated via Docker
- Core media functionality is completely implemented
- Lombok is used for entity classes (may show warnings but works)
- Clean layered architecture following SOLID principles (at least I tried)

---

## TROUBLESHOOTING

- Ensure **Docker Desktop** is running before setup
- Ports **5432 (DB)** and **8080 (API)** should be available
- Run `bash cleanup.sh` to reset everything