"""Task 03B.2 focused pickup checks and CC0 comparison; no packaging/install/export."""
from pathlib import Path
import os,subprocess,sys
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task03b_2';OUT.mkdir(parents=True,exist_ok=True)
entries=[str(ROOT/'core/build/classes/java/main')];cache=Path.home()/'.gradle/caches/modules-2/files-2.1'
for group in ['com.badlogicgames.gdx','org.lwjgl','com.badlogicgames.jlayer','org.jcraft']:
    parent=cache/group
    if parent.exists():
        for artifact in parent.iterdir():
            versions=sorted(p for p in artifact.iterdir() if p.is_dir())
            if versions:entries.extend(str(p) for p in versions[-1].rglob('*.jar') if not p.name.endswith(('-sources.jar','-javadoc.jar')))
classes=ROOT/'core/build/task03b2-check-classes';classes.mkdir(parents=True,exist_ok=True);cp=os.pathsep.join(entries)
phase=sys.argv[1] if len(sys.argv)>1 else 'after'
names=['PickupRefinementProbe'] if phase=='before' else ['PickupRefinementProbe','PickupChecks','PickupSmoke']
if len(sys.argv)>2:names=sys.argv[2:]
sources=[next(ROOT/f'tools/{folder}/{name}.java' for folder in ['checks','smoke'] if (ROOT/f'tools/{folder}/{name}.java').exists()) for name in names]
subprocess.run(['javac','-encoding','UTF-8','-cp',cp,'-d',str(classes),*map(str,sources)],check=True)
if phase=='before':
    snapshot=OUT/'PickupRenderer-before.java.txt'
    if not snapshot.exists():raise RuntimeError('Before-renderer snapshot from task start is required.')
    old_source=OUT/'before-src/com/example/monstersurvivor/pickup/PickupRenderer.java';old_source.parent.mkdir(parents=True,exist_ok=True);old_source.write_bytes(snapshot.read_bytes())
    old_classes=ROOT/'core/build/task03b2-before-classes';old_classes.mkdir(parents=True,exist_ok=True)
    subprocess.run(['javac','-encoding','UTF-8','-cp',cp,'-d',str(old_classes),str(old_source)],check=True)
    cp=str(old_classes)+os.pathsep+cp
cp+=os.pathsep+str(classes);argfile=OUT/'java-args.txt';argfile.write_text('-cp\n"'+cp.replace('\\','/')+'"\n',encoding='utf-8')
for name in names:
    args=[phase] if name=='PickupRefinementProbe' else []
    r=subprocess.run(['java','@'+str(argfile),name,*args],cwd=ROOT/'assets',text=True,capture_output=True,encoding='utf-8',errors='replace')
    (OUT/(name+'-'+phase+'.log')).write_text(r.stdout+r.stderr,encoding='utf-8');print(r.stdout+r.stderr,flush=True);r.check_returncode()
