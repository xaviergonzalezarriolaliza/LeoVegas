Cypress.Commands.add
// Load cypress-plugin-api to expose `cy.api`
try {
  require('cypress-plugin-api')
} catch (e) {
  // plugin may not be installed in all environments; fallback gracefully
}

// Optional selector helper: returns a jQuery collection (may be empty) without failing
Cypress.Commands.add('optional', (selector) => {
  return cy.document().then(doc => {
    const els = doc.querySelectorAll(selector)
    return cy.wrap(Cypress.$(els))
  })
})

// Automatically click the "accept" / consent button when pages load or when it's added to the DOM
Cypress.on('window:load', (win) => {
  const tryClick = () => {
    try {
      // If an element with id ":rj:" exists, click it directly (no escaping needed)
      const rj = win.document.getElementById(':rj:')
      if (rj) { rj.click(); return }
      const sel = 'button[data-testid="accept-button"], button[bol-component="accept"]'
      const btn = win.document.querySelector(sel)
      if (btn) { btn.click(); return }

      const buttons = Array.from(win.document.querySelectorAll('button'))
      const found = buttons.find(b => /Aceptarlas|Aceptar/i.test(b.textContent))
      if (found) { found.click(); return }
    } catch (e) {
      // ignore cross-origin or timing errors
    }
  }

  tryClick()
  const root = win.document.body || win.document.documentElement
  if (!root) return
  const mo = new win.MutationObserver(() => tryClick())
  mo.observe(root, { childList: true, subtree: true })
})
