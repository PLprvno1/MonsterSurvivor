// Offline UI/media regression check. Uses installed Edge, Node's built-ins and an
// isolated headless profile. Does not need a web server or modify source audio.
// Run: node tools/check_audio_audition.mjs
import assert from 'node:assert/strict';
import fs from 'node:fs/promises';
import path from 'node:path';
import {fileURLToPath, pathToFileURL} from 'node:url';
import {spawn} from 'node:child_process';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const out = path.join(root, 'core/build/reports/task05_1a_1');
await fs.mkdir(out, {recursive:true});
const profile = await fs.mkdtemp(path.join(out, 'edge-profile-'));
const downloads = await fs.mkdtemp(path.join(out, 'exports-'));
const edge = process.env.AUDITION_EDGE || 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe';
const child = spawn(edge, ['--headless', '--disable-gpu', '--mute-audio', '--no-first-run',
  '--disable-background-networking', '--remote-debugging-port=0', '--user-data-dir=' + profile,
  'about:blank'], {windowsHide:true, stdio:'ignore'});
const wait = ms => new Promise(resolve => setTimeout(resolve, ms));
async function until(probe, message, timeout = 12000) {
  const start = Date.now();
  while (Date.now() - start < timeout) {
    const value = await probe();
    if (value) return value;
    await wait(80);
  }
  throw new Error('Timeout: ' + message);
}
let socket, rpc;
const results = [], exceptions = [], media = [];
try {
  const port = await until(async () => {
    try { return (await fs.readFile(path.join(profile, 'DevToolsActivePort'), 'utf8')).split('\n')[0]; }
    catch { return null; }
  }, 'Edge debug port');
  const targets = await (await fetch(`http://127.0.0.1:${port}/json/list`)).json();
  const target = targets.find(item => item.type === 'page');
  socket = new WebSocket(target.webSocketDebuggerUrl);
  await new Promise((resolve, reject) => {socket.onopen = resolve; socket.onerror = reject;});
  let id = 0;
  const pending = new Map();
  socket.onmessage = ({data}) => {
    const message = JSON.parse(data);
    if (message.id) {
      const callbacks = pending.get(message.id); pending.delete(message.id);
      if (message.error) callbacks.reject(new Error(callbacks.method + ' ' + JSON.stringify(callbacks.params).slice(0,500) + ': ' + JSON.stringify(message.error)));
      else callbacks.resolve(message.result);
    } else if (message.method === 'Runtime.exceptionThrown') exceptions.push(message.params);
  };
  rpc = (method, params = {}) => new Promise((resolve, reject) => {
    const next = ++id; pending.set(next, {resolve, reject, method, params});
    socket.send(JSON.stringify({id:next, method, params}));
  });
  const evaluate = async expression => {
    const result = await rpc('Runtime.evaluate', {expression, returnByValue:true, awaitPromise:true});
    if (result.exceptionDetails) throw new Error(JSON.stringify(result.exceptionDetails));
    return result.result.value;
  };
  const card = event => `[data-event-id="${event}"]`;
  async function click(selector) {
    const point = await evaluate(`(() => {
      const el=document.querySelector(${JSON.stringify(selector)});
      el.scrollIntoView({block:'center'});
      const box=el.getBoundingClientRect(),x=box.x+box.width/2,y=box.y+box.height/2;
      if(document.elementFromPoint(x,y)!==el) throw new Error('Click target obscured: '+${JSON.stringify(selector)});
      return {x,y};
    })()`);
    await rpc('Input.dispatchMouseEvent', {type:'mousePressed', ...point, button:'left', clickCount:1});
    await rpc('Input.dispatchMouseEvent', {type:'mouseReleased', ...point, button:'left', clickCount:1});
  }
  async function browse(event, file) {
    await evaluate(`(() => {const el=document.querySelector(${JSON.stringify(card(event) + ' .file-select')});
      el.value=${JSON.stringify(file)}; el.dispatchEvent(new Event('change')); if (!el.value) throw new Error('File not selectable');})()`);
  }
  async function input(selector, value, type = 'input') {
    await evaluate(`(() => {const el=document.querySelector(${JSON.stringify(selector)}); el.value=${JSON.stringify(value)}; el.dispatchEvent(new Event(${JSON.stringify(type)}));})()`);
  }
  const selection = () => evaluate('exportData()');
  async function reload() {
    await rpc('Page.reload');
    await until(() => evaluate("document.readyState === 'complete' && document.querySelectorAll('.card').length===49"), 'reload');
  }
  async function importJSON(name, data) {
    const file = path.join(out, name);
    await fs.writeFile(file, typeof data === 'string' ? data : JSON.stringify(data), 'utf8');
    const document = await rpc('DOM.getDocument');
    const {nodeId} = await rpc('DOM.querySelector', {nodeId:document.root.nodeId, selector:'#import-file'});
    await evaluate("document.getElementById('notice').textContent='Waiting for import test' ");
    await rpc('DOM.setFileInputFiles', {nodeId, files:[file]});
    await until(() => evaluate("document.getElementById('notice').textContent !== 'Waiting for import test'"), 'import notification');
    return evaluate("document.getElementById('notice').textContent");
  }
  await rpc('Runtime.enable');
  await rpc('Page.enable');
  await rpc('Emulation.setDeviceMetricsOverride', {width:1400,height:1000,deviceScaleFactor:1,mobile:false});
  await rpc('Browser.setDownloadBehavior', {behavior:'allow', downloadPath:downloads});
  await rpc('Page.navigate', {url:pathToFileURL(path.join(root, 'AUDIO_AUDITION.html')).href});
  await until(() => evaluate("document.readyState === 'complete' && document.querySelectorAll('.card').length===49"), '49 cards');
  assert.equal(await evaluate('location.protocol'), 'file:');
  assert.equal((await selection()).events.filter(row => row.status === 'selected').length, 0);
  assert.equal(await evaluate("player.hasAttribute('src')"), false);
  assert.equal(await evaluate("document.querySelectorAll('audio').length"), 1);
  const coverage = await evaluate(`events.map(e=>({id:e.id,kind:kind(e),files:[...document.querySelector('[data-event-id="'+e.id+'"] .file-select').options].map(o=>o.value).filter(Boolean)}))`);
  for (const event of coverage) {
    assert.equal(event.files.length, event.kind === 'BGM' ? 4 : 281, event.id);
    assert.equal(new Set(event.files).size, event.files.length);
    assert(!event.files.some(file => file.includes('Preview.ogg')));
    const suggestions = await evaluate(`byEvent.get(${JSON.stringify(event.id)}).candidates`);
    assert.deepEqual(event.files.slice(0, suggestions.length), suggestions);
  }
  results.push('file:// initial load; all 49 events; full per-role catalogs; missing events covered; suggestions first; none selected/autoplay');

  const hurt = 'PLAYER_HURT', death = 'PLAYER_DEATH';
  const impact = 'kenney_impact-sounds/Audio/footstep_carpet_000.ogg';
  const interfaceFile = 'kenney_interface-sounds/Audio/back_001.ogg';
  const rpg = 'kenney_rpg-audio/Audio/bookClose.ogg';
  const mp3 = 'end_dungeon.mp3';
  await input(card(hurt) + ' input[type=search]', 'footstep_carpet_000');
  assert.equal(await evaluate(`document.querySelector('${card(hurt)} .file-select').options.length`), 2);
  assert.equal(await evaluate(`document.querySelector('${card(hurt)} .file-select').value`), '');
  await input(card(hurt) + ' input[type=search]', 'NO_SUCH_AUDIO_123');
  assert.equal(await evaluate(`document.querySelector('${card(hurt)} .play').disabled`), true);
  await input(card(hurt) + ' input[type=search]', '');
  await input(card(hurt) + ' .pickers select', 'kenney_impact-sounds/Audio', 'change');
  assert.equal(await evaluate(`document.querySelector('${card(hurt)} .file-select').options.length`), 131);
  await input(card(hurt) + ' .pickers select', 'all', 'change');
  results.push('Per-event filename search, zero matches, source filter and no automatic dropdown choice');

  for (const [event, file] of [[hurt, impact], [hurt, interfaceFile], [hurt, rpg], ['BGM_MENU', mp3], ['BGM_MENU', 'battle_music_01-loop.ogg']]) {
    await browse(event, file);
    await click(card(event) + ' .play');
    await until(() => evaluate('player.played.length > 0 && player.currentTime > 0'), 'actual local media playback: ' + file);
    const state = await evaluate(`({file:player.currentSrc,duration:player.duration,time:player.currentTime,error:player.error?.code||null,played:player.played.length,selected:selected.size})`);
    assert.equal(state.error, null);
    assert.equal(state.selected, 0, 'Audition must not approve');
    assert(state.file.startsWith('file:///') && state.duration > 0 && state.time > 0);
    media.push(state);
  }
  assert.equal(await evaluate("document.querySelectorAll('audio').length"), 1);
  await browse(hurt, impact); await click(card(hurt) + ' .play');
  await until(() => evaluate("player.currentSrc.includes('kenney_impact-sounds') && player.currentTime > 0"), 'switch from BGM to SFX');
  await click('#stop');
  assert.equal(await evaluate("player.paused && !player.hasAttribute('src')"), true);
  results.push('Actual media playback: OGG from all 3 packs and MP3 BGM; one shared player; switch interrupts BGM; Stop unloads source');

  await click(card(hurt) + ' .choose');
  await browse(death, impact); await click(card(death) + ' .choose');
  let chosen = await selection();
  assert.equal(chosen.events.filter(row => row.status === 'selected').length, 2);
  assert.equal(chosen.events.find(row => row.event_id === hurt).file_path, 'asset_sources/audio/' + impact);
  await browse(hurt, rpg);
  assert.equal((await selection()).events.find(row => row.event_id === hurt).file_path, 'asset_sources/audio/' + impact);
  await click(card(hurt) + ' .choose');
  await reload();
  assert.equal((await selection()).events.find(row => row.event_id === hurt).file_path, 'asset_sources/audio/' + rpg);
  assert.equal(await evaluate(`document.querySelector('${card(hurt)} .file-select').value`), '');
  await evaluate("document.getElementById('unselected-only').checked=true;document.getElementById('unselected-only').dispatchEvent(new Event('change'))");
  assert.equal(await evaluate("document.querySelectorAll('.card:not([hidden])').length"), 47);
  await input('#event-search', '48');
  assert.equal(await evaluate("document.querySelectorAll('.card:not([hidden])').length"), 0);
  await evaluate("document.getElementById('unselected-only').checked=false;document.getElementById('unselected-only').dispatchEvent(new Event('change'))");
  assert.equal(await evaluate("document.querySelectorAll('.card:not([hidden])').length"), 1);
  await input('#event-search', '');
  results.push('Choose/change/shared file; browsing does not overwrite saved choice; localStorage reload; selected/unselected filter + event search');

  await click('#export');
  const download = path.join(downloads, 'monster-survivor-audio-selections.json');
  const exported = await until(async () => {try{return JSON.parse(await fs.readFile(download,'utf8'));}catch{return null;}}, 'actual JSON download');
  assert.equal(exported.events.length, 49);
  assert.equal(exported.events.filter(row => row.status === 'selected').length, 2);
  const imported = structuredClone(exported);
  imported.events.find(row => row.event_id === hurt).file_path = 'asset_sources/audio/' + interfaceFile;
  imported.events.find(row => row.event_id === death).file_path = null;
  imported.events.find(row => row.event_id === death).status = 'unselected';
  assert((await importJSON('import-valid.json', imported)).includes('49 event'));
  assert.equal((await selection()).events.find(row => row.event_id === hurt).file_path, 'asset_sources/audio/' + interfaceFile);
  assert.equal((await selection()).events.find(row => row.event_id === death).status, 'unselected');
  const preserved = (await selection()).events;
  for (const [name, mutate] of [
    ['traversal', data => {data.events[0] = {event_id:'PLAYER_HURT',status:'selected',file_path:'asset_sources/audio/../../secret.ogg'};}],
    ['wrong-kind', data => {data.events.find(row => row.event_id === hurt).file_path = 'asset_sources/audio/' + mp3;}],
    ['duplicate', data => {data.events[1] = {...data.events[0]};}],
    ['unknown-event', data => {data.events[0].event_id = 'BAD_ID';}],
    ['bad-status', data => {data.events[0].status = 'approved';}],
  ]) {
    const bad = structuredClone(imported); mutate(bad);
    assert((await importJSON('import-bad-' + name + '.json', bad)).startsWith('Không nhập JSON:'));
    assert.deepEqual((await selection()).events, preserved, 'Invalid import must be atomic: ' + name);
  }
  assert((await importJSON('import-malformed.json', '{bad json')).startsWith('Không nhập JSON:'));
  assert.deepEqual((await selection()).events, preserved);
  await importJSON('import-partial.json', {schema_version:2, asset_root:'asset_sources/audio/', events:[{event_id:death, status:'selected', file_path:'asset_sources/audio/' + impact}]});
  assert.equal((await selection()).events.find(row => row.event_id === hurt).file_path, 'asset_sources/audio/' + interfaceFile);
  await importJSON('import-legacy.json', {task:'05.1A',choices:{PLAYER_HURT:{status:'approved',candidate:rpg},BGM_GAME_OVER:{status:'silence',candidate:null}}});
  assert.equal((await selection()).events.find(row => row.event_id === hurt).file_path, 'asset_sources/audio/' + rpg);
  await reload();
  assert.equal((await selection()).events.find(row => row.event_id === hurt).file_path, 'asset_sources/audio/' + rpg);
  results.push('Real 49-row JSON download + file-input import; partial/legacy import; invalid path/kind/status/duplicates/unknown/malformed JSON rejected atomically; imported choices persist');

  await click(card(hurt) + ' .clear');
  assert.equal((await selection()).events.find(row => row.event_id === hurt).status, 'unselected');
  await evaluate(`void Object.defineProperty(window,'localStorage',{configurable:true,value:{setItem(){throw new Error('Blocked storage')},getItem(){throw new Error('Blocked storage')}}})`);
  await browse(hurt, impact); await click(card(hurt) + ' .choose');
  assert((await evaluate("notice.textContent")).includes('không lưu được'));
  assert.equal((await selection()).events.find(row => row.event_id === hurt).status, 'selected');
  assert.equal((await selection()).events.length, 49);
  results.push('Clear selection; blocked storage still allows in-memory selection and JSON export');
  await reload();
  await evaluate(`localStorage.removeItem(STORAGE);localStorage.setItem(LEGACY,JSON.stringify({PLAYER_HURT:{status:'approved',candidate:${JSON.stringify(impact)}}}))`);
  await reload();
  assert.equal((await selection()).events.find(row => row.event_id === hurt).file_path,'asset_sources/audio/' + impact);
  assert.equal((await selection()).events.filter(row => row.status === 'selected').length,1);
  results.push('Existing v1 localStorage approved file migrates; all other events remain unselected');
  assert.equal(exceptions.length, 0, JSON.stringify(exceptions));
  await input('#event-search','48');
  await click(card(hurt) + ' .show-saved');
  await evaluate("say('Kiểm tra offline hoàn tất. Lựa chọn chỉ nằm trong profile kiểm thử.');window.scrollTo(0,0)");
  const image = await rpc('Page.captureScreenshot', {format:'png', captureBeyondViewport:false});
  await fs.writeFile(path.join(out, 'audition-after.png'), Buffer.from(image.data,'base64'));
  const evidence = {passed:true, checked_at:new Date().toISOString(), protocol:'file://',
    browser:await rpc('Browser.getVersion'), no_file_access_or_autoplay_override:true,
    media_output:'muted; actual media playback clock/ranges verified, not human sound approval',
    results, media, exceptions, download};
  await fs.writeFile(path.join(out, 'browser-checks.json'), JSON.stringify(evidence,null,2));
  console.log(JSON.stringify(evidence,null,2));
} catch (error) {
  if (rpc && socket?.readyState === WebSocket.OPEN) {
    try {
      const state = await rpc('Runtime.evaluate', {expression:"JSON.stringify({now:document.getElementById('now').textContent,src:document.getElementById('player').currentSrc,paused:document.getElementById('player').paused,error:document.getElementById('player').error?.code,time:document.getElementById('player').currentTime,duration:document.getElementById('player').duration})",returnByValue:true});
      console.error('Failure state:', state.result.value);
      const screenshot = await rpc('Page.captureScreenshot', {format:'png'});
      await fs.writeFile(path.join(out,'audition-failure.png'),Buffer.from(screenshot.data,'base64'));
    } catch {}
  }
  throw error;
} finally {
  if (rpc && socket?.readyState === WebSocket.OPEN) {
    try { await rpc('Browser.close'); } catch {}
  }
  socket?.close();
  if (child.exitCode === null) child.kill();
}
