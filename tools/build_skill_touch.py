"""Compile a small shell-only Android MotionEvent dex helper. No APK, install or game build."""
from pathlib import Path
import subprocess,sys
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports'/('task07' if '--task07' in sys.argv else 'task061');OUT.mkdir(parents=True,exist_ok=True);SDK=Path.home()/'AppData/Local/Android/Sdk'
classes=OUT/'android-touch-classes';classes.mkdir(parents=True,exist_ok=True)
dex=OUT/'android-touch-dex';dex.mkdir(exist_ok=True);android=SDK/'platforms/android-36/android.jar'
subprocess.run(['javac','--release','8','-encoding','UTF-8','-cp',str(android),'-d',str(classes),str(ROOT/'tools/android/SkillTouch.java')],check=True)
d8=sorted((SDK/'build-tools').glob('*/lib/d8.jar'),key=lambda p:p.parts[-3])[-1]
subprocess.run(['java','-cp',str(d8),'com.android.tools.r8.D8','--lib',str(android),'--min-api','23','--output',str(dex),str(classes/'SkillTouch.class')],check=True)
subprocess.run([str(SDK/'platform-tools/adb.exe'),'push',str(dex/'classes.dex'),'/data/local/tmp/task06-touch.dex'],check=True)
print('Touch-only dex ready. Executes as shell UID via app_process; no app installation or activation shortcut.')
