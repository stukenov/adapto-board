.PHONY: dev dev-server dev-db db-up db-down db-logs clean build help

# Default storage path for local development
STORAGE_PATH ?= $(CURDIR)/storage

help:
	@echo "Available targets:"
	@echo "  make dev        - Start database and server"
	@echo "  make dev-db     - Start only database"
	@echo "  make dev-server - Start only server (requires db running)"
	@echo "  make db-up      - Start database container"
	@echo "  make db-down    - Stop database container"
	@echo "  make db-logs    - Show database logs"
	@echo "  make build      - Build server application"
	@echo "  make clean      - Stop containers and clean build"

# Start everything for development
dev: db-up dev-server

# Start only the database
dev-db: db-up
	@echo "Database started. Connect at localhost:5432"
	@echo "  Database: playoutedge"
	@echo "  User: dev"
	@echo "  Password: dev"

# Start only the server (database must be running)
dev-server:
	@mkdir -p $(STORAGE_PATH)
	DATABASE_URL=jdbc:postgresql://localhost:5432/playoutedge \
	DATABASE_USER=dev \
	DATABASE_PASSWORD=dev \
	JWT_SECRET=dev-secret-change-in-production \
	STORAGE_MODE=LOCAL \
	STORAGE_LOCAL_PATH=$(STORAGE_PATH) \
	STORAGE_LOCAL_URL=http://localhost:8080/api/storage \
	./gradlew :apps:server:run

# Database management
db-up:
	docker-compose up -d postgres
	@echo "Waiting for database to be healthy..."
	@until docker-compose exec -T postgres pg_isready -U dev -d playoutedge > /dev/null 2>&1; do \
		sleep 1; \
	done
	@echo "Database is ready!"

db-down:
	docker-compose down

db-logs:
	docker-compose logs -f postgres

# Build
build:
	./gradlew :apps:server:build

# Clean everything
clean:
	docker-compose down -v
	./gradlew clean

# Reset database (drops and recreates)
db-reset:
	docker-compose down -v
	docker-compose up -d postgres
	@echo "Waiting for database to be healthy..."
	@until docker-compose exec -T postgres pg_isready -U dev -d playoutedge > /dev/null 2>&1; do \
		sleep 1; \
	done
	@echo "Database reset complete!"
