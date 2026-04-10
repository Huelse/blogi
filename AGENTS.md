# Repository Guidelines

## Project Structure & Module Organization
`client/` contains the Nuxt 4 frontend. Page routes live in `client/pages/`, shared styles in `client/assets/css/`, and runtime config in `client/nuxt.config.ts`. `server/` contains the Spring Boot backend: application entrypoint in `server/src/main/java/com/blogi/`, feature code under `server/src/main/java/com/blogi/modules/`, shared response/config classes under `server/src/main/java/com/blogi/common/` and `server/src/main/java/com/blogi/config/`, and tests in `server/src/test/java/`. Infrastructure files stay at the repo root: `docker-compose.yml`, `.env.example`, and the workspace `package.json`.

## Build, Test, and Development Commands
Use `pnpm install` at the repo root to install frontend dependencies. `pnpm dev` starts the Nuxt app, `pnpm build` creates the frontend production build, and `pnpm lint` runs ESLint in `client/`. Start local services with `docker compose up -d` for PostgreSQL and Redis. For the backend, run `cd server && mvn spring-boot:run` for development and `cd server && mvn test` to execute JUnit tests.

## Coding Style & Naming Conventions
Follow the existing language defaults instead of reformatting files aggressively. Vue and Nuxt files use TypeScript, `<script setup>`, and 2-space indentation; keep route files lowercase (`pages/index.vue`) and use PascalCase for exported Vue components when added. Java uses 4-space indentation, `com.blogi` package names, PascalCase classes, and camelCase methods. Run `pnpm lint` before opening a PR; frontend linting is configured through `client/eslint.config.mjs` with `@nuxt/eslint`.

## Testing Guidelines
Backend tests use JUnit 5 via `spring-boot-starter-test`. Place new tests under `server/src/test/java/` mirroring the production package path, and prefer class names ending in `Tests` or `Test`. There is currently no committed frontend test runner, so new UI tests should be introduced deliberately and documented in the PR. At minimum, verify `/api/health` and the Nuxt homepage after backend or config changes.

## Commit & Pull Request Guidelines
Recent history uses short imperative subjects such as `Init project` and `Update README.md`. Keep commit titles brief, capitalized, and action-oriented. PRs should include a clear summary, note any env or schema changes, link related issues, and attach screenshots for visible frontend changes. If a change touches both `client/` and `server/`, describe the API contract or config updates explicitly.

## Security & Configuration Tips
Copy `.env.example` to `.env` before local setup. Do not commit real secrets; replace `JWT_SECRET` with a strong local value and keep `NUXT_PUBLIC_API_BASE` aligned with the backend port (`http://localhost:8080/api` by default).
