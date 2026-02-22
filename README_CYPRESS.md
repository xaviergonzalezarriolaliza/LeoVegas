**Cypress test generator (non-destructive)**

- To install Cypress locally (isolated in `cypress/`):

```bash
cd cypress
npm install
```

- To generate 2000 tests (or any N):

```bash
node ../scripts/generate_cypress_tests.js 2000
```

- To run headless:

```bash
npx cypress run --config-file cypress.config.js
```

Notes:
- Tests are lightweight and only visit pages and assert HTTP status and presence of basic DOM elements.
- Do not run large numbers of tests against production sites without permission; throttle or run against a test/staging environment if available.
