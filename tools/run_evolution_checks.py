"""Focused Task 07 checks. Java compile and GL only; no Android packaging/install."""
from pathlib import Path
import os,subprocess,sys
sys.stdout.reconfigure(encoding='utf-8')
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task07';OUT.mkdir(parents=True,exist_ok=True)
entries=[str(ROOT/'core/build/classes/java/main')];cache=Path.home()/'.gradle/caches/modules-2/files-2.1'
for group in ['com.badlogicgames.gdx','org.lwjgl','com.badlogicgames.jlayer','org.jcraft']:
    parent=cache/group
    if parent.exists():
        for artifact in parent.iterdir():
            versions=sorted(p for p in artifact.iterdir() if p.is_dir())
            if versions:entries.extend(str(p) for p in versions[-1].rglob('*.jar') if not p.name.endswith(('-sources.jar','-javadoc.jar')))
classes=ROOT/'core/build/evolution-check-classes';classes.mkdir(parents=True,exist_ok=True);cp=os.pathsep.join(entries)
names=sys.argv[1:] or ['EvolutionChecks','EvolutionSmoke','DiagnosticIsolationChecks','UpgradeChecks','SkillChecks','Task04A1Checks','WeaponChecks','PickupChecks','BossChecks','AudioChecks','UpgradeSmoke']
sources=[next(ROOT/f'tools/{folder}/{name}.java' for folder in ['checks','smoke'] if (ROOT/f'tools/{folder}/{name}.java').exists()) for name in names]
compiled=subprocess.run(['javac','-encoding','UTF-8','-cp',cp,'-d',str(classes),*map(str,sources)],capture_output=True,text=True,encoding='utf-8')
if compiled.returncode:print(compiled.stderr);sys.exit(compiled.returncode)
cp+=os.pathsep+str(classes);argfile=OUT/'java-args.txt';argfile.write_text('-cp\n"'+cp.replace('\\','/')+'"\n',encoding='utf-8')
for name in names:
    r=subprocess.run(['java','-Dfile.encoding=UTF-8','-Dsun.stdout.encoding=UTF-8','-Dsun.stderr.encoding=UTF-8','-Dms.report=../core/build/reports/task07/','@'+str(argfile),name],cwd=ROOT/'assets',text=True,capture_output=True,encoding='utf-8',errors='replace')
    (OUT/(name+'.log')).write_text(r.stdout+r.stderr,encoding='utf-8');print(r.stdout+r.stderr,flush=True);r.check_returncode()
