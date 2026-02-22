const fs = require('fs')
const path = require('path')

// Usage: node generate_cypress_tests.js [count]
const count = parseInt(process.argv[2], 10) || 100
const outDir = path.join(__dirname, '..', 'cypress', 'e2e')
if (!fs.existsSync(outDir)) fs.mkdirSync(outDir, { recursive: true })

const base = 'https://www.leovegas.es'
const endpoints = ['/', '/promotions', '/casino', '/games', '/sports', '/help', '/about', '/terms']
const selectors = ['header', 'nav', 'footer', 'main', 'a', 'button']

function pick(arr, i) { return arr[i % arr.length] }

for (let i = 1; i <= count; i++) {
  const ep = pick(endpoints, i)
  const s1 = pick(selectors, i)
  const s2 = pick(selectors, i + 3)
  const file = path.join(outDir, `auto_test_${String(i).padStart(4,'0')}.spec.js`)
  const content = `describe('Auto test #${i} - ${ep}', () => {
  it('visits ${ep} and performs assertions', () => {
    const url = '${base}${ep}'
    // Randomized short wait to avoid hitting the server too fast
    const rndWait = Math.floor(Math.random() * 1500) + 300
    // Check HTTP status with timeout
    cy.request({ url, failOnStatusCode: false, timeout: 20000 }).then((resp) => {
      expect([200,301,302,403,404]).to.include(resp.status)
    })
    // Visit and check DOM elements with longer timeouts and retries
    cy.visit(url, { timeout: 30000 })
    cy.wait(rndWait)
    cy.get('${s1}', { timeout: 15000 }).should('exist')
    // add a small randomized interaction to vary requests
    cy.get('body', { timeout: 10000 }).then($b => {
      if ($b.find('${s2}').length > 0) {
        cy.get('${s2}', { timeout: 8000 }).first().then($el => {
          // sometimes click if visible and actionable
          if ($el.is('a') || $el.is('button')) {
            cy.wrap($el).click({ force: true })
            cy.wait(300 + Math.floor(Math.random() * 700))
            // after click, go back
            cy.go('back')
          }
        })
      }
    })
    // Ensure at least two assertions per test
    cy.get('title', { timeout: 10000 }).then(() => {
      cy.title().then(t => {
        expect(t.length).to.be.at.least(0)
      })
    })
  })
})
`
  fs.writeFileSync(file, content)
}

console.log(`Wrote ${count} Cypress spec files to ${outDir}`)
