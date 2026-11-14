"""Audit source metadata, validate decoded pixels, and pack only runtime enemy frames.
No downloads, no N3XUS processing, no Android packaging. Run from any directory.
"""
from pathlib import Path
import collections,copy,json,math,re,struct,subprocess,sys
ROOT=Path(__file__).resolve().parents[1]
PACK=ROOT/'asset_sources/MonstersCreatures_v1.0/MonstersCreatures'
OUT=ROOT/'core/build/reports/task04a';OUT.mkdir(parents=True,exist_ok=True)
ACTIONS={'idle':(4,8,True),'walk':(4,10,True),'attack':(5,12,False),'hurt':(3,12,False),'death':(6,10,False),'special':(5,12,False)}
DIRS=['down','up','right','left']
def rel(path):return str(path.relative_to(ROOT)).replace('\\','/')
def png(path):
    b=path.read_bytes()[:24]
    if b[:8]!=b'\x89PNG\r\n\x1a\n':raise ValueError(f'Invalid PNG header: {path}')
    return struct.unpack('>II',b[16:24])
def frame(path,x,y,w,h,**extra):
    sw,sh=png(path)
    if min(x,y)<0 or min(w,h)<=0 or x+w>sw or y+h>sh:raise ValueError(f'Frame bounds: {path}: {x,y,w,h}')
    return {'source':rel(path),'rect':[x,y,w,h],**extra}
def mc_definition(name,biome,w,h,special):
    flying=name in {'thorn_sprite','bat_swarm','gargoyle','giant_mosquito','frost_moth','flame_skull','jelly','wraith','dust_devil','bumble_guard'}
    fast=name in {'thorn_sprite','jackalope','bat_swarm','giant_mosquito','mummy_cat','frill_lizard','frost_fox','shadow_cat','ratfolk_thief','bone_hound','ash_hound'}
    tank=name in {'moss_golem','crystal_cube','rock_crab','living_armor','lily_snapper','cyclops','magma_slug','crab_knight'}
    ranged=name in {'flytrap_walker','swamp_hag','goblin_bomber','yeti_cub','mimic_jar'}
    archetype='NIGHT' if biome=='Night' else 'RANGED_CANDIDATE' if ranged else 'TANK' if tank else 'FLYING' if flying else 'FAST_MELEE' if fast else 'CHASER'
    tuning={
        'mushroom_folk':(0,1,2,28,1.2),'thorn_sprite':(40,1,1,40,1),'jackalope':(60,1,2,40,1),
        'owlbear_cub':(90,2,3,28,.8),'flytrap_walker':(150,3,4,23,.7),'moss_golem':(420,6,8,18,.5),
        'bat_swarm':(120,1,2,45,.9),'bone_hound':(240,3,4,42,.7),'crystal_cube':(120,3,4,22,.8),
        'rock_crab':(180,4,5,20,.6),'mimic_jar':(150,3,4,27,.7),'roller_bug':(150,2,3,32,.8),
        'kobold_miner':(120,2,3,30,.9),'living_armor':(420,5,7,23,.5),'gargoyle':(180,3,4,35,.6),
        'bog_lurker':(240,3,4,24,.8),'giant_mosquito':(240,1,2,45,.7),'swamp_hag':(285,4,5,23,.6),
        'lily_snapper':(270,4,5,20,.6),'lizardfolk':(260,3,4,30,.8),'antlion':(240,3,4,25,.7),
        'mummy_cat':(180,2,3,42,.8),'dust_devil':(240,2,3,40,.6),'frill_lizard':(210,2,3,42,.8),
        'frost_fox':(300,3,4,44,.7),'yeti_cub':(330,3,4,28,.7),'frost_moth':(360,2,3,36,.6),
        'magma_slug':(420,5,7,20,.6),'flame_skull':(450,3,4,42,.6),'ash_hound':(480,5,6,44,.4),
        'crab_knight':(285,4,5,26,.6),'jelly':(240,2,3,30,.7),'pufferfish':(250,2,3,28,.7),
        'pumpkin_head':(300,4,5,28,.8),'shadow_cat':(300,2,3,45,.8),'wraith':(330,3,4,34,.7),
        'goblin_bomber':(150,2,3,30,.7),'bumble_guard':(60,1,2,42,.9),'ratfolk_thief':(110,2,3,42,.9),'cyclops':(450,6,8,18,.4)}
    unlock,easy,hard,speed,weight=tuning[name]
    return dict(id='mc_'+name,name=name.replace('_',' ').title(),sprite='mc_'+name,biome=biome,archetype=archetype,
        easyHp=easy,hardHp=hard,damage=1,hardDamage=2 if tank and unlock>=420 else 1,speed=speed,
        halfWidth=6 if w==32 else 5,halfHeight=4 if w==32 else 3,flying=flying,unlock=unlock,weight=weight,
        hardWeight=1.2 if tank or fast else 1,nightWeight=3 if biome=='Night' else 1,
        residentLimit=4 if tank else 8,family='mc_'+name,special=special,elite=False,
        scale=1 if h==16 or w==32 else .85,feetX=w/2,feetY=h-2,attackSeconds=5/12,hurtSeconds=3/12,deathSeconds=6/10)
