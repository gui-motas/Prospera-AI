.PHONY: help up down logs build rebuild ps test test-backend test-frontend dev-backend dev-frontend db-shell clean

help: ## Lista os comandos disponíveis
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-15s %s\n", $$1, $$2}'

up: ## Sobe banco, backend e frontend com Docker
	docker compose up -d --build
	@echo "Frontend: http://localhost:5173"
	@echo "Backend:  http://localhost:8090  (Swagger: http://localhost:8090/swagger-ui.html)"

down: ## Para os containers
	docker compose down

logs: ## Mostra os logs de todos os containers
	docker compose logs -f

ps: ## Status dos containers
	docker compose ps

build: ## Constrói as imagens
	docker compose build

rebuild: ## Reconstrói tudo do zero (apaga o volume do banco)
	docker compose down -v
	docker compose build --no-cache
	docker compose up -d

dev-backend: ## Roda o backend localmente (requer PostgreSQL em localhost:5432)
	./mvnw spring-boot:run

dev-frontend: ## Roda o frontend localmente (Vite em http://localhost:5173)
	cd frontend && npm install && npm run dev

test: test-backend test-frontend ## Roda todos os testes

test-backend: ## Testes do backend (JUnit)
	./mvnw test

test-frontend: ## Testes do frontend (Vitest)
	cd frontend && npm test

db-shell: ## Abre o psql no container do banco
	docker exec -it gfp-postgres psql -U postgres -d gestao_financeira

clean: ## Remove containers e volumes
	docker compose down -v
