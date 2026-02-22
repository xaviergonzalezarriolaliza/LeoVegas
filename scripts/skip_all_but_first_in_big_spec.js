const fs = require('fs')
const path = require('path')

const p = path.join(__dirname, '..', 'cypress', 'e2e', 'leovegas.spec.js')
if (!fs.existsSync(p)) {
  console.error('File not found:', p)
  process.exit(2)
}

let s = fs.readFileSync(p, 'utf8')
const regex = /^\s*it\(/gm
let matches = []
let m
while ((m = regex.exec(s)) !== null) matches.push(m.index)

if (matches.length <= 1) {
  console.log('No or only one test found, nothing to skip.')
  process.exit(0)
}

let arr = s.split('')
for (let i = 1; i < matches.length; i++) {
  const pos = s.indexOf('it(', matches[i])
  if (pos !== -1) {
    arr.splice(pos + 2, 0, '.skip')
  }
}

const out = arr.join('')
fs.writeFileSync(p, out, 'utf8')
console.log('Skipped', matches.length - 1, 'tests in', p)