def audit():
    readme=(PACK/'README.txt').read_text(encoding='utf-8');license=(PACK/'LICENSE.txt').read_text(encoding='utf-8')
    if 'Use the assets in unlimited personal and commercial' not in license or 'Modify, recolor, cut' not in license:raise ValueError('Review changed MonstersCreatures license before processing')
    stats={m[0]:m for m in re.findall(r'^  (\w+)\s+(\w+)\s+(\d+)x(\d+)\s+HP\s+(\d+)\s+DMG\s+(\d+)\s+(\w+)\s+hitbox x(\d+) y(\d+) (\d+)x(\d+)\s+special: (\w+)',readme,re.M)}
    if len(stats)!=40:raise ValueError('README stats contract changed; inspect source')
    sprites=[];definitions=[];inventory=[];strip_checks=[]
    for folder in sorted(PACK.glob('01_Monsters_*')):
        for creature in sorted(p for p in folder.iterdir() if p.is_dir()):
            name=creature.name;row=stats[name];biome=row[1];w,h=map(int,row[2:4]);base=creature/f'{name}_{w}x{h}_sheet.png'
            if png(base)!=(w*6,h*24):raise ValueError(f'Actual sheet does not match README grid: {base}')
            for ai,(action,(count,fps,loop)) in enumerate(ACTIONS.items()):
                for di,direction in enumerate(DIRS):
                    strip=creature/'strips'/f'{name}_{action}_{direction}_{count}f.png'
                    if png(strip)!=(w*count,h):raise ValueError(f'Actual strip size mismatch: {strip}')
                    strip_checks.append(dict(sheet=rel(base),strip=rel(strip),x=0,y=(ai*4+di)*h,w=w*count,h=h))
            files=[(base,None)]
            for p in sorted((creature/'recolors').glob('*.png')):
                variant=re.fullmatch(re.escape(name)+r'_(.+)_recolor_'+str(w)+'x'+str(h)+r'_sheet',p.stem)
                if not variant or png(p)!=png(base):raise ValueError(f'Recolor contract changed: {p}')
                files.append((p,variant[1]))
            for source,variant in files:
                d=mc_definition(name,biome,w,h,row[-1]);id=d['id']+('_'+variant if variant else '')
                d.update(id=id,sprite=id,name=d['name']+(' / '+variant if variant else ''))
                if variant:d.update(elite=True,unlock=max(420,d['unlock']+60),easyHp=d['easyHp']+1,hardHp=d['hardHp']+2,weight=d['weight']*.18,residentLimit=2)
                animations=[]
                for ai,(action,(count,fps,loop)) in enumerate(ACTIONS.items()):
                    for di,direction in enumerate(DIRS):
                        animations.append(dict(state=action,direction=direction,fps=fps,loop=loop,mirrorX=False,
                            frames=[frame(source,i*w,(ai*4+di)*h,w,h) for i in range(count)],tryMirror=direction=='left'))
                sprite=dict(id=id,title=d['name'],frameWidth=w,frameHeight=h,feetX=w/2,feetY=h-2,displayScale=d['scale'],animations=animations)
                sprites.append(sprite);definitions.append(d)
                inventory.append(dict(id=id,base=name,variant=variant,biome=biome,status='READY',source=rel(source),w=w,h=h,feet=[w/2,h-2],special=row[-1],sourceStats=dict(hp=int(row[4]),damage=int(row[5]),speedClass=row[6],hitbox=list(map(int,row[7:11]))),auxFiles=[rel(p) for p in creature.rglob('*') if p.is_file()]))
    sc=ROOT/'asset_sources/spritecanvas-monsters-cc0'
    for p in sc.glob('monster-*/LICENSE.txt'):
        if p.read_text(encoding='utf-8')!=(sc/'monster-goblin/LICENSE.txt').read_text(encoding='utf-8'):raise ValueError('Review changed SpriteCanvas license '+str(p))
    for creature in sorted(sc.glob('monster-*')):
        name=creature.name.removeprefix('monster-');id='sc_'+name;animations=[];source_moves=[];w=h=None;px=py=None
        for source in sorted(creature.glob('*.json')):
            meta=json.loads(source.read_text(encoding='utf-8'));m=meta['meta'];move=source.stem.split('-')[-1]
            image=source.with_name(m['image']);cw,ch=m['cell']['w'],m['cell']['h'];pivot=m['pivot_px']
            if png(image)!=(m['size']['w'],m['size']['h']):raise ValueError('SpriteCanvas sheet size mismatch')
            factor=(48 if name=='golem' else 32)/max(cw,ch);sw,sh=math.ceil(cw*factor),math.ceil(ch*factor)
            if w is not None and (w,h,px,py)!=(sw,sh,pivot[0]*factor,pivot[1]*factor):raise ValueError('Inconsistent SpriteCanvas pivots/canvases')
            w,h,px,py=sw,sh,pivot[0]*factor,pivot[1]*factor
            source_frames=list(meta['frames'].values());durations={f['duration'] for f in source_frames}
            if len(durations)!=1 or next(iter(durations))<=0:raise ValueError('Variable timing needs explicit clip support')
            frames=[]
            for f in source_frames:
                if f['rotated'] or f['trimmed']:raise ValueError('Trimmed/rotated SpriteCanvas input needs adapter')
                r=f['frame'];frames.append(frame(image,r['x'],r['y'],r['w'],r['h'],factor=factor))
            for direction in DIRS:animations.append(dict(state=move,direction=direction,fps=1000/next(iter(durations)),loop=m['loop'],mirrorX=direction=='right',frames=copy.deepcopy(frames)))
            source_moves.append(dict(action=move,count=len(frames),cell=[cw,ch],pivot=pivot,fps=m['fps'],durationMs=next(iter(durations)),png=rel(image),json=rel(source)))
        if {m['action'] for m in source_moves}!={'idle','walk','attack','hurt','death'}:raise ValueError('Missing SpriteCanvas base clip')
        sprites.append(dict(id=id,title='SpriteCanvas '+name.title(),frameWidth=w,frameHeight=h,feetX=px,feetY=py,displayScale=.75,animations=animations))
        base=mc_definition('mushroom_folk','Forest',16,32,'none');base.update(id=id,sprite=id,name='SpriteCanvas '+name.title(),biome='CC0',family=id,scale=.75,feetX=px,feetY=py,unlock=420 if name=='golem' else 60 if name=='goblin' else 0,
            easyHp=6 if name=='golem' else 1 if name=='slime' else 2,hardHp=8 if name=='golem' else 2 if name=='slime' else 3,
            speed=18 if name=='golem' else 34 if name=='goblin' else 28,archetype='TANK' if name=='golem' else 'FAST_MELEE' if name=='goblin' else 'CHASER',weight=.5 if name=='golem' else .8,
            halfWidth=6 if name=='golem' else 5,halfHeight=4 if name=='golem' else 3,special='none',residentLimit=4 if name=='golem' else 8)
        for move in source_moves:
            if move['action'] in ['attack','hurt','death']:base[move['action']+'Seconds']=move['count']*move['durationMs']/1000
        definitions.append(base);inventory.append(dict(id=id,base=name,variant=None,biome='CC0',status='ADAPTABLE',source=rel(creature),w=w,h=h,feet=[px,py],special='none',moves=source_moves,adaptation='Nearest normalization; LEFT original, RIGHT mirrored; UP/DOWN reuse LEFT side view.'))
    legacy=json.loads((ROOT/'assets/game/catalog.json').read_text(encoding='utf-8'))
    tune={'tiny_slime':(0,1,1,28,3,False,'CHASER'),'tiny_bat':(0,1,2,52,2.5,True,'FLYING'),'wild_boar':(0,2,3,38,2,False,'FAST_MELEE'),
        'honey_slime':(0,1,2,26,1.4,False,'CHASER'),'giant_bee':(90,1,2,46,.9,True,'FLYING'),'red_cap':(120,2,3,28,.8,False,'CHASER'),
        'red_crab':(240,4,5,22,.6,False,'TANK'),'giant_frog':(240,3,4,32,.7,False,'SPECIAL_CANDIDATE'),
        'rock_golem':(420,6,8,18,.5,False,'TANK'),'living_armor':(420,5,7,23,.5,False,'TANK'),'bone_hound':(240,3,4,48,.7,False,'FAST_MELEE')}
    old_defs=[]
    for s in legacy['sprites']:
        if s['id'] not in tune:continue
        id=s['id'];unlock,easy,hard,speed,weight,flying,arch=tune[id]
        d=dict(id=id,name=s['title'],sprite=id,biome='Tiny' if id.startswith('tiny_') else 'Willosuna',archetype=arch,easyHp=easy,hardHp=hard,damage=1,hardDamage=1,speed=speed,
            halfWidth=5,halfHeight=3,flying=flying,unlock=unlock,weight=weight,hardWeight=1.15 if arch=='TANK' else 1,nightWeight=1,residentLimit=10,family=id,special='legacy_charge' if id=='wild_boar' else 'none',elite=False,
            scale=s.get('displayScale',1),feetX=s['feetX'],feetY=s['feetY'])
        for state in ['attack','hurt','death']:
            a=next(a for a in s['animations'] if a['state']==state and a['direction']=='down');d[state+'Seconds']=len(a['frames'])/a['fps']
        old_defs.append(d)
        inventory.append(dict(id=id,base=id,variant=None,biome=d['biome'],status='READY',source=s['texture'],w=s['frameWidth'],h=s['frameHeight'],feet=[s['feetX'],s['feetY']],special=d['special'],adaptation='Existing runtime pixels/catalog retained; enabled in registry.'))
    definitions=old_defs+definitions
    ids=[d['id'] for d in definitions]
    if len(ids)!=len(set(ids)):raise ValueError('Duplicate definition id')
    balance_path=ROOT/'tools/enemy_balance.json'
    if balance_path.exists():
        saved=json.loads(balance_path.read_text(encoding='utf-8'));overrides={d['id']:d for d in saved['enemies']}
        for d in definitions:
            if d['id'] in overrides:d.update(overrides[d['id']])
    by_id={d['id']:d for d in definitions}
    for s in sprites:
        d=by_id[s['id']];s.update(displayScale=d['scale'],feetX=d['feetX'],feetY=d['feetY'])
    balance={'note':'Gameplay tuning only. Source recommended HP/DMG intentionally scaled down for Monster Survivor. Generated defaults; edits persist on rebuild.','enemies':definitions}
    balance_path.write_text(json.dumps(balance,indent=2)+'\n',encoding='utf-8')
    bosses=[]
    for folder in sorted((PACK/'02_Bosses').iterdir()):
        if not folder.is_dir():continue
        sheet=next(folder.glob('*_sheet.png'));m=re.search(r'(\d+)x(\d+)_sheet',sheet.stem);w,h=map(int,m.groups())
        sheet_w,sheet_h=png(sheet)
        if sheet_w%w or sheet_h!=h*12:raise ValueError(f'Boss grid changed: {sheet}: {sheet_w,sheet_h}')
        clips=[]
        for p in sorted((folder/'strips').glob('*.png')):
            n=int(re.search(r'_(\d+)f$',p.stem)[1]);assert png(p)==(w*n,h)
            clips.append(dict(file=rel(p),frames=n,size=png(p)))
        bosses.append(dict(id=folder.name,source=rel(sheet),size=png(sheet),cell=[w,h],feet=[w/2,h],clips=clips,note='Not imported. Tide Kraken actual death strip is 7f; generic README says 8f.' if folder.name=='tide_kraken' else 'Not imported; boss pool excluded.'))
    fx=[]
    for category in ['03_Projectiles_FX','04_Status_Effects','05_Loot_Drops','06_Combat_UI','07_Bestiary']:
        for p in sorted((PACK/category).rglob('*.png')):fx.append(dict(category=category,file=rel(p),size=png(p)))
    plan=dict(sprites=sprites,definitions=definitions,inventory=inventory,bosses=bosses,fx=fx,stripChecks=strip_checks)
    (OUT/'enemy-import-plan.json').write_text(json.dumps(plan,indent=2),encoding='utf-8')
    return plan
