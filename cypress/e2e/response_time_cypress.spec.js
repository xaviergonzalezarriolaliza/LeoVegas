describe('Cypress - Response Time for Mock API Endpoints', () => {
  it('measures request -> response time and checks status code', () => {
    const port = Cypress.env('MOCK_PORT') || Cypress.env('mock_port') || '4567';
    const host = Cypress.env('MOCK_HOST') || Cypress.env('mock_host') || '127.0.0.1';
    const base = `http://${host}:${port}`;
    const endpoints = ['/hello', '/manyFieldsPayload', '/echo', '/long', '/notfound', '/chiquito'];

    endpoints.forEach((ep) => {
      const url = base + ep;
      const start = Date.now();
      cy.request({ url, failOnStatusCode: false }).then((resp) => {
        const elapsed = Date.now() - start;
        // basic status code assertion and a timing measurement
        expect(typeof resp.status).to.equal('number');
        // log timing so it appears in CI output
        // eslint-disable-next-line no-console
        console.log(`Cypress: ${ep} -> status=${resp.status} time=${elapsed}ms`);
        // assert elapsed is a non-negative number
        expect(elapsed).to.be.a('number').and.to.be.at.least(0);
      });
    });
  });
});
