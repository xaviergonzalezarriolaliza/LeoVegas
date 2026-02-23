# LeoVegas — Local run & CI notes

This repository contains a small Spark-based mock API used by three test suites:

- Java RestAssured/JUnit tests (`src/test/java/com/leovegas/apitest/*`)
- Cypress API/UI tests (`cypress/e2e/*.js` using `cypress-plugin-api`)
- Playwright Java tests (`PlaywrightMockApiTest`) — executed as a JUnit class

CI is defined at `.github/workflows/ci.yml` and contains three independent jobs: `restassured`, `cypress`, and `playwright`.

## Run locally

1. Build the project and copy runtime dependencies (recommended for CI parity):

```bash
mvn -B package
mvn dependency:copy-dependencies -DincludeScope=runtime
```

2. Start the mock server (CI-style, classpath from target):

```bash
nohup java -cp "target/classes:target/dependency/*" com.leovegas.mockapi.MockApiServer > nohup.out 2>&1 &
echo $! > server.pid
```

Allow a few seconds for the server to bind; verify with:

```bash
curl --fail http://localhost:4567/hello
```

Alternative (developer convenience):

```bash
mvn exec:java -Dexec.mainClass="com.leovegas.mockapi.MockApiServer" &
```

3. Run Java RestAssured tests:

```bash
mvn -Dtest=com.leovegas.apitest.MockApiJunitRestAssuredTest test
```

4. Run Cypress tests (requires Node):

```bash
# if package.json exists
npm ci
# run cypress headless
npx cypress run --headless --spec "cypress/e2e/**/*.js"
```

5. Run Playwright Java tests (browsers required):

```bash
# install Playwright browsers
npx playwright install --with-deps
# run the Playwright JUnit test class
mvn -Dtest=com.leovegas.apitest.PlaywrightMockApiTest test
```

## Notes about recent changes

- Tests now derive representative payloads from the mock server's `/manyFieldsPayload` endpoint instead of using hard-coded payloads. This reduces brittleness and ensures the tests better mirror real data.
- SLF4J: a test-scoped `slf4j-simple` binding was added so tests emit visible logs (see `pom.xml`). If you prefer a different logging backend, update the test scope dependency.
- Cypress: tests use `cypress-plugin-api` and `cy.api()` to call the mock server. Ensure `cypress-plugin-api` is installed in `node_modules` or `package.json`.
- CI pin: GitHub Actions use Node `18.18.0` to avoid a known `tsx` loader/runtime issue on newer Node releases. The workflow installs Playwright browsers on Linux runners.

## CI (quick troubleshooting)

- Workflow file: `.github/workflows/ci.yml` (jobs: `restassured`, `cypress`, `playwright`).
- The CI jobs start the mock server with the same `java -cp "target/classes:target/dependency/*"` command used above — this is the recommended approach for parity with local runs.
- If a job times out waiting for the mock server, inspect `nohup.out` (the workflow prints the file tail on failure) and upload it as an artifact.
- For Cypress runtime errors related to `tsx` or `--loader`, use Node `18.18.0` (that's the runner version pinned in the workflow).

## Helpful commands

Start server (background):

```bash
nohup java -cp "target/classes:target/dependency/*" com.leovegas.mockapi.MockApiServer > nohup.out 2>&1 &
```

Tail logs:

```bash
tail -n 200 nohup.out
```

Stop server (local):

```bash
if [ -f server.pid ]; then kill $(cat server.pid); else pkill -f 'com.leovegas.mockapi.MockApiServer'; fi
```ci: trigger Mon, Feb 23, 2026  1:15:16 PM
