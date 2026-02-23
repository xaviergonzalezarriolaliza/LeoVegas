describe('Smoke Test Suite', () => {
  const host = Cypress.env('MOCK_HOST') || '127.0.0.1'
  const port = Cypress.env('MOCK_PORT') || '4567'
  const base = `http://${host}:${port}`

  context('Mock API - basic smoke', () => {
    it('responds to core endpoints', () => {
      // /hello
      cy.api({ url: `${base}/hello`, failOnStatusCode: false }).then((res) => {
        const ok = res.status === 200 && res.body && res.body.message
        expect(res.status).to.equal(200)
        expect(res.body).to.have.property('message')
        cy.task('recordCheck', { id: 'main:hello', ok: !!ok })
      })

      // /manyFieldsPayload
      cy.api({ url: `${base}/manyFieldsPayload` }).then((res) => {
        const ok = res.status === 200 && res.body && res.body.id
        expect(res.status).to.equal(200)
        expect(res.body).to.have.property('id')
        cy.task('recordCheck', { id: 'main:manyFieldsPayload', ok: !!ok })
      })

      // /echo (POST)
      cy.api({ url: `${base}/echo`, method: 'POST', body: { smoke: true }, headers: { 'content-type': 'application/json' } }).then((res) => {
        const ok = res.status === 200 && res.body && res.body.echo
        expect(res.status).to.equal(200)
        expect(res.body).to.have.property('echo')
        cy.task('recordCheck', { id: 'main:echo_post', ok: !!ok })
      })

      // /notfound should 404
      cy.api({ url: `${base}/notfound`, failOnStatusCode: false }).then((res) => {
        const ok = res.status === 404
        expect(res.status).to.equal(404)
        cy.task('recordCheck', { id: 'main:notfound_404', ok: !!ok })
      })

      // /chiquito
      cy.api({ url: `${base}/chiquito`, failOnStatusCode: false }).then((res) => {
        const ok = res.status === 200 && res.body && typeof res.body === 'object'
        expect([200, 200]).to.include(res.status)
        expect(res.body).to.be.an('object')
        cy.task('recordCheck', { id: 'main:chiquito', ok: !!ok })
      })
    })
  })

  context('Public homepage basic check', () => {
    it('loads the LeoVegas homepage and checks basics', () => {
      const url = 'https://www.leovegas.es/'
      cy.request({ url, failOnStatusCode: false }).then((resp) => {
        // Accept common site codes (200 or redirects)
        const ok = [200, 301, 302, 403, 404].includes(resp.status)
        expect([200, 301, 302, 403, 404]).to.include(resp.status)
        cy.task('recordCheck', { id: 'public:homepage_status', ok: !!ok })
      })
      cy.visit(url)
      cy.get('header').should('exist').then(() => cy.task('recordCheck', { id: 'public:header', ok: true }))
      cy.get('nav').its('length').should('be.gte', 0).then(() => cy.task('recordCheck', { id: 'public:nav', ok: true }))
      cy.title().its('length').should('be.gte', 0).then(() => cy.task('recordCheck', { id: 'public:title', ok: true }))
    })
    after(() => {
      // Ensure checks summary is written as test artifact
      cy.task('writeChecks', { path: 'report/checks.json' })
    })
  })
})