def documentation(plan):
    lines=['# Enemy asset inventory — Task 04A','',
        'Inspected actual source PNG headers, README/license and SpriteCanvas JSON. Pixel decoding/strip comparison results are in `core/build/reports/task04a/asset-validation.json`. No N3XUS artwork processed.','',
        'MonstersCreatures: 40 base species + 27 elite recolors, all included. Six bosses are metadata-only. Existing Tiny/Willosuna: 11 regular species retained/enabled. SpriteCanvas: four ADAPTABLE species. Total regular definitions: 82.','',
        'MC source layout: 6 columns / 24 rows. IDLE 4f @8 fps loop; WALK 4f @10 fps loop; ATTACK 5f @12 fps once; HURT 3f @12 fps once; DEATH 6f @10 fps once; SPECIAL 5f @12 fps once. Directions DOWN/UP/RIGHT/LEFT. README suggests idle/walk 8–10 and attack 12; remaining playback rates are explicit importer policy, not claimed source metadata. Source attack hit frame 2 is documented for future skills, not wired as a runtime damage event; contact damage behavior is documented separately.','',
        'MC frames remain native pixels. LEFT uses a verified mirrored RIGHT only where every source pixel agrees. Feet = (W/2,H-2), fixed for all clips. Recolors use the base layout and are validated individually. SPECIAL clips are imported for later behavior; no corresponding skill is claimed complete.','',
        '| ID | Biome | Status | Source | Cell/runtime canvas | Feet (top-left) | Special / adaptation |','| --- | --- | --- | --- | --- | --- | --- |']
    for i in plan['inventory']:lines.append(f"| {i['id']} | {i['biome']} | {i['status']} | `{i['source']}` | {i['w']}x{i['h']} | {i['feet']} | {i.get('special','')} {i.get('adaptation','')} |")
    lines+=['','## Source files and per-creature details','']
    for i in plan['inventory']:
        if i['id'].startswith('mc_') and not i['variant']:
            lines += [f"### {i['id']}",'',f"Source suggested stats (not runtime balance): `{i['sourceStats']}`. Actual auxiliary files: {len(i['auxFiles'])}.",'',*['- `'+f+'`' for f in i['auxFiles']],'']
        if 'moves' in i:
            lines += [f"### {i['id']}",'', '| Move | Frames | Source cell | Source pivot | Declared fps | Frame duration ms |','| --- | --- | --- | --- | --- | --- |']
            for m in i['moves']:lines.append(f"| {m['action']} | {m['count']} | {m['cell']} | {m['pivot']} | {m['fps']} | {m['durationMs']} |")
            lines+=['','SpriteCanvas runtime uses uniform Nearest normalization to a 32px longest edge (48px Golem), with the same transform for pivot and all animation canvases. Source files are untouched. Playback uses actual JSON duration milliseconds.','']
    lines+=['## Boss metadata (not regular enemies)','']
    for b in plan['bosses']:
        lines += [f"### {b['id']}",'',f"`{b['source']}`, actual sheet {b['size']}, cell {b['cell']}, bottom-center pivot {b['feet']}. {b['note']}",'',*['- `'+c['file']+'`: '+str(c['frames'])+' frames' for c in b['clips']],'']
    lines+=['Lich Lord remains the designated main boss from the existing catalog/GDD. No boss is registered in EnemyRegistry or natural spawn.','', '## License and exclusions','',
        'MonstersCreatures LICENSE allows personal/commercial projects and modification/cutting, prohibits standalone asset redistribution and AI-training datasets. This task uses the art only inside the game, not as training data. SpriteCanvas licenses are CC0. Runtime license copies accompany derived assets. Tiny Questers/Willosuna use existing licensed runtime files unchanged; no restricted artwork is exported for AI inspection. N3XUS is excluded by its previously audited AI restriction.','',
        'READY 78 (67 MC plus 11 existing); ADAPTABLE 4 SpriteCanvas; BLOCKED regular species 0 after successful decode validation. Bosses and FX are intentionally not normal-enemy imports. Recolor variants are counted separately from base species.']
    (ROOT/'ENEMY_ASSET_INVENTORY.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
if __name__=='__main__':
    plan=audit()
    print('Audited',len(plan['inventory']),'regular definitions;',len(plan['sprites']),'new sprites;',len(plan['bosses']),'bosses; no silent omissions.',flush=True)
    # Java performs real PNG decoding, source-strip equality, packing and extrusion validation.
    gdx=next((Path.home()/'.gradle/caches/modules-2/files-2.1/com.badlogicgames.gdx/gdx/1.14.2').rglob('gdx-1.14.2.jar'))
    subprocess.run(['java','-cp',str(gdx),str(ROOT/'tools/PrepareEnemyAtlas.java'),str(OUT/'enemy-import-plan.json'),*sys.argv[1:]],cwd=ROOT,check=True)
    documentation(plan)
    from write_enemy_reports import generate
    generate(plan)
