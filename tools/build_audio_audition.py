"""Build the offline audition page/catalog. Never writes source audio or runtime assets.

Run: python tools/build_audio_audition.py
Rescans and decodes the audio tree using the existing local audit tool. The manifest
preserves event IDs/order/suggestions; its embedded copy needs no file:// fetch.
"""
from datetime import datetime, timezone
import argparse
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / 'AUDIO_AUDITION_MANIFEST.json'
OUT = ROOT / 'core/build/reports/task05_1a_1'


def build(events=None, audit_path=None):
    if events is None:
        if MANIFEST.exists():
            events = json.loads(MANIFEST.read_text(encoding='utf-8'))['events']
        else:
            page = (ROOT / 'AUDIO_AUDITION.html').read_text(encoding='utf-8')
            events = json.loads(re.search(
                r'<script id="plan" type="application/json">(.*?)</script>', page, re.S)[1])
    events = [{key: value for key, value in event.items() if key != 'choices'}
              for event in events if event['group'] != 'Future']
    assert len(events) == 49 and len({e['id'] for e in events}) == 49
    if audit_path is None:
        import audit_audio_assets as audit
        audit.OUT = OUT
        audit.main()
        audit_path = OUT / 'audio-audit.json'
    audit_data = json.loads(Path(audit_path).read_text(encoding='utf-8'))
    catalog, excluded = [], []
    for row in audit_data['files']:
        reason = ('pack preview' if row['class'] == 'PREVIEW_NOT_RUNTIME' else
                  'decode/container/duration error' if row['pcm'].get('decode') != 'OK'
                  or row.get('containerErrors') or not row.get('duration', 0) > 0 else None)
        if reason:
            excluded.append({'path': row['path'], 'reason': reason})
            continue
        # Validate exact source bytes so --audit cannot quietly reuse a stale scan.
        import hashlib
        path = ROOT / 'asset_sources/audio' / row['path']
        assert hashlib.sha256(path.read_bytes()).hexdigest() == row['sha256'], row['path']
        catalog.append(dict(path=row['path'], kind=row['class'], name=path.name,
                            source=path.parent.relative_to(ROOT / 'asset_sources/audio').as_posix(),
                            duration=row['duration'], format=row['format'], bytes=row['bytes'],
                            sha256=row['sha256']))
    catalog.sort(key=lambda item: (item['name'].casefold(), item['path'].casefold()))
    by_path = {item['path']: item for item in catalog}
    for event in events:
        for candidate in event['candidates']:
            assert candidate in by_path, (event['id'], candidate)
            assert by_path[candidate]['kind'] == ('BGM' if event['group'] == 'BGM' else 'SFX')
    manifest = dict(schema_version=2, generated_at=datetime.now(timezone.utc).isoformat(),
                    asset_root='asset_sources/audio/',
                    counts=dict(events=len(events), sfx=sum(a['kind'] == 'SFX' for a in catalog),
                                bgm=sum(a['kind'] == 'BGM' for a in catalog),
                                scanned_files=audit_data['summary']['totalFiles']),
                    events=events, audio=catalog, excluded=excluded)
    serialized = json.dumps(manifest, ensure_ascii=False, indent=2)
    template = (ROOT / 'tools/audio/audition_template.html').read_text(encoding='utf-8')
    assert template.count('__MANIFEST__') == 1
    page = template.replace('__MANIFEST__', serialized.replace('<', '\\u003c'))
    MANIFEST.write_text(serialized + '\n', encoding='utf-8')
    (ROOT / 'AUDIO_AUDITION.html').write_text(page, encoding='utf-8')
    print(json.dumps(manifest['counts']))


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--audit', type=Path, help='Reuse a verified scan JSON (source hashes checked).')
    build(audit_path=parser.parse_args().audit)
