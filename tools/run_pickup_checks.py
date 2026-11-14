"""Scoped Task 03A logic + real GL checks. Cached dependencies, no packaging/install tasks."""
from pathlib import Path
import os,subprocess
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'core/build/reports/task03a';OUT.mkdir(parents=True,exist_ok=True)
entries=[str(ROOT/'core/build/classes/java/main')]
cache=Path.home()/'.gradle/caches/modules-2/files-2.1'
for group in ['com.badlogicgames.gdx','org.lwjgl','com.badlogicgames.jlayer','org.jcraft']:
    parent=cache/group
    if parent.exists():
        for artifact in parent.iterdir():
            versions=sorted(p for p in artifact.iterdir() if p.is_dir())
            if versions:entries.extend(str(p) for p in versions[-1].rglob('*.jar') if not p.name.endswith(('-sources.jar','-javadoc.jar')))
classes=ROOT/'core/build/task03a-check-classes';classes.mkdir(parents=True,exist_ok=True)
cp=os.pathsep.join(entries)
sources=[ROOT/p for p in ['tools/checks/PickupChecks.java','tools/checks/Task021Checks.java','tools/checks/PickupAssetCheck.java','tools/checks/PickupRoute.java','tools/smoke/PickupSmoke.java']]
subprocess.run(['javac','-encoding','UTF-8','-cp',cp,'-d',str(classes),*map(str,sources)],check=True)
cp+=os.pathsep+str(classes)
argfile=OUT/'java-args.txt';argfile.write_text('-cp\n"'+cp.replace('\\','/')+'"\n',encoding='utf-8')
for name in ['PickupAssetCheck','PickupRoute']:
    result=subprocess.run(['java','@'+str(argfile),name],cwd=ROOT,text=True,capture_output=True,encoding='utf-8',errors='replace')
    (OUT/(name+'.log')).write_text(result.stdout+result.stderr,encoding='utf-8');print(result.stdout+result.stderr);result.check_returncode()
for name,args in [('PickupChecks',[]),('Task021Checks',['a']),('PickupSmoke',[])]:
    result=subprocess.run(['java','@'+str(argfile),name,*args],cwd=ROOT/'assets',text=True,capture_output=True,encoding='utf-8',errors='replace')
    (OUT/(name+'.log')).write_text(result.stdout+result.stderr,encoding='utf-8')
    print(result.stdout+result.stderr);result.check_returncode()
