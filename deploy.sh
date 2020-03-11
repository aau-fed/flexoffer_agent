#!bin/bash
echo "Building code and performing unit and integration tests..."
mvn clean install -DskipTests
echo "Creating docker images..."
docker-compose build
echo "Pushing docker images to private docker registry..."
docker-compose push
# Note: you must be logged in to the private docker registry to run these commands
echo "Server: pulling docker images from private docker registry and re-creating containers..."
ssh goflex-atp "cd /srv/GOFLEX/docker/foa && docker-compose pull && docker-compose up -d"
