describe('Cypress - Response Time for Mock API Endpoints', () => {
  it('measures request -> response time and checks status code', () => {
    const port = Cypress.env('MOCK_PORT') || Cypress.env('mock_port') || '4567';
    const host = Cypress.env('MOCK_HOST') || Cypress.env('mock_host') || '127.0.0.1';
    const base = `http://${host}:${port}`;
    const endpoints = ['/hello', '/manyFieldsPayload', '/echo', '/long', '/notfound', '/chiquito'];

    endpoints.forEach((ep) => {
      const url = base + ep;
      const start = Date.now();
      cy.api({ url, failOnStatusCode: false }).then((resp) => {
        const elapsed = Date.now() - start;
        // assert there is a numeric status and log timing for CI
        expect(resp).to.have.property('status').that.is.a('number');
        // eslint-disable-next-line no-console
        console.log(`Cypress: ${ep} -> status=${resp.status} time=${elapsed}ms`);
        expect(elapsed).to.be.a('number').and.to.be.at.least(0);
      });
    });
  });
});
