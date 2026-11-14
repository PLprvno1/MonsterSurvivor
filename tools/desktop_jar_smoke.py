"""Windows check of the packaged Desktop JAR, including its real launcher/resources."""
from pathlib import Path
import ctypes
import json
import subprocess
import time

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'core/build/reports/task01b2'
OUT.mkdir(parents=True,exist_ok=True)
jar=max((ROOT/'lwjgl3/build/libs').glob('*.jar'),key=lambda p:p.stat().st_mtime)
log=OUT/'desktop-jar.log'
with log.open('w',encoding='utf-8') as stream:
    process=subprocess.Popen(['java','-jar',str(jar)],cwd=ROOT,stdout=stream,stderr=subprocess.STDOUT)
    try:
        deadline=time.monotonic()+15
        ready=False
        while time.monotonic()<deadline and process.poll() is None:
            if 'Ready: 100x100 forest' in log.read_text(encoding='utf-8',errors='replace'):
                ready=True;break
            time.sleep(.2)
        assert ready,'Packaged launcher did not load GameScreen; inspect desktop-jar.log'
        time.sleep(3)
        assert process.poll() is None,'Desktop exited unexpectedly'
        user32=ctypes.windll.user32
        user32.FindWindowW.argtypes=[ctypes.c_wchar_p,ctypes.c_wchar_p]
        user32.FindWindowW.restype=ctypes.c_void_p
        user32.GetWindowThreadProcessId.argtypes=[ctypes.c_void_p,ctypes.POINTER(ctypes.c_ulong)]
        user32.PostMessageW.argtypes=[ctypes.c_void_p,ctypes.c_uint,ctypes.c_size_t,ctypes.c_ssize_t]
        window=user32.FindWindowW(None,'MonsterSurvivor')
        pid=ctypes.c_ulong()
        user32.GetWindowThreadProcessId(window,ctypes.byref(pid))
        assert window and pid.value==process.pid,'Cannot identify owned Desktop window'
        user32.PostMessageW(window,0x0010,0,0) # WM_CLOSE, normal libGDX disposal path.
        code=process.wait(timeout=10)
        assert code==0,f'Packaged desktop exit code {code}'
        result={'jar':str(jar.relative_to(ROOT)),'ready':True,'windowClosedNormally':True,'exitCode':code}
        (OUT/'desktop-jar.json').write_text(json.dumps(result,indent=2)+'\n')
        print('PACKAGED DESKTOP JAR PASSED:',result)
    finally:
        if process.poll() is None:process.terminate();process.wait(timeout=10)
