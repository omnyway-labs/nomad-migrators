DB               ?= nomad-test


init-postgres-db:
	@dropdb $(PG_OPTS) --if-exists $(DB)
	@createdb $(PG_OPTS) $(DB)

setup-postgres:
	@echo "Setting up PostgreSQL Database"
	$(MAKE) init-postgres-db

setup-postgres-docker:
	PGPASSWORD=postgres DB_HOST=localhost DB_PORT=5432 DB_USER=postgres \
		            DB_PASS=postgres \
			    PG_OPTS="-h localhost -U postgres" \
			    $(MAKE) setup-postgres

install-docker:
	@docker rm postgres
	@docker run --name postgres -p 5432:5432 -e POSTGRES_PASSWORD="postgres" -d postgres
