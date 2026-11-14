"""Rebuild the small Vietnamese/English bitmap font from local Pixel_4 system files, offline."""
from pathlib import Path
import gzip,hashlib,html,re,subprocess
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task04a_1';OUT.mkdir(parents=True,exist_ok=True)
ADB=str(Path.home()/'AppData/Local/Android/Sdk/platform-tools/adb.exe')
for remote,local in [('/system/fonts/RobotoStatic-Regular.ttf',OUT/'RobotoStatic-Regular.ttf'),('/system/etc/NOTICE.xml.gz',OUT/'android-NOTICE.xml.gz')]:
    if not local.exists():subprocess.run([ADB,'pull',remote,str(local)],check=True)
notice=gzip.decompress((OUT/'android-NOTICE.xml.gz').read_bytes()).decode('utf-8',errors='strict')
ident=None
for attributes,name in re.findall(r'<file-name([^>]*)>([^<]*)</file-name>',notice):
    if name.endswith('/RobotoStatic-Regular.ttf'):ident=re.search(r'contentId="([^"]+)"',attributes)[1];break
if ident is None:raise RuntimeError('No RobotoStatic license in the local Android NOTICE')
license=html.unescape(re.search(r'<file-content[^>]*contentId="'+re.escape(ident)+r'"[^>]*>(.*?)</file-content>',notice,re.S)[1].replace('<![CDATA[','').replace(']]>',''))
if 'Apache License' not in license:raise RuntimeError('Review changed source font license before regenerating')
font=OUT/'RobotoStatic-Regular.ttf';destination=ROOT/'assets/game/fonts'
subprocess.run(['java','-Dfile.encoding=UTF-8',str(ROOT/'tools/PrepareUiFont.java'),str(font),str(destination)],check=True)
header='Roboto bitmap UI font. Copyright 2011 Google Inc. All Rights Reserved.\nDerived offline from Pixel_4 API 37 /system/fonts/RobotoStatic-Regular.ttf.\nSource SHA-256: '+hashlib.sha256(font.read_bytes()).hexdigest()+'\nConverted to a 15px monochrome BMFont page; original TTF is not shipped.\nSource attribution/license from /system/etc/NOTICE.xml.gz follows.\n'
(destination/'Roboto-LICENSE.txt').write_text(header+license,encoding='utf-8')
(OUT/'Roboto-license-source.txt').write_text(license,encoding='utf-8')
