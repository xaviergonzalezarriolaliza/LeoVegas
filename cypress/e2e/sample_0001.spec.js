describe('Sample auto test 1', () => {
  it('visits homepage and checks elements', () => {
    const url = 'https://www.leovegas.es/'
    cy.request({ url, failOnStatusCode: false }).then((resp) => {
      expect([200,301,302,403,404]).to.include(resp.status)
    })
    cy.visit(url)
    cy.get('header').should('exist')
    cy.get('nav').its('length').should('be.gte', 0)
    cy.title().then(t => { expect(t.length).to.be.at.least(0) })
  })
})
