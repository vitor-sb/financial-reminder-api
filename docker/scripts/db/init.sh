#!/bin/bash

set -e

export PGUSER="$POSTGRES_USER"
export PGDATABASE="$POSTGRES_DB"

echo "Creating application user..."
psql -v ON_ERROR_STOP=1 \
     -v APP_DB_USER="'${API_DB_USER}'" \
     -v APP_DB_PASSWORD="'${API_DB_PASSWORD}'" \
     -f /docker-entrypoint-initdb.d/create-app-user.sql

echo "Creating owner user..."
psql -v ON_ERROR_STOP=1 \
     -v APPLICATION_OWNER_EMAIL="'${APPLICATION_OWNER_EMAIL}'" \
     -v APPLICATION_OWNER_NAME="'${APPLICATION_OWNER_NAME}'" \
     -v APPLICATION_OWNER_PASSWORD_HASH="'${APPLICATION_OWNER_PASSWORD_HASH}'" \
     -f /docker-entrypoint-initdb.d/create-owner-user.sql

echo "Database initialization scripts executed successfully."