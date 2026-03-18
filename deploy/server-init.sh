#!/bin/bash
# Run once on a fresh Ubuntu 22.04 server as root or sudo user

set -e

echo "=== Installing Docker ==="
apt-get update -q
apt-get install -y ca-certificates curl gnupg
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
  > /etc/apt/sources.list.d/docker.list
apt-get update -q
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

echo "=== Creating data directories ==="
mkdir -p /opt/vibeproto/projects
mkdir -p /opt/vibeproto/storage
mkdir -p /opt/vibeproto/deploy

echo "=== Creating .env from example ==="
if [ ! -f /opt/vibeproto/deploy/.env ]; then
  cp /opt/vibeproto/deploy/.env.example /opt/vibeproto/deploy/.env
  echo ""
  echo "⚠️  Please edit /opt/vibeproto/deploy/.env before starting services!"
  echo "   Required: DB_PASS, JWT_SECRET, BASE_PREVIEW_URL"
fi

echo "=== Done. Next steps ==="
echo "1. Edit /opt/vibeproto/deploy/.env"
echo "2. cd /opt/vibeproto/deploy && docker compose up -d"
