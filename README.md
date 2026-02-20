# LeoVegas — Local run & CI notes

## Run locally

- Start the mock server (background):

```bash
mvn exec:java -Dexec.mainClass="com.leovegas.mockapi.MockApiServer" &
```

- Run tests:

```bash
mvn test
```

Notes:
- Allow a few seconds after starting the mock server for it to bind before running tests.
- If tests require a specific host/port, ensure the mock server uses that same host/port (usually `localhost`).

## GitHub Actions: common issues and fixes

If your workflow file is incorrectly named or placed, GitHub Actions will not run. Follow these points to ensure workflows run successfully:

- File location and name: place the workflow at `.github/workflows/maven.yml`. If your file is named `mavan.yml` or sits in the repo root, rename/move it to `.github/workflows/maven.yml`.
- Workflow trigger: ensure the workflow has `on: [push, pull_request]` (or equivalent) so pushes and PRs run the job.
- Java setup: use `actions/setup-java` with the correct `distribution` and `java-version` matching your `pom.xml`.
- Start mock server before tests: if tests require the local mock server, add a step that starts it in background and waits briefly before running `mvn test`. Example:

```yaml
- name: Start Mock API server
  run: |
    nohup mvn exec:java -Dexec.mainClass="com.leovegas.mockapi.MockApiServer" > server.log 2>&1 &
    sleep 3
```

Alternatively use `&` and `sleep` if `nohup` is not available:

```yaml
- name: Start Mock API server
  run: mvn exec:java -Dexec.mainClass="com.leovegas.mockapi.MockApiServer" &
- run: sleep 3
```

- Use dependency caching: cache Maven repository to speed up builds with `actions/cache`.
- Ensure build uses non-interactive flags where needed, e.g. `mvn -B test`.
- Port and network: ensure the mock server binds to `0.0.0.0` or `localhost` and does not require privileged ports.
- Failure diagnostics: capture `server.log` (created above) as an artifact or print last lines on failure to help debugging.

## Minimal example workflow

Create `.github/workflows/maven.yml` with contents along these lines:

```yaml
name: CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v4

    - name: Set up JDK
      uses: actions/setup-java@v4
      with:
        distribution: temurin
        java-version: '17'

    - name: Cache Maven packages
      uses: actions/cache@v4
      with:
        path: ~/.m2/repository
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: |
          ${{ runner.os }}-m2-

    - name: Build (compile only)
      run: mvn -B -DskipTests package

    - name: Start Mock API server
      run: |
        nohup mvn exec:java -Dexec.mainClass="com.leovegas.mockapi.MockApiServer" > server.log 2>&1 &
        sleep 3

    - name: Run tests
      run: mvn -B test

    - name: Upload server log on failure
      if: failure()
      uses: actions/upload-artifact@v4
      with:
        name: server-log
        path: server.log
```

Adjust `java-version` to match your `pom.xml` and ensure the mock server's port is available.

---

If you want, I can: move/rename your existing workflow to `.github/workflows/maven.yml`, or create the workflow file for you. Which would you prefer?
