"""Pack only the player evolution FX used at runtime; preserve source pixels/frame order."""
from pathlib import Path
import hashlib,json,shutil,struct,subprocess
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'asset_sources/MonstersCreatures_v1.0/MonstersCreatures'
entries=[('meteor','03_Projectiles_FX/Projectiles/fireball_right_4f.png',4,12),
 ('arrow','03_Projectiles_FX/Projectiles/arrow_right_2f.png',2,12),
 ('blast','03_Projectiles_FX/Combat_FX/explosion_32x32_6f.png',6,18.75),
 ('slash','03_Projectiles_FX/Combat_FX/slash_white_32x32_4f.png',4,12),
 ('spark','03_Projectiles_FX/Combat_FX/hit_spark_16x16_4f.png',4,28.5714286)]
# README specifies frame order/count, no mandatory FX frame duration. Timing above is explicit game tuning.
burn=next(SOURCE.rglob('overlay_burn_32x32_4f.png'),None)
if burn:entries.append(('burn',burn.relative_to(SOURCE).as_posix(),4,8))
frames={};provenance=[];plan=[];y=2
for name,rel,count,fps in entries:
    path=SOURCE/rel;sw,h=struct.unpack('>II',path.read_bytes()[16:24]);w=sw//count
    if sw!=w*count:raise ValueError(rel)
    rects=[]
    for i in range(count):
        x=2+i*(w+2);plan.append(f'{path}\t{i*w}\t0\t{w}\t{h}\t{x}\t{y}');rects.append([x,y,w,h])
    frames[name]={'fps':fps,'frames':rects};provenance.append({'name':name,'source':rel,'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'count':count,'fps':fps});y+=h+2
if y>256:raise ValueError('Atlas overflow')
out=ROOT/'assets/game';work=ROOT/'core/build/reports/task07';work.mkdir(parents=True,exist_ok=True)
plan_path=work/'fx-plan.tsv';plan_path.write_text('\n'.join(plan),encoding='utf-8')
subprocess.run(['java','tools/PackEvolutionFx.java',str(plan_path),str(out/'evolution-fx.png')],cwd=ROOT,check=True)
(out/'evolution-fx.json').write_text(json.dumps({'primitivePixel':[255,255,1,1],'clips':frames,'provenance':provenance},ensure_ascii=False,indent=2),encoding='utf-8')
shutil.copyfile(SOURCE/'LICENSE.txt',out/'licenses/MonstersCreatures-Evolution.txt')
print(f'{len(entries)} clips; {sum(e[2] for e in entries)} source frames; 256x256 RGBA = 262144 bytes')
