This folder will contain generated Cypress specs. To generate tests, run from repository root:

```bash
# install inside cypress folder (optional)
cd cypress
npm install

# generate N tests (e.g. 2000)
node ../scripts/generate_cypress_tests.js 2000

# run them (headless)
npx cypress run --config-file cypress.config.js
```

The generator creates simple, non-destructive tests that only visit pages and make assertions about HTTP status and presence of basic DOM elements.
