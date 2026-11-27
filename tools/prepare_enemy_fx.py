"""Task08 subset atlas and provenance, using actual source dimensions. No source resampling."""
from pathlib import Path
import re,json,hashlib,struct,subprocess,shutil
ROOT=Path(__file__).resolve().parents[1];SOURCE=ROOT/'asset_sources/MonstersCreatures_v1.0/MonstersCreatures'
OUT=ROOT/'core/build/reports/task08';OUT.mkdir(parents=True,exist_ok=True)
entries=[]
for name in ['arrow','rock','coin_shot','seed','thorn','spore','poison_spit','fireball','lava_glob','ice_shard','snowball','hex_bolt','shadow_orb','bubble']:
    for direction in ['right','left','up','down']:
        matches=list((SOURCE/'03_Projectiles_FX/Projectiles').glob(name+'_'+direction+'_*.png'));assert len(matches)==1,matches
        entries.append((name+'_'+direction,matches[0],12))
    entries.append((name+'_impact',SOURCE/('03_Projectiles_FX/Projectiles/Impacts/'+name+'_impact_4f.png'),16))
for key,rel,fps in [('bomb_impact','03_Projectiles_FX/Projectiles/Impacts/bomb_impact_4f.png',16),('magic_orb_impact','03_Projectiles_FX/Projectiles/Impacts/magic_orb_impact_4f.png',16),('quake','03_Projectiles_FX/Combat_FX/ground_quake_32x32_5f.png',12.5),('shockwave','03_Projectiles_FX/Combat_FX/shockwave_64x32_6f.png',15),('cast','03_Projectiles_FX/Combat_FX/hit_spark_16x16_4f.png',16)]:entries.append((key,SOURCE/rel,fps))
for name in ['poison','burn','slow']:
    entries.append(('overlay_'+name,SOURCE/('04_Status_Effects/Overlays_32x32/overlay_'+name+'_32x32_4f.png'),8))
    entries.append(('status_'+name,SOURCE/('04_Status_Effects/Icons/status_'+name+'.png'),1))
x=y=2;row=0;clips={};provenance=[];plan=[]
for key,path,fps in entries:
    data=path.read_bytes();sw,h=struct.unpack('>II',data[16:24]);match=re.search(r'_(\d+)f\.png$',path.name);count=int(match[1]) if match else 1
    assert sw%count==0;w=sw//count;rects=[]
    for frame in range(count):
        if x+w+2>510:x=2;y+=row+4;row=0
        assert y+h+2<510,(key,x,y)
        plan.append(f'{path}\t{frame*w}\t{w}\t{h}\t{x}\t{y}');rects.append([x,y,w,h]);x+=w+4;row=max(row,h)
    clips[key]={'fps':fps,'frames':rects};provenance.append({'clip':key,'source':path.relative_to(SOURCE).as_posix(),'sha256':hashlib.sha256(data).hexdigest(),'count':count,'cell':[w,h],'fps':fps})
plan_path=OUT/'enemy-fx-plan.tsv';plan_path.write_text('\n'.join(plan),encoding='utf-8')
subprocess.run(['java','tools/PackEnemyFx.java',str(plan_path),str(ROOT/'assets/game/enemy-fx.png')],cwd=ROOT,check=True)
(ROOT/'assets/game/enemy-fx.json').write_text(json.dumps({'size':[512,512],'primitivePixel':[511,511,1,1],'clips':clips,'provenance':provenance},indent=2),encoding='utf-8')
shutil.copyfile(SOURCE/'LICENSE.txt',ROOT/'assets/game/licenses/MonstersCreatures-Enemy-FX.txt')
print(len(entries),'strips/icons,',sum(p['count'] for p in provenance),'frames, 1 MiB RGBA; selected timing is game tuning, not authored source timing')
