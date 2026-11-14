"""Paired short CPU simulations of saved pre-task sources and current compiled sources; no GL/FPS claims."""
from pathlib import Path
import os,re,subprocess
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task04a_1'
args=(OUT/'java-args.txt').read_text(encoding='utf-8');classpath=re.search(r'"([^"]+)"',args)[1]
baseline=OUT/'before-classes';baseline.mkdir(exist_ok=True)
sources=list((OUT/'before/core/src/main/java').rglob('*.java'))+[ROOT/'tools/checks/BalanceProbe.java']
argfile=OUT/'baseline-javac-args.txt';argfile.write_text('\n'.join('"'+str(p).replace('\\','/')+'"' for p in sources),encoding='utf-8')
subprocess.run(['javac','-encoding','UTF-8','-cp',classpath,'-d',str(baseline),'@'+str(argfile)],check=True)
for label,classes in [('before',baseline),('after',ROOT/'core/build/task04a_1-check-classes')]:
    if label=='after':subprocess.run(['javac','-cp',classpath,'-d',str(classes),str(ROOT/'tools/checks/BalanceProbe.java')],check=True)
    result=subprocess.run(['java','-Dms.balance.label='+label,'-cp',str(classes)+os.pathsep+classpath,'BalanceProbe'],cwd=ROOT/'assets',capture_output=True,text=True,check=True)
    (OUT/('balance-'+label+'.log')).write_text(result.stdout+result.stderr,encoding='utf-8');print(label,'completed',flush=True)
