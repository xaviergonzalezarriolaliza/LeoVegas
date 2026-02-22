describe('Mock API - Cypress reproduction of MockApiTest', () => {
  const base = 'http://localhost:4567'

  // Ensure server is reachable before running tests
  before(() => {
    const tries = 10
    const wait = 500
    let attempt = 0
    function ping() {
      attempt++
      return cy.api({ url: `${base}/hello`, failOnStatusCode: false }).then((res) => {
        if (res.status === 200) return
        if (attempt < tries) return cy.wait(wait).then(ping)
        throw new Error('Mock API not reachable')
      })
    }
    ping()
  })

  it('testHelloEndpoint', () => {
    cy.api(`${base}/hello`).then((res) => {
      expect(res.status).to.equal(200)
      expect(res.headers['content-type']).to.include('application/json')
      expect(res.body).to.have.property('message', 'Hello, LeoVegas!')
    })

    cy.api({ url: `${base}/hello`, method: 'POST', failOnStatusCode: false }).then((res) => {
      expect([404, 405]).to.include(res.status)
    })
  })

  it('testEchoEndpoint', () => {
    // Use a sample payload from the mock server so tests mirror real cases
    cy.api(`${base}/manyFieldsPayload`).then((sample) => {
      expect(sample.status).to.equal(200)
      const payload = {
        id: sample.body.id || 123,
        device: sample.body.device || 'iPhone',
        os: sample.body.os || 'iOS 17',
        foo: 'bar'
      }

      cy.api({ url: `${base}/echo`, method: 'POST', body: payload, headers: { 'content-type': 'application/json' } }).then((res) => {
        expect(res.status).to.equal(200)
        expect(res.headers['content-type']).to.include('application/json')
        expect(res.body).to.have.nested.property('echo.id', payload.id)
        expect(res.body).to.have.nested.property('echo.device', payload.device)
        expect(res.body).to.have.nested.property('echo.os', payload.os)
        expect(res.body).to.have.nested.property('echo.foo', 'bar')
      })

      cy.api({ url: `${base}/echo`, method: 'GET', failOnStatusCode: false }).then((res) => {
        expect([404, 405]).to.include(res.status)
      })
    })
  })

  it('testLongResponseTime', () => {
    cy.api({ url: `${base}/long`, timeout: 20000 }).then((res) => {
      expect(res.status).to.equal(200)
      // record response time for debug (Cypress doesn't expose .time(), but request has duration in ms)
      // we can at least assert body present
      expect(res.body).to.have.property('message')
    })

    cy.api({ url: `${base}/long`, method: 'POST', failOnStatusCode: false }).then((res) => {
      expect([404, 405]).to.include(res.status)
    })
  })

  it('testInvalidEndpoint', () => {
    cy.api({ url: `${base}/notfound`, failOnStatusCode: false }).then((res) => {
      expect(res.status).to.equal(404)
    })
    cy.api({ url: `${base}/notfound`, method: 'POST', failOnStatusCode: false }).then((res) => {
      expect(res.status).to.equal(404)
    })
  })

  it('testEchoEndpointWithMissingOrInvalidPayload', () => {
    // empty body
    cy.api({ url: `${base}/echo`, method: 'POST', headers: { 'content-type': 'application/json' }, body: '', failOnStatusCode: false }).then((res) => {
      expect(res.status).to.equal(200)
      expect(res.body).to.have.property('echo', '')
    })

    // derive invalid and partial payloads from the sample
    cy.api(`${base}/manyFieldsPayload`).then((sample) => {
      const sampleJson = JSON.stringify(sample.body || {})
      const invalid = sampleJson.substring(0, Math.max(1, sampleJson.length - 1))
      cy.api({ url: `${base}/echo`, method: 'POST', body: invalid, headers: { 'content-type': 'application/json' } }).then((res) => {
        expect(res.status).to.equal(200)
        expect(res.body).to.have.property('echo', invalid)
      })

      const partial = { id: sample.body.id, device: sample.body.device }
      cy.api({ url: `${base}/echo`, method: 'POST', body: partial, headers: { 'content-type': 'application/json' } }).then((res) => {
        expect(res.status).to.equal(200)
        expect(res.body).to.have.nested.property('echo.id', partial.id)
        expect(res.body).to.have.nested.property('echo.device', partial.device)
      })
    })

    // PUT not allowed
    cy.api({ url: `${base}/echo`, method: 'PUT', body: { foo: 'bar' }, failOnStatusCode: false }).then((res) => {
      expect([404, 405]).to.include(res.status)
    })
  })

  it('testManyFieldsPayload', () => {
    cy.api(`${base}/manyFieldsPayload`).then((res) => {
      expect(res.status).to.equal(200)
      expect(res.headers['content-type']).to.include('application/json')
      expect(res.body).to.include({ id: 1001, name: 'Alice', email: 'alice@example.com', age: 30, country: 'SE', city: 'Stockholm', device: 'Android', os: 'Android 14', appVersion: '5.2.1', sessionId: 'sess-abc-123', isPremium: true, locale: 'sv-SE', currency: 'SEK', features: 'A,B,C', tags: 'tag1,tag2', notes: 'test user with many fields' })
      // balance may be a number close to 1234.56
      expect(res.body.balance).to.be.closeTo(1234.56, 0.01)
      expect(res.body.lastLogin).to.equal('2026-02-20T10:00:00Z')
    })
  })

  it('testEchoEndpointWithLargePayload', () => {
    // Build a large payload based on the server-provided sample notes field
    cy.api(`${base}/manyFieldsPayload`).then((sample) => {
      const baseNotes = (sample.body && sample.body.notes) || 'x'
      let large = baseNotes.repeat(Math.ceil(10000 / baseNotes.length)).substring(0, 10000)
      const body = { data: large }
      cy.api({ url: `${base}/echo`, method: 'POST', body: body, headers: { 'content-type': 'application/json' } }).then((res) => {
        expect(res.status).to.equal(200)
        expect(res.headers['content-type']).to.include('application/json')
        expect(res.body).to.have.nested.property('echo.data', large)
      })
    })
  })
})
