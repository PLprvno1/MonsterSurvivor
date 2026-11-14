"""Scoped roster/import/spawn checks and relevant regressions. No Android packaging/install."""
from pathlib import Path
import os,subprocess,sys
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task04a';OUT.mkdir(parents=True,exist_ok=True)
entries=[str(ROOT/'core/build/classes/java/main')];cache=Path.home()/'.gradle/caches/modules-2/files-2.1'
for group in ['com.badlogicgames.gdx','org.lwjgl','com.badlogicgames.jlayer','org.jcraft']:
    parent=cache/group
    if parent.exists():
        for artifact in parent.iterdir():
            versions=sorted(p for p in artifact.iterdir() if p.is_dir())
            if versions:entries.extend(str(p) for p in versions[-1].rglob('*.jar') if not p.name.endswith(('-sources.jar','-javadoc.jar')))
classes=ROOT/'core/build/task04a-check-classes';classes.mkdir(parents=True,exist_ok=True);cp=os.pathsep.join(entries)
names=sys.argv[1:] or ['EnemyRosterChecks','EnemyAssetSmoke','EnemyRosterSmoke']
sources=[next(ROOT/f'tools/{folder}/{name}.java' for folder in ['checks','smoke'] if (ROOT/f'tools/{folder}/{name}.java').exists()) for name in names]
subprocess.run(['javac','-encoding','UTF-8','-cp',cp,'-d',str(classes),*map(str,sources)],check=True)
cp+=os.pathsep+str(classes);argfile=OUT/'java-args.txt';argfile.write_text('-cp\n"'+cp.replace('\\','/')+'"\n',encoding='utf-8')
for name in names:
    r=subprocess.run(['java','-Dms.report=../core/build/reports/task04a/','@'+str(argfile),name],cwd=ROOT/'assets',text=True,capture_output=True,encoding='utf-8',errors='replace')
    (OUT/(name+'.log')).write_text(r.stdout+r.stderr,encoding='utf-8');print(r.stdout+r.stderr,flush=True);r.check_returncode()
