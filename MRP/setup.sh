echo "Media Ratings Platform - Setup"

#start database
docker compose down > /dev/null 2>&1
docker compose up -d

#wait for database
until docker exec mrp-postgres pg_isready -U mrp_user -d mrp_db > /dev/null 2>&1; do
    sleep 2
done

#copy SQL files into container
docker cp database/schema.sql mrp-postgres:/tmp/schema.sql
docker cp database/sample_data.sql mrp-postgres:/tmp/sample_data.sql

#execute SQL files from inside container
docker exec mrp-postgres psql -U mrp_user -d mrp_db -f /tmp/schema.sql
docker exec mrp-postgres psql -U mrp_user -d mrp_db -f /tmp/sample_data.sql

echo "Database setup complete!"