#!/usr/bin/env python3
import os
import glob
import xml.etree.ElementTree as ET
from html import escape

REPORT_DIR = os.path.join('target', 'site')
SRC_DIR = os.path.join('target', 'surefire-reports')
OUT_FILE = os.path.join(REPORT_DIR, 'surefire-report.html')

os.makedirs(REPORT_DIR, exist_ok=True)

tests = []
summary = {'tests':0,'failures':0,'errors':0,'skipped':0,'time':0.0}

for path in glob.glob(os.path.join(SRC_DIR, 'TEST-*.xml')):
    try:
        tree = ET.parse(path)
        root = tree.getroot()
        t = int(root.attrib.get('tests', '0'))
        f = int(root.attrib.get('failures', '0'))
        e = int(root.attrib.get('errors', '0'))
        s = int(root.attrib.get('skipped', '0'))
        time = float(root.attrib.get('time', '0'))
        summary['tests'] += t
        summary['failures'] += f
        summary['errors'] += e
        summary['skipped'] += s
        summary['time'] += time

        for case in root.findall('.//testcase'):
            name = case.attrib.get('name')
            classname = case.attrib.get('classname')
            time = case.attrib.get('time', '0')
            status = 'passed'
            msg = ''
            if case.find('failure') is not None:
                status = 'failure'
                msg = case.find('failure').attrib.get('message','') or case.find('failure').text or ''
            elif case.find('error') is not None:
                status = 'error'
                msg = case.find('error').attrib.get('message','') or case.find('error').text or ''
            elif case.find('skipped') is not None:
                status = 'skipped'
                msg = case.find('skipped').attrib.get('message','') or case.find('skipped').text or ''
            tests.append({'name':name,'classname':classname,'time':time,'status':status,'message':msg})
    except Exception as ex:
        print('Failed to parse', path, ex)

html = []
html.append('<!doctype html>')
html.append('<html><head><meta charset="utf-8"><title>Surefire Report</title>')
html.append('<style>body{font-family:Arial,Helvetica,sans-serif}table{border-collapse:collapse;width:100%}th,td{border:1px solid #ddd;padding:8px}th{background:#f4f4f4}</style>')
html.append('</head><body>')
html.append(f'<h1>Surefire Report</h1>')
html.append(f'<p>Tests: {summary["tests"]} — Failures: {summary["failures"]} — Errors: {summary["errors"]} — Skipped: {summary["skipped"]} — Time: {summary["time"]:.2f}s</p>')
html.append('<table><thead><tr><th>Class</th><th>Test</th><th>Time (s)</th><th>Status</th><th>Message</th></tr></thead><tbody>')

for t in tests:
    html.append('<tr>')
    html.append(f'<td>{escape(t["classname"] or "")}</td>')
    html.append(f'<td>{escape(t["name"] or "")}</td>')
    html.append(f'<td>{escape(str(t["time"]))}</td>')
    cls = 'color:green' if t['status']=='passed' else ('color:orange' if t['status']=='skipped' else 'color:red')
    html.append(f'<td style="{cls}">{escape(t["status"])}</td>')
    html.append(f'<td>{escape(t["message"] or "")}</td>')
    html.append('</tr>')

html.append('</tbody></table>')
html.append('</body></html>')

with open(OUT_FILE, 'w', encoding='utf-8') as f:
    f.write('\n'.join(html))

print('Wrote', OUT_FILE)
