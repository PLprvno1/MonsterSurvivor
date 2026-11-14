"""Opt in to real Android GL terrain/frame verification, then remove the local flag."""
import json,re,time
import android_smoke as a

out=a.ROOT/'core/build/reports/task01b2';out.mkdir(parents=True,exist_ok=True)
try:
    a.adb('shell','run-as',a.PACKAGE,'touch','files/asset-visual-check')
    a.adb('shell','am','force-stop',a.PACKAGE);a.adb('logcat','-c')
    a.adb('shell','am','start','-W','-n',a.ACTIVITY)
    deadline=time.monotonic()+45
    while True:
        text=a.logs()
        match=re.search(r'AssetVisualCheck terrainSamples=(\d+) unexpectedPixels=(\d+) tinyFramesRendered=(\d+) glError=(\d+)',text)
        if match:break
        if 'FATAL EXCEPTION' in text or time.monotonic()>deadline:raise AssertionError(text)
        time.sleep(.5)
    samples,bad,frames,error=map(int,match.groups())
    assert samples==20 and bad==0 and frames>300 and error==0
    result={'terrainSamples':samples,'unexpectedPixels':bad,'tinyFramesRendered':frames,'glError':error,
            'method':'Actual emulator GL, offscreen framebuffer readback; no licensed artwork submitted to AI.'}
    (out/'android-visual.json').write_text(json.dumps(result,indent=2)+'\n')
    (out/'android-visual-log.txt').write_text(text,encoding='utf8')
    print('ANDROID GPU VERIFICATION PASSED',result)
finally:
    a.adb('shell','run-as',a.PACKAGE,'rm','-f','files/asset-visual-check')
