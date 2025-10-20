# Media Ratings Platform

## GITHUB
https://github.com/makathii/SWEN_Semester

## QUICK START
1. Run: `bash setup.sh`
2. Start Server class in IDE: `at.technikum_wien.presentation.server.Server`
3. Test API: [http://localhost:8080](http://localhost:8080)

---

## WHAT'S INCLUDED
- **Database:** PostgreSQL in Docker with sample data  
- **Authentication:** User registration & login  
- **Media Management:** Full CRUD operations  
- **Search & Filter:** Advanced media search  
- **Sample Data:** 2 users, 3 media items, ratings, favorites  

---

## IMPLEMENTED ENDPOINTS

### AUTHENTICATION
| Method | Endpoint | Description |
|---------|-----------|-------------|
| POST | `/api/users/register` | Register new user |
| POST | `/api/users/login` | Login user |

### MEDIA MANAGEMENT
| Method | Endpoint | Description |
|---------|-----------|-------------|
| GET | `/api/media` | Get all media (with search/filter) |
| POST | `/api/media` | Create media (requires auth) |
| GET | `/api/media/{id}` | Get media by ID |
| PUT | `/api/media/{id}` | Update media (creator only) |
| DELETE | `/api/media/{id}` | Delete media (creator only) |

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

**Presentation Layer**
- `UserHandler`, `MediaHandler`, `RatingHandler`, `Server`

**Application Layer**
- `UserService`, `MediaService`, `RatingService`

**Domain Layer**
- `User`, `Media`, `Rating` entities

**Infrastructure Layer**
- Database repositories, utilities, security

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
Use provided Postman collection from moodle: **MRP_Postman_Collection.json** 

---

## NOTES FOR EXAMINER
- Database setup is fully automated via Docker  
- Core media functionality is completely implemented  
- Some endpoints return 501 (not implemented) as per requirements  
- Lombok is used for entity classes (may show warnings but works)  
- Clean layered architecture following SOLID principles  

---

## TROUBLESHOOTING
- Ensure **Docker Desktop** is running before setup  
- Ports **5432 (DB)** and **8080 (API)** should be available  
- Run `bash cleanup.sh` to reset everything