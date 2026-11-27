"""Exact Android state reads and one-use, namespaced automated diagnostic launches. No APK/install."""
from pathlib import Path
import json,re,subprocess,uuid

PKG='com.example.monstersurvivor'
ADB=str(Path.home()/'AppData/Local/Android/Sdk/platform-tools/adb.exe')

def retired_root_fixture_runner():
    raise SystemExit('This historical runner writes root fixture files and is retired by Task 06.1. '
                     'Use android_runtime.AutomatedRun and android_upgrade_smoke.py; ordinary launches never load those files.')

def adb(*args, binary=False):
    r=subprocess.run([ADB,*args],capture_output=True,check=True)
    return r.stdout if binary else r.stdout.decode('utf-8',errors='replace')

def safe(path):
    if not re.fullmatch(r'(files|shared_prefs)/[a-zA-Z0-9_./-]+',path) or '..' in path.split('/'):
        raise ValueError('Unsafe app-relative path')
    return path

def read(path):
    path=safe(path)
    # exec-out exit codes cannot distinguish absent files. An explicit marker preserves that distinction.
    result=adb('exec-out','run-as',PKG,'sh','-c',f"if test -f {path}; then printf CODEX_EXISTS:; cat {path}; else printf CODEX_MISSING:; fi",binary=True)
    if result==b'CODEX_MISSING:':return None
    if not result.startswith(b'CODEX_EXISTS:'):raise RuntimeError('Could not read app state: '+repr(result[:120]))
    return result[len(b'CODEX_EXISTS:'):]

def write(path,data):
    path=safe(path);tmp=path+'.codex-write-'+uuid.uuid4().hex
    if isinstance(data,str):data=data.encode('utf-8')
    subprocess.run([ADB,'shell',f"run-as {PKG} sh -c 'cat > {tmp} && mv {tmp} {path}'"],input=data,capture_output=True,check=True)

def restore(snapshot):
    for path,data in snapshot.items():
        if read(path)==data:continue
        if data is None:adb('shell','run-as',PKG,'rm','-f',safe(path))
        else:write(path,data)
    if any(read(p)!=v for p,v in snapshot.items()):raise AssertionError('Android state restoration mismatch')

class AutomatedRun:
    def __init__(self):
        self.id=uuid.uuid4().hex;self.token=uuid.uuid4().hex;self.root='files/diagnostic-runs/'+self.id
        adb('shell','run-as',PKG,'mkdir','-p',self.root)
    def write(self,name,data):
        if not re.fullmatch(r'[a-zA-Z0-9_.-]+',name):raise ValueError(name)
        write(self.root+'/'+name,data)
    def launch(self,difficulty='EASY',mode='NORMAL'):
        # This request is deliberately one-use and expires before a stale Intent can authorize another run.
        now=int(adb('shell','date','+%s').strip())*1000
        self.write('lease.json',json.dumps({'owner':'codex-automated-tests','token':self.token,'createdMs':now}))
        return adb('shell','am','start','-W','-n',PKG+'/.android.DiagnosticLauncher','--es','diagnostic_run',self.id,
                   '--es','diagnostic_token',self.token,'--es','EXTRA_DIFFICULTY',difficulty,'--es','EXTRA_GAME_MODE',mode)
    def cleanup(self):
        # Only this freshly generated UUID directory is owned by this runner; legacy/user fixtures are untouched.
        names=adb('shell','run-as',PKG,'ls','-1',self.root).splitlines()
        for name in names:
            if not re.fullmatch(r'[a-zA-Z0-9_.-]+',name):raise AssertionError('Unexpected run output name')
            adb('shell','run-as',PKG,'rm','-f',self.root+'/'+name)
        adb('shell','run-as',PKG,'rmdir',self.root)
