// Export plain Cypress configuration object to avoid requiring the
// top-level 'cypress' package when the Cypress app loads this file.
module.exports = {
  e2e: {
    // Tests generated will use full URLs; baseUrl is optional
    setupNodeEvents() {},
    specPattern: 'cypress/e2e/**/*.spec.js',
    // Load the support file so cypress-plugin-api and global helpers are available
    supportFile: 'cypress/support/e2e.js',
    defaultCommandTimeout: 8000,
  },
}
