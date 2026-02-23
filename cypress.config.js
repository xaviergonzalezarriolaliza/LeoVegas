// Export plain Cypress configuration object to avoid requiring the
// top-level 'cypress' package when the Cypress app loads this file.
module.exports = {
  e2e: {
    // Tests generated will use full URLs; baseUrl is optional
    setupNodeEvents(on, config) {
      // In-memory checks recorder for feature/checks coverage
      const checks = {}

      on('task', {
        recordCheck({ id, ok }) {
          if (!id) return null
          if (!checks[id]) checks[id] = { pass: 0, total: 0 }
          checks[id].total += 1
          if (ok) checks[id].pass += 1
          return null
        },
        writeChecks({ path = 'report/checks.json' } = {}) {
          const fs = require('fs')
          const summary = {
            totalChecks: Object.keys(checks).length,
            passedChecks: Object.values(checks).filter(c => c.pass > 0).length,
            checks,
          }
          fs.mkdirSync(require('path').dirname(path), { recursive: true })
          fs.writeFileSync(path, JSON.stringify(summary, null, 2))
          return summary
        },
        resetChecks() {
          Object.keys(checks).forEach(k => delete checks[k])
          return null
        },
        getChecks() {
          return checks
        }
      })

      return config
    },
    specPattern: 'cypress/e2e/**/*.spec.js',
    // Load the support file so cypress-plugin-api and global helpers are available
    supportFile: 'cypress/support/e2e.js',
    defaultCommandTimeout: 8000,
  },
}
