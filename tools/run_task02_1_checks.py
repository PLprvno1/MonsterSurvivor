"""Offline, phase-specific checks. No APK/AAB packaging or installation."""
from pathlib import Path
import os
import subprocess
import sys

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'core/build/reports/task02_1'
OUT.mkdir(parents=True,exist_ok=True)
cache=Path.home()/'.gradle/caches/modules-2/files-2.1'
entries=[str(ROOT/'core/build/classes/java/main')]
for group in ['com.badlogicgames.gdx','org.lwjgl','com.badlogicgames.jlayer','org.jcraft']:
    parent=cache/group
    if parent.exists():
        for artifact in parent.iterdir():
            versions=sorted(p for p in artifact.iterdir() if p.is_dir())
            if versions:entries.extend(str(p) for p in versions[-1].rglob('*.jar') if not p.name.endswith(('-sources.jar','-javadoc.jar')))
classes=ROOT/'core/build/task02_1-check-classes';classes.mkdir(parents=True,exist_ok=True)
cp=os.pathsep.join(entries)
phase=sys.argv[1] if len(sys.argv)>1 else 'a'
sources=[ROOT/'tools/checks/Task021Checks.java']
if phase=='final':sources += [ROOT/p for p in ['tools/smoke/Task021Smoke.java','tools/checks/FoundationChecks.java','tools/checks/CameraChecks.java','tools/smoke/TerrainProbe.java']]
subprocess.run(['javac','-encoding','UTF-8','-cp',cp,'-d',str(classes),*map(str,sources)],check=True)
cp+=os.pathsep+str(classes)
argfile=OUT/'java-args.txt';argfile.write_text('-cp\n"'+cp.replace('\\','/')+'"\n',encoding='utf-8')
commands=[('Task021Checks',[phase])] if phase!='final' else [
    ('Task021Checks',['a']),('Task021Checks',['b']),('com.example.monstersurvivor.FoundationChecks',[]),('CameraChecks',[]),('Task021Smoke',[]),('TerrainProbe',[])]
for name,args in commands:
    result=subprocess.run(['java','-Dms.report=../core/build/reports/task02_1/','@'+str(argfile),name,*args],cwd=ROOT/'assets',text=True,capture_output=True)
    (OUT/(f'phase-{phase}.log' if phase!='final' else name.rsplit('.',1)[-1]+('-'+args[0] if args else '')+'.log')).write_text(result.stdout+result.stderr,encoding='utf-8')
    print(result.stdout+result.stderr);result.check_returncode()
