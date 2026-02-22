#!/usr/bin/env python3
import json
import os
from html import escape

SRC = os.path.join('cypress', 'results', 'mochawesome.json')
OUT_DIR = os.path.join('cypress', 'results', 'html')
OUT_FILE = os.path.join(OUT_DIR, 'mochawesome-report.html')

os.makedirs(OUT_DIR, exist_ok=True)

try:
    with open(SRC, 'r', encoding='utf-8') as f:
        data = json.load(f)
except Exception as e:
    print('Could not read mochawesome JSON:', e)
    raise

stats = data.get('stats', {})
results = []
for r in data.get('results', []):
    for suite in r.get('suites', []):
        title = suite.get('title') or 'Root Suite'
        for t in suite.get('tests', []):
            results.append({
                'suite': title,
                'title': t.get('title'),
                'fullTitle': t.get('fullTitle'),
                'state': t.get('state'),
                'duration': t.get('duration'),
                'err': t.get('err') or {}
            })

html = []
html.append('<!doctype html>')
html.append('<html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">')
html.append('<title>Mochawesome Report</title>')
html.append('<style>body{font-family:Arial,Helvetica,sans-serif;margin:16px} .passed{color:green}.failed{color:red}.test{border-bottom:1px solid #eee;padding:8px}</style>')
html.append('</head><body>')
html.append(f"<h1>Mochawesome Report</h1>")
html.append(f"<div>Tests: {stats.get('tests',0)} | Passes: {stats.get('passes',0)} | Failures: {stats.get('failures',0)} | Duration: {stats.get('duration',0)}ms</div>")
html.append('<div style="margin-top:12px">')
for t in results:
    cls = 'passed' if t['state']=='passed' else 'failed'
    html.append(f"<div class=\"test\"><strong>{escape(t['fullTitle'] or t['title'])}</strong> <span class=\"{cls}\">{t['state']}</span><div class=\"small\">{t['duration']}ms</div></div>")
html.append('</div>')
html.append('</body></html>')

with open(OUT_FILE, 'w', encoding='utf-8') as f:
    f.write('\n'.join(html))

print('Wrote', OUT_FILE)
