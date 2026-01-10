#!/usr/bin/env bash
set -euo pipefail

DOCKER_NETWORK="idear-net"
REDIS_CONTAINER_NAME="redis"
CONTAINER_NAME="idear-api"
IMAGE_NAME="${DOCKERHUB_USERNAME}/${DOCKER_IMAGE_NAME}:latest"

sudo docker network inspect "${DOCKER_NETWORK}" >/dev/null 2>&1 || sudo docker network create "${DOCKER_NETWORK}"

if ! sudo docker ps -a --format '{{.Names}}' | grep -qx "${REDIS_CONTAINER_NAME}"; then
  sudo docker run -d \
    --name "${REDIS_CONTAINER_NAME}" \
    --network "${DOCKER_NETWORK}" \
    --restart unless-stopped \
    redis:7-alpine
else
  sudo docker network connect "${DOCKER_NETWORK}" "${REDIS_CONTAINER_NAME}" >/dev/null 2>&1 || true

  if ! sudo docker ps --format '{{.Names}}' | grep -qx "${REDIS_CONTAINER_NAME}"; then
    sudo docker start "${REDIS_CONTAINER_NAME}"
  fi
fi

CONTAINER_ID=$(sudo docker ps -aqf "name=^/${CONTAINER_NAME}$")
if [ -n "${CONTAINER_ID}" ]; then
  sudo docker stop "${CONTAINER_ID}"
  sudo docker rm "${CONTAINER_ID}"
fi

sudo docker pull "${IMAGE_NAME}"

sudo docker run --name "${CONTAINER_NAME}" -d \
  --network "${DOCKER_NETWORK}" \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e IDEAR_CRAWLER_ENABLED=false \
  -e PROD_MYSQL_URL="${PROD_MYSQL_URL}" \
  -e PROD_MYSQL_USER="${PROD_MYSQL_USER}" \
  -e PROD_MYSQL_PASSWORD="${PROD_MYSQL_PASSWORD}" \
  -e REDIS_HOST="${REDIS_CONTAINER_NAME}" \
  -e REDIS_PORT=6379 \
  -e JWT_KEY="${JWT_KEY}" \
  -e FRONT_ORIGIN="${FRONT_ORIGIN}" \
  -e S3_ACCESS_KEY="${S3_ACCESS_KEY}" \
  -e S3_SECRET_KEY="${S3_SECRET_KEY}" \
  -e KAKAO_CLIENT_ID="${KAKAO_CLIENT_ID}" \
  -e KAKAO_CLIENT_SECRET="${KAKAO_CLIENT_SECRET}" \
  -e KAKAO_REDIRECT_URI="${KAKAO_REDIRECT_URI}" \
  -e NAVER_CLIENT_ID="${NAVER_CLIENT_ID}" \
  -e NAVER_CLIENT_SECRET="${NAVER_CLIENT_SECRET}" \
  -e NAVER_REDIRECT_URI="${NAVER_REDIRECT_URI}" \
  -e GOOGLE_CLIENT_ID="${GOOGLE_CLIENT_ID}" \
  -e GOOGLE_CLIENT_SECRET="${GOOGLE_CLIENT_SECRET}" \
  -e GOOGLE_REDIRECT_URI="${GOOGLE_REDIRECT_URI}" \
  -e BLOCKCHAIN_GATEWAY_URL="http://idear-chain:3000" \
  -e BLOCKCHAIN_CONTRACT_ADDRESS="${BLOCKCHAIN_CONTRACT_ADDRESS}" \
  -e SERVER_PRIVATE_KEY="${SERVER_PRIVATE_KEY}" \
  -e SPRING_MAIL_USERNAME="${SPRING_MAIL_USERNAME}" \
  -e SPRING_MAIL_PASSWORD="${SPRING_MAIL_PASSWORD}" \
  "${IMAGE_NAME}"

sudo docker system prune -a -f
