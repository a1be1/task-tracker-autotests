#!/bin/bash

DB_CONTAINER_NAME="task-tracker-db"
APP_CONTAINER_NAME="task-tracker-app"

CONTAINER_NAMES=($DB_CONTAINER_NAME $APP_CONTAINER_NAME)

echo "---"
echo "Starting automated conflict check and deployment..."
echo "---"

for NAME in "${CONTAINER_NAMES[@]}"; do
    echo "Checking for conflicting container: ${NAME}"

    if docker ps -a --format '{{.Names}}' | grep -q "^${NAME}$"; then
        echo "🚨 Conflict found! Stopping and removing container: ${NAME}"

        docker stop "${NAME}" > /dev/null 2>&1

        docker rm "${NAME}"
        echo "✅ Removed ${NAME}"
    else
        echo "Container ${NAME} not found. Proceeding."
    fi
done

echo "---"
echo "Starting services with docker-compose..."
docker-compose up --force-recreate -d

echo "---"
echo "Deployment complete! Status of new containers:"
docker ps --filter name=task-tracker