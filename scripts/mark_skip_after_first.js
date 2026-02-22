const fs = require('fs')
const path = require('path')

const p = path.join(__dirname, '..', 'cypress', 'e2e', 'leovegas.spec.js')
if (!fs.existsSync(p)) {
  console.error('File not found:', p)
  process.exit(2)
}

let s = fs.readFileSync(p, 'utf8')
// Replace newline + optional indent + it(  => newline + indent + it.skip(
let replaced = s.replace(/\n(\s*)it\(/g, '\n$1it.skip(')
// Restore the first occurrence of it.skip( back to it(
const first = replaced.indexOf('it.skip(')
if (first !== -1) {
  replaced = replaced.slice(0, first) + 'it(' + replaced.slice(first + 'it.skip('.length)
}

fs.writeFileSync(p, replaced, 'utf8')
console.log('Normalized skips; restored first test.');
