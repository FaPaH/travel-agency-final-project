cd "$(dirname "$0")/.."

docker network inspect travel-network >/dev/null 2>&1 || \
    docker network create travel-network

echo "Starting app..."
docker-compose -f docker-compose.yml up --build -d

echo "Everything is up!"