#!/usr/bin/env node
const fs = require('fs')
const path = require('path')

const CHECKS_PATH = process.env.CHECKS_PATH || path.join(process.cwd(), 'report', 'checks.json')
const OUT_JSON = process.env.OUT_JSON || path.join(process.cwd(), 'report', 'checks-summary.json')
const OUT_HTML = process.env.OUT_HTML || path.join(process.cwd(), 'report', 'checks-summary.html')

function safeRead(p) {
  try {
    return JSON.parse(fs.readFileSync(p, 'utf8'))
  } catch (e) {
    return null
  }
}

const summary = safeRead(CHECKS_PATH)
if (!summary) {
  console.error('No checks summary found at', CHECKS_PATH)
  process.exit(2)
}

// summary.checks is an object of id -> {pass, total}
const checks = summary.checks || {}
let passCount = 0
let totalCount = 0
for (const k of Object.keys(checks)) {
  const c = checks[k]
  passCount += (c.pass || 0)
  totalCount += (c.total || 0)
}

const percent = totalCount === 0 ? 0 : Math.round((passCount / totalCount) * 10000) / 100

const out = {
  generatedAt: new Date().toISOString(),
  checksFile: CHECKS_PATH,
  totalChecks: Object.keys(checks).length,
  totalAssertions: totalCount,
  passedAssertions: passCount,
  passPercent: percent,
}

fs.mkdirSync(path.dirname(OUT_JSON), { recursive: true })
fs.writeFileSync(OUT_JSON, JSON.stringify(out, null, 2))

const html = `<!doctype html>
<html>
<head><meta charset="utf-8"><title>Checks Summary</title></head>
<body>
  <h1>Checks Summary</h1>
  <p>Generated: ${out.generatedAt}</p>
  <ul>
    <li>Total unique checks: ${out.totalChecks}</li>
    <li>Total assertions: ${out.totalAssertions}</li>
    <li>Passed assertions: ${out.passedAssertions}</li>
    <li>Pass %: ${out.passPercent}%</li>
  </ul>
  <pre>${JSON.stringify(checks, null, 2)}</pre>
</body>
</html>`

fs.writeFileSync(OUT_HTML, html)

console.log('Wrote', OUT_JSON, 'and', OUT_HTML)
console.log('Pass %:', out.passPercent)

// If threshold provided, exit non-zero when below threshold
const threshold = parseFloat(process.env.CHECKS_THRESHOLD || '')
if (!Number.isNaN(threshold)) {
  if (out.passPercent < threshold) {
    console.error(`Pass percent ${out.passPercent}% < threshold ${threshold}%, failing job.`)
    process.exit(3)
  }
}

process.exit(0)
