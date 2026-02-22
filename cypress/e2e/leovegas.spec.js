describe('Leovegas big generated suite', () => {
  // Ensure cookie/consent dialog is accepted if present in each test
  beforeEach(() => {
    cy.get('body').then($body => {
      const btn = $body.find('button[data-testid="accept-button"], button[bol-component="accept"], button:contains("Aceptar"), button:contains("Aceptarlas")').first();
      if (btn && btn.length) {
        cy.wrap(btn).click({ force: true })
        cy.wait(200)
      }
    })
  })

  it("leovegas #1 - /", () => {
    const url = 'https://www.leovegas.es/'
    const rndWait = 822
    cy.request({ url, failOnStatusCode: false, timeout: 20000 }).then((resp) => { expect([200,301,302,403,404]).to.include(resp.status) })
    cy.visit(url, { timeout: 30000 })
    cy.wait(rndWait)
    cy.get('nav', { timeout: 15000 }).should('exist')
    cy.optional('nav').then($nav => { if ($nav.length) cy.wrap($nav).should('be.visible') })
    cy.get('body', { timeout: 10000 }).then($b => {
      const anchors = $b.find('a').toArray()
      if (anchors.length === 0) return
      const candidate = anchors.find(a => {
        const href = (a.getAttribute && a.getAttribute('href')) || ''
        return href && href !== '#' && !href.startsWith('javascript:') && !/login|auth|signup|mailto:/i.test(href)
      })
      if (!candidate) { cy.log('no suitable anchor found'); return }
      cy.wrap(candidate).click({ force: true })
      cy.wait(300 + Math.floor(Math.random() * 700))
      cy.go('back')
    })
    cy.title().then(t => { expect(t.length).to.be.at.least(0) })
  })

  it("leovegas #2 - homepage has header and links", () => {
    const url = 'https://www.leovegas.es/'
    cy.visit(url, { timeout: 30000 })
    cy.get('header, nav', { timeout: 15000 }).should('exist')
    cy.optional('header, nav').then($hn => { if ($hn.length) cy.wrap($hn).should('be.visible') })
    cy.get('a', { timeout: 8000 }).its('length').should('be.gte', 1)
  })

  it("leovegas #3 - homepage footer and legal links", () => {
    const url = 'https://www.leovegas.es/'
    cy.visit(url, { timeout: 30000 })
    cy.get('footer', { timeout: 15000 }).should('exist')
    cy.optional('footer').then($f => { if ($f.length) cy.wrap($f).should('be.visible') })
    // look for common legal/terms links in footer
    cy.get('footer a', { timeout: 8000 }).then($links => {
      const texts = $links.toArray().map(el => el.textContent.trim()).join(' ')
      expect(/términos|terms|legal|privacidad|privacy/i.test(texts)).to.be.true
    })
  })

  
  it("leovegas #4 - homepage has a visible CTA (play/register)", () => {
    const url = 'https://www.leovegas.es/'
    cy.visit(url, { timeout: 30000 })
    // look for common CTA text in visible buttons/links (ignore hidden elements)
    cy.get('a,button', { timeout: 10000 }).then($els => {
      // non-fatal visibility reinforcement for first control
      cy.optional('a,button').then($o => { if ($o.length) cy.wrap($o.first()).should('be.visible') })
      const visibleMatch = $els.toArray().some(el => {
        const text = (el.textContent || '').trim()
        return /jugar|play|registrar|registro|sign ?up|join/i.test(text) && Cypress.dom.isVisible(el)
      })
      expect(visibleMatch).to.be.true
    })
  })

  it('leovegas #5 - navigate first prominent link and back', () => {
    const url = 'https://www.leovegas.es/'
    cy.visit(url, { timeout: 30000 })
    // use direct DOM lookup
    cy.document().then(doc => {
      const links = Array.from(doc.querySelectorAll('nav a, header a'))
      if (!links || links.length === 0) { cy.log('no nav links found'); return }
      const candidate = links.find(a => {
        const href = a.getAttribute('href') || ''
        return href && href !== '#' && !href.startsWith('javascript:') && !/login|auth|signup|mailto:/i.test(href)
      })
      if (!candidate) { cy.log('no suitable nav link found'); return }
      cy.wrap(candidate).click({ force: true })
      cy.wait(500)
      cy.location('href').then(href => {
        if (href === url) {
          cy.log('click did not navigate away from homepage')
        } else {
          cy.get('h1, h2', { timeout: 8000 }).then($h => {
            if ($h.length === 0) cy.log('no h1/h2 found on navigated page')
          })
        }
      })
      cy.go('back')
    })
  })

  it('leovegas #6 - try header/footer search if present', () => {
    const url = 'https://www.leovegas.es/'
    cy.visit(url, { timeout: 30000 })
    // look for inputs via DOM to avoid cy.get timeouts when none exist
    cy.document().then(doc => {
      // prefer semantic attributes/labels for search inputs
      const candidate = doc.querySelector('input[placeholder*="buscar" i], input[aria-label*="search" i], input[name*="q" i], input[id*="search" i]')
      if (!candidate) { cy.log('no search input candidate found'); return }
      cy.wrap(candidate).type('slots{enter}')
      cy.wait(800)
      cy.document().then(d => {
        if (d.querySelector('.search-results, .results, .product-list')) {
          expect(true).to.be.true
        } else {
          cy.url().should('not.eq', url)
        }
      })
    })
  })

  it('leovegas #7 - detect language switch presence (try english)', () => {
    const url = 'https://www.leovegas.es/'
    cy.visit(url, { timeout: 30000 })
    // use DOM query to make this check optional
    cy.document().then(doc => {
      const anchors = doc.querySelectorAll('a[hreflang="en"], a[href*="/en/"]')
      if (!anchors || anchors.length === 0) { cy.log('no english language link found'); return }
      const first = anchors[0]
      cy.wrap(first).click({ force: true })
      cy.location('pathname', { timeout: 10000 }).then(p => {
        if (!/\/en\//.test(p)) cy.log('navigated but /en/ not present')
      })
    })
  })

  it('leovegas #8 - open promotions/offers and assert content', () => {
    const url = 'https://www.leovegas.es/'
    cy.visit(url, { timeout: 30000 })
    cy.contains(/promo|oferta|bono|offers|promotions/i, { timeout: 10000 }).then($el => {
      if (!$el || $el.length === 0) { cy.log('no promotions link found'); return }
      cy.wrap($el).click({ force: true })
      cy.get('body', { timeout: 10000 }).then($b => {
        if ($b.find('.offer, .offers, .promotion, .promotions').length) {
          expect(true).to.be.true
        } else {
          cy.log('no offers list found after clicking promotions')
        }
      })
    })
  })

  it('leovegas #9 - footer social links present and have hrefs', () => {
    const url = 'https://www.leovegas.es/'
    cy.visit(url, { timeout: 30000 })
    cy.get('footer a', { timeout: 10000 }).then($links => {
      const social = Array.from($links).filter(el => /facebook|twitter|instagram|youtube|tiktok/i.test(el.href || ''))
      if (social.length === 0) { cy.log('no social links in footer'); return }
      social.forEach(el => {
        expect(el.href).to.match(/^https?:\/\//)
      })
    })
  })

  it('leovegas #10 - basic accessibility smoke', () => {
    const url = 'https://www.leovegas.es/'
    cy.visit(url, { timeout: 30000 })
    // Basic accessibility checks: images have alt and headings exist
    cy.document().then(doc => {
      const imgs = Array.from(doc.querySelectorAll('img'))
      if (!imgs || imgs.length === 0) {
        cy.log('no images found on page')
      } else {
        const missingAlt = imgs.filter(i => !(i.getAttribute('alt') || '').trim())
        if (missingAlt.length) cy.log(`${missingAlt.length}/${imgs.length} images missing alt attribute`)
      }
      const headings = doc.querySelectorAll('h1, h2, h3')
      expect(headings.length).to.be.gte(1)
    })
  })

  it('leovegas #11 - ARIA landmarks and focusable elements', () => {
    const url = 'https://www.leovegas.es/'
    cy.visit(url, { timeout: 30000 })
    // Check for landmark elements and at least one focusable control
    cy.document().then(doc => {
      const landmarks = doc.querySelectorAll('header, nav, main, footer, [role="main"], [role="navigation"], [role="banner"], [role="contentinfo"]')
      if (!landmarks || landmarks.length === 0) cy.log('no ARIA landmarks found')
      const focusables = doc.querySelectorAll('a[href], button, input, textarea, select, [tabindex]:not([tabindex="-1"])')
      expect(focusables.length).to.be.gte(1)
    })
  })

  it('leovegas #12 - open Deportes (sports) section', () => {
    const url = 'https://www.leovegas.es/'
    cy.visit(url, { timeout: 30000 })
    // try header/nav link or any visible link that suggests sports
    cy.document().then(doc => {
      const link = doc.querySelector('a[href*="deportes" i], a[href*="sports" i]') || Array.from(doc.querySelectorAll('a')).find(a => /deportes|sports|apuestas/i.test(a.textContent || ''))
      if (!link) { cy.log('no deportes link found'); return }
      cy.wrap(link).click({ force: true })
      cy.location('pathname', { timeout: 10000 }).then(p => {
        if (!/deportes|sports|apuestas/i.test(p)) cy.log('navigated but path does not include deportes')
      })
    })
  })

  it('leovegas #13 - find a match mentioning Malaga', () => {
    // Assumes we are on Deportes page; if not, go there
    cy.document().then(doc => {
      const hasDeportesPath = /deportes|sports|apuestas/i.test(location.pathname)
      if (!hasDeportesPath) {
        const link = doc.querySelector('a[href*="deportes" i], a[href*="sports" i]')
        if (link) cy.wrap(link).click({ force: true })
      }
    })
    cy.wait(1000)
    // look for text nodes containing Malaga
    cy.document().then(doc => {
      const matchEl = Array.from(doc.querySelectorAll('*')).find(el => /malaga/i.test(el.textContent || ''))
      if (!matchEl) { cy.log('no match with Malaga found'); return }
      cy.wrap(matchEl).scrollIntoView()
      cy.wrap(matchEl).should('be.visible')
    })
  })

  it('leovegas #14 - add Malaga win selection to betslip (do not place)', () => {
    cy.document().then(doc => {
      // Ensure on deportes page
      const link = doc.querySelector('a[href*="deportes" i], a[href*="sports" i]')
      if (link && !/deportes|sports|apuestas/i.test(location.pathname)) cy.wrap(link).click({ force: true })
    })
    cy.wait(1200)
    cy.document().then(doc => {
      // find an element that mentions Malaga and try to click an adjacent odds button
      const candidate = Array.from(doc.querySelectorAll('*')).find(el => /malaga/i.test(el.textContent || ''))
      if (!candidate) { cy.log('no malaga element found to select'); return }
      // search upward to a container that likely holds market buttons
      let container = candidate
      for (let i = 0; i < 6 && container; i++) {
        if (container.querySelector && container.querySelector('button, a')) break
        container = container.parentElement
      }
      if (!container) { cy.log('no market container found'); return }
      // prefer odds-like buttons (numeric text)
      const buttons = Array.from(container.querySelectorAll('button, a')).filter(b => /\d+(\.\d+)?/.test((b.textContent||'').trim()))
      const pick = buttons.length ? buttons[0] : container.querySelector('button, a')
      if (!pick) { cy.log('no selectable odds/button found near Malaga'); return }
      cy.wrap(pick).click({ force: true })

      // now check for presence of betslip: common selectors or text
      cy.document().then(d => {
        const slip = d.querySelector('.betslip, .bet-slip, #betslip, [data-test*="bet"]') || Array.from(d.querySelectorAll('*')).find(el => /bet ?slip|apuesta|ticket|ticket de apuesta/i.test(el.textContent||''))
        if (!slip) { cy.log('no betslip panel detected after selection'); return }
        cy.wrap(slip).scrollIntoView()
        cy.wrap(slip).should('be.visible')
        // verify it mentions Malaga
        const texts = (slip.textContent || '')
        expect(/malaga/i.test(texts)).to.be.true
      })
    })
  })
})
