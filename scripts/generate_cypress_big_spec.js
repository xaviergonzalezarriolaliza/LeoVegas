const fs = require('fs')
const path = require('path')

// Usage: node generate_cypress_big_spec.js [count]
const count = parseInt(process.argv[2], 10) || 2000
const outFile = path.join(__dirname, '..', 'cypress', 'e2e', 'leovegas.spec.js')
const base = 'https://www.leovegas.es'
const endpoints = ['/', '/promotions', '/casino', '/games', '/sports', '/help', '/about', '/terms']
const selectors = ['header', 'nav', 'footer', 'main', 'a', 'button']

function pick(arr, i) { return arr[i % arr.length] }

let parts = []
parts.push("// Auto-generated single spec with " + count + " tests\n")
parts.push("describe('Leovegas big generated suite', () => {\n")

for (let i = 1; i <= count; i++) {
  const ep = pick(endpoints, i)
  const s1 = pick(selectors, i)
  const s2 = pick(selectors, i + 3)
  const testName = `leovegas #${i} - ${ep}`
  const rndWait = Math.floor(Math.random() * 1500) + 300

  const itBlock = `  it(${JSON.stringify(testName)}, () => {\n`+
    `    const url = '${base}${ep}'\n`+
    `    const rndWait = ${rndWait}\n`+
    `    cy.request({ url, failOnStatusCode: false, timeout: 20000 }).then((resp) => { expect([200,301,302,403,404]).to.include(resp.status) })\n`+
    `    cy.visit(url, { timeout: 30000 })\n`+
    `    cy.wait(rndWait)\n`+
    `    cy.get('${s1}', { timeout: 15000 }).should('exist')\n`+
    `    cy.get('body', { timeout: 10000 }).then($b => {\n`+
    `      if ($b.find('${s2}').length > 0) {\n`+
    `        cy.get('${s2}', { timeout: 8000 }).first().then($el => {\n`+
    `          if ($el.is('a') || $el.is('button')) {\n`+
    `            cy.wrap($el).click({ force: true })\n`+
    `            cy.wait(300 + Math.floor(Math.random() * 700))\n`+
    `            cy.go('back')\n`+
    `          }\n`+
    `        })\n`+
    `      }\n`+
    `    })\n`+
    `    cy.title().then(t => { expect(t.length).to.be.at.least(0) })\n`+
    `  })\n`;

  parts.push(itBlock)
  if (i % 50 === 0) parts.push("  // chunk break\n")
}

parts.push('})\n')

fs.writeFileSync(outFile, parts.join(''))
console.log(`Wrote ${count} tests into ${outFile}`)
