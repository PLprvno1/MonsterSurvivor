extends Node2D

const WAVES := [[{"name": "fire_elemental", "tres": "res://willosuna_rpg_monsters/godot/spriteframes/elementals/fire_elemental.tres", "w": 32, "feet": 28, "hp": 3, "el": "fire", "ranged": true}, {"name": "lesser_demon", "tres": "res://willosuna_rpg_monsters/godot/spriteframes/demons/lesser_demon.tres", "w": 32, "feet": 28, "hp": 3, "el": "hellfire", "ranged": false}, {"name": "wild_boar", "tres": "res://willosuna_rpg_monsters/godot/spriteframes/meadow/wild_boar_full.tres", "w": 32, "feet": 28, "hp": 3, "el": "nature", "ranged": false}, {"name": "honey_slime", "tres": "res://willosuna_rpg_monsters/godot/spriteframes/meadow/honey_slime_full.tres", "w": 32, "feet": 28, "hp": 3, "el": "nature", "ranged": true}]]
const BOSS := {"name": "lich_lord", "tres": "res://willosuna_rpg_monsters/godot/spriteframes/bosses/lich_lord.tres", "w": 64, "feet": 56, "hp": 16, "el": "shadow", "ranged": true, "phase2": "res://willosuna_rpg_monsters/godot/spriteframes/bosses/lich_lord_phase2.tres", "area": "shadow"}
const HERO_TRES := "res://demo_v2/heroes/samurai_red.tres"
const FX := "res://willosuna_rpg_monsters/godot/spriteframes/fx/"
const NUM := "res://willosuna_rpg_monsters/x1/fx/damage_numbers/"

var data: Dictionary = {}
var size := Vector2i.ZERO
var solid: Array = []
var world: Node2D
var ground: Sprite2D
var ground_tex: Array = []
var t := 0.0
var cam: Camera2D
var label: Label
var hpbar: ColorRect
var bossbar: ColorRect
var bossback: ColorRect
var hero: Dictionary = {}
var mobs: Array = []
var shots: Array = []
var fx_proj: SpriteFrames
var fx_imp: SpriteFrames
var fx_area: SpriteFrames
var fx_tel: SpriteFrames
var glyphs: Dictionary = {}
var showcase := false
var wave_i := 0
var wave_t := 0.0
var boss_on := false
var kills := 0
var hits := 0
var rng := RandomNumberGenerator.new()
var hero_t := 0.0
var camf := Vector2.ZERO


func _ready() -> void:
	rng.seed = 7
	showcase = "--showcase" in OS.get_cmdline_user_args()
	RenderingServer.set_default_clear_color(Color(0.055, 0.047, 0.07))
	var f := FileAccess.open("res://demo/map/map.json", FileAccess.READ)
	data = JSON.parse_string(f.get_as_text())
	size = Vector2i(int(data["size"][0]), int(data["size"][1]))
	solid = data["solid"]
	for k in 4:
		ground_tex.append(load("res://demo/map/ground_%d.png" % k))
	ground = Sprite2D.new()
	ground.texture = ground_tex[0]
	ground.centered = false
	ground.z_index = -10
	add_child(ground)
	world = Node2D.new()
	world.y_sort_enabled = true
	add_child(world)
	for r in data["rows"]:
		var s := Sprite2D.new()
		s.texture = load("res://demo/map/" + str(r["file"]))
		s.centered = false
		s.offset = Vector2(0, -float(r["h"]))
		s.position = Vector2(float(r["x"]), float(r["y"]))
		world.add_child(s)
	var over := Sprite2D.new()
	over.texture = load("res://demo/map/over.png")
	over.centered = false
	over.z_index = 5
	add_child(over)
	fx_proj = load(FX + "projectiles.tres")
	fx_imp = load(FX + "impacts.tres")
	fx_area = load(FX + "area.tres")
	fx_tel = load(FX + "telegraphs.tres")
	for c in ["white", "yellow", "red", "green"]:
		var g := {}
		for ch in ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"]:
			g[ch] = load(NUM + c + "/" + ch + ".png")
		g["crit"] = load(NUM + c + "/crit.png")
		g["miss"] = load(NUM + c + "/miss.png")
		glyphs[c] = g
	var sp: Array = data["spawn"]
	hero = _make(HERO_TRES, 32, 28, _center(Vector2i(int(sp[0]), int(sp[1]))))
	hero["hp"] = 60.0
	hero["max"] = 60.0
	hero["is_hero"] = true
	cam = Camera2D.new()
	add_child(cam)
	camf = hero["pos"]
	cam.position = camf.round()
	cam.limit_left = 0
	cam.limit_top = 0
	cam.limit_right = size.x * 16
	cam.limit_bottom = size.y * 16
	cam.make_current()
	_hud()
	_wave()


func _hud() -> void:
	var hud := CanvasLayer.new()
	add_child(hud)
	label = Label.new()
	label.position = Vector2(6, 3)
	label.add_theme_font_size_override("font_size", 10)
	label.add_theme_color_override("font_outline_color", Color.BLACK)
	label.add_theme_constant_override("outline_size", 3)
	hud.add_child(label)
	var back := ColorRect.new()
	back.color = Color(0, 0, 0, 0.6)
	back.position = Vector2(6, 18)
	back.size = Vector2(62, 6)
	hud.add_child(back)
	hpbar = ColorRect.new()
	hpbar.color = Color(0.85, 0.2, 0.2)
	hpbar.position = Vector2(7, 19)
	hpbar.size = Vector2(60, 4)
	hud.add_child(hpbar)
	bossback = ColorRect.new()
	bossback.color = Color(0, 0, 0, 0.6)
	bossback.position = Vector2(140, 252)
	bossback.size = Vector2(200, 8)
	bossback.visible = false
	hud.add_child(bossback)
	bossbar = ColorRect.new()
	bossbar.color = Color(0.6, 0.2, 0.9)
	bossbar.position = Vector2(141, 253)
	bossbar.size = Vector2(198, 6)
	bossbar.visible = false
	hud.add_child(bossbar)


func _center(c: Vector2i) -> Vector2:
	return Vector2(c.x * 16 + 8, c.y * 16 + 12)


func _cell(p: Vector2) -> Vector2i:
	return Vector2i(int(floor(p.x / 16.0)), int(floor(p.y / 16.0)))


func _is_solid(c: Vector2i) -> bool:
	if c.x < 0 or c.y < 0 or c.x >= size.x or c.y >= size.y:
		return true
	var row: String = solid[c.y]
	return row[c.x] == "1"


func _blocked(p: Vector2) -> bool:
	for o in [Vector2(-4, -3), Vector2(4, -3), Vector2(-4, 2), Vector2(4, 2)]:
		if _is_solid(_cell(p + o)):
			return true
	return false


func _move(d: Dictionary, v: Vector2) -> void:
	var p: Vector2 = d["pos"]
	if not _blocked(p + Vector2(v.x, 0)):
		p.x += v.x
	if not _blocked(p + Vector2(0, v.y)):
		p.y += v.y
	d["pos"] = p
	d["node"].position = p.round()


func _free_spot(around: Vector2, dist: float) -> Vector2:
	for k in 40:
		var a := rng.randf() * TAU
		var p := around + Vector2(cos(a), sin(a) * 0.8) * dist
		if not _blocked(p):
			return p
	return around


func _make(tres: String, w: int, feet: int, pos: Vector2) -> Dictionary:
	var a := AnimatedSprite2D.new()
	a.sprite_frames = load(tres)
	a.offset = Vector2(0, w / 2.0 - feet)
	a.position = pos.round()
	world.add_child(a)
	var d := {"node": a, "pos": pos, "dir": "down", "lock": 0.0, "state": "", "dead": false, "w": w, "cool": 0.0, "is_hero": false}
	_play(d, "idle")
	return d


func _dir_of(v: Vector2) -> String:
	if absf(v.x) > absf(v.y):
		return "right" if v.x > 0.0 else "left"
	return "down" if v.y > 0.0 else "up"


func _dir_vec(s: String) -> Vector2:
	return {"down": Vector2(0, 1), "up": Vector2(0, -1), "left": Vector2(-1, 0), "right": Vector2(1, 0)}[s]


func _has(d: Dictionary, anim: String) -> bool:
	var sp: AnimatedSprite2D = d["node"]
	return sp.sprite_frames.has_animation(anim + "_" + str(d["dir"]))


func _len(d: Dictionary, anim: String) -> float:
	var full: String = anim + "_" + str(d["dir"])
	var sp: AnimatedSprite2D = d["node"]
	if not sp.sprite_frames.has_animation(full):
		return 0.3
	return sp.sprite_frames.get_frame_count(full) / sp.sprite_frames.get_animation_speed(full)


func _play(d: Dictionary, anim: String, lock: float = 0.0) -> void:
	var full: String = anim + "_" + str(d["dir"])
	var sp: AnimatedSprite2D = d["node"]
	if not sp.sprite_frames.has_animation(full):
		return
	if sp.animation != full or lock > 0.0:
		sp.play(full)
		sp.frame = 0
	d["lock"] = lock
	d["state"] = anim


func _fx(frames: SpriteFrames, anim: String, pos: Vector2, z: int = 0) -> AnimatedSprite2D:
	var a := AnimatedSprite2D.new()
	a.sprite_frames = frames
	a.position = pos.round()
	a.z_index = z
	add_child(a)
	a.play(anim)
	a.animation_finished.connect(a.queue_free)
	return a


func _number(value: String, color: String, pos: Vector2) -> void:
	var n := Node2D.new()
	n.position = (pos + Vector2(rng.randi_range(-4, 4), -26)).round()
	n.z_index = 20
	add_child(n)
	var g: Dictionary = glyphs[color]
	var x := 0
	if value == "crit" or value == "miss":
		var s := Sprite2D.new()
		s.texture = g[value]
		s.centered = false
		n.add_child(s)
		x = s.texture.get_width()
	else:
		for ch in value:
			var s := Sprite2D.new()
			s.texture = g[ch]
			s.centered = false
			s.position = Vector2(x, 0)
			n.add_child(s)
			x += s.texture.get_width() - 1
	for c in n.get_children():
		var s2 := c as Node2D
		s2.position.x -= floor(x / 2.0)
	var tw := create_tween()
	tw.tween_property(n, "position", n.position + Vector2(0, -12), 0.5).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
	tw.tween_interval(0.25)
	tw.tween_callback(n.queue_free)


func _wave() -> void:
	if wave_i >= WAVES.size():
		wave_i = 0
	var w: Array = WAVES[wave_i]
	wave_i += 1
	var i := 0
	for m in w:
		var p := _free_spot(hero["pos"], 70.0 + 14.0 * i)
		_spawn(m, p, false)
		i += 1


func _spawn(m: Dictionary, p: Vector2, boss: bool) -> void:
	var d := _make(str(m["tres"]), int(m["w"]), int(m["feet"]), p)
	d["name"] = str(m["name"])
	d["boss"] = boss
	d["hp"] = float(m["hp"])
	d["max"] = float(m["hp"])
	d["el"] = str(m["el"])
	d["ranged"] = bool(m.get("ranged", false))
	d["cool"] = rng.randf_range(0.4, 1.2)
	d["dir"] = _dir_of(hero["pos"] - p)
	d["phase"] = 1
	d["p2"] = str(m.get("phase2", ""))
	d["area"] = str(m.get("area", ""))
	var intro := "entrance" if boss else "spawn"
	_play(d, intro, _len(d, intro))
	mobs.append(d)


func _process(delta: float) -> void:
	t += delta
	ground.texture = ground_tex[int(t * 4.0) % 4]
	_hero(delta)
	for m in mobs.duplicate():
		_mob(m, delta)
	_shots(delta)
	if not showcase:
		camf = camf.lerp(hero["pos"], 1.0 - exp(-5.0 * delta))
	cam.position = camf.round()
	hpbar.size.x = 60.0 * clampf(float(hero["hp"]) / float(hero["max"]), 0.0, 1.0)
	label.text = "Pixel RPG Monsters Top-Down Free Demo v2  -  defeated: " + str(kills)
	var alive := 0
	for m in mobs:
		if not m["dead"]:
			alive += 1
	wave_t += delta
	var boss_time := 2.6 if showcase else 30.0
	if not boss_on and (t > boss_time or (kills >= 6 and alive == 0)):
		boss_on = true
		_spawn(BOSS, _free_spot(hero["pos"], 64.0), true)
	elif alive == 0 and wave_t > 1.0:
		wave_t = 0.0
		_wave()
		if boss_on and kills > 10:
			boss_on = false


func _nearest() -> Dictionary:
	var best := {}
	var bd := 1e9
	for m in mobs:
		if m["dead"] or str(m["state"]) in ["spawn", "entrance"]:
			continue
		var d: float = (m["pos"] - hero["pos"]).length()
		if d < bd:
			bd = d
			best = m
	return best


func _hero(delta: float) -> void:
	if float(hero["lock"]) > 0.0:
		hero["lock"] = float(hero["lock"]) - delta
		if hero["state"] == "attack1" and not hero.get("hit_done", true) and float(hero["lock"]) < 0.22:
			hero["hit_done"] = true
			_hero_strike()
		if hero["state"] == "dodge":
			_move(hero, -_dir_vec(hero["dir"]) * 60.0 * delta)
		return
	hero["hp"] = minf(float(hero["max"]), float(hero["hp"]) + delta * 2.0)
	hero_t -= delta
	var v := Vector2.ZERO
	var manual := Input.get_vector("ui_left", "ui_right", "ui_up", "ui_down")
	if manual != Vector2.ZERO and not showcase:
		v = manual
	else:
		var m: Dictionary = hero.get("target", {})
		if m.is_empty() or m["dead"] or not mobs.has(m):
			m = _nearest()
			hero["target"] = m
		if not m.is_empty():
			var to: Vector2 = m["pos"] - hero["pos"]
			var reach := 22.0 if int(m["w"]) <= 32 else 30.0
			if to.length() < reach:
				hero["dir"] = _dir_of(to)
				hero["hit_done"] = false
				_play(hero, "attack1", _len(hero, "attack1"))
				return
			v = to.normalized()
	if Input.is_key_pressed(KEY_J) or Input.is_key_pressed(KEY_SPACE):
		hero["hit_done"] = false
		_play(hero, "attack1", _len(hero, "attack1"))
		return
	if v != Vector2.ZERO:
		hero["dir"] = _dir_of(v)
		_move(hero, v.normalized() * 70.0 * delta)
		_play(hero, "run")
	else:
		_play(hero, "idle")


func _hero_strike() -> void:
	for m in mobs:
		if m["dead"]:
			continue
		var to: Vector2 = m["pos"] - hero["pos"]
		var reach := 26.0 if int(m["w"]) <= 32 else 36.0
		if to.length() < reach:
			hits += 1
			var crit := hits % 4 == 0
			var dmg := 3 if crit else rng.randi_range(1, 2)
			if crit:
				_number("crit", "yellow", m["pos"] + Vector2(0, -8))
			_number(str(dmg * 7), "yellow" if crit else "white", m["pos"])
			_fx(fx_imp, "fire" if crit else "arcane", m["pos"] + Vector2(0, -10), 6)
			_damage(m, float(dmg), crit)


func _damage(m: Dictionary, n: float, crit: bool) -> void:
	m["hp"] = float(m["hp"]) - n
	var sp: AnimatedSprite2D = m["node"]
	if float(m["hp"]) <= 0.0:
		m["dead"] = true
		kills += 1
		var an := "dissolve" if (kills % 2 == 0 or m["boss"]) else "death"
		_play(m, an, 99.0)
		m["dead_t"] = 0.0
		return
	if m["boss"] and int(m["phase"]) == 1 and float(m["hp"]) <= float(m["max"]) * 0.5 and m["p2"] != "":
		m["phase"] = 2
		sp.sprite_frames = load(str(m["p2"]))
		_play(m, "phase_shift", _len(m, "phase_shift"))
		return
	if crit and _has(m, "stun"):
		_play(m, "stun", 1.1)
	elif not m["boss"] or str(m["state"]) == "idle":
		_play(m, "hurt", _len(m, "hurt"))


func _mob(m: Dictionary, delta: float) -> void:
	var sp: AnimatedSprite2D = m["node"]
	if m["dead"]:
		m["dead_t"] = float(m["dead_t"]) + delta
		if float(m["dead_t"]) > 1.6:
			sp.modulate.a = maxf(0.0, 1.0 - (float(m["dead_t"]) - 1.6) * 2.0)
		if float(m["dead_t"]) > 2.2:
			sp.queue_free()
			mobs.erase(m)
			if m["boss"]:
				bossbar.visible = false
				bossback.visible = false
		return
	if m["boss"]:
		bossbar.visible = true
		bossback.visible = true
		bossbar.size.x = 198.0 * clampf(float(m["hp"]) / float(m["max"]), 0.0, 1.0)
		bossbar.color = Color(0.6, 0.2, 0.9) if int(m["phase"]) == 1 else Color(0.95, 0.3, 0.15)
	if float(m["lock"]) > 0.0:
		m["lock"] = float(m["lock"]) - delta
		if str(m["state"]) == "run":
			_move(m, _dir_vec(m["dir"]) * 95.0 * delta)
		if m.has("fire_at") and float(m["lock"]) <= float(m["fire_at"]):
			m.erase("fire_at")
			_shoot(m)
		if m.has("slam_at") and float(m["lock"]) <= float(m["slam_at"]):
			m.erase("slam_at")
			_fx(fx_area, str(m["area"]) + "_shockwave", m["pos"], -1)
			if (hero["pos"] - m["pos"]).length() < 56.0:
				_hit_hero(8)
		return
	m["cool"] = float(m["cool"]) - delta
	var to: Vector2 = hero["pos"] - m["pos"]
	var d := to.length()
	m["dir"] = _dir_of(to)
	var reach := 20.0 if int(m["w"]) <= 32 else 30.0
	if float(m["cool"]) <= 0.0:
		if m["boss"] and d < 70.0 and rng.randf() < 0.6:
			m["cool"] = 2.2 if int(m["phase"]) == 1 else 1.5
			var tel := _fx(fx_tel, "circle_96", m["pos"], -2)
			tel.speed_scale = 1.2
			_play(m, "slam", _len(m, "slam"))
			m["slam_at"] = _len(m, "slam") * 0.45
			return
		if (m["ranged"] or m["boss"]) and d > 40.0 and d < 140.0:
			m["cool"] = 1.6 if not m["boss"] else 1.4
			var line := _fx(fx_tel, "line_64_" + str(m["dir"]), m["pos"] + _dir_vec(m["dir"]) * 36.0 + Vector2(0, -6), -2)
			line.speed_scale = 1.6
			_play(m, "special", _len(m, "special"))
			m["fire_at"] = _len(m, "special") * 0.45
			return
		if d < reach + 6.0:
			m["cool"] = 1.2
			_play(m, "attack", _len(m, "attack"))
			if (hero["pos"] - m["pos"]).length() < reach + 8.0:
				_hit_hero(3)
			return
		if d > 50.0 and d < 120.0 and _has(m, "run"):
			m["cool"] = 0.8
			_play(m, "run", 0.45)
			return
	if d > reach:
		_move(m, to.normalized() * (26.0 if m["boss"] else 34.0) * delta)
		_play(m, "walk")
	else:
		_play(m, "idle")


func _hit_hero(n: int) -> void:
	hero["hp"] = maxf(1.0, float(hero["hp"]) - n)
	_number(str(n), "red", hero["pos"])
	var sp: AnimatedSprite2D = hero["node"]
	var tw := create_tween()
	tw.tween_property(sp, "modulate", Color(1.6, 1.6, 1.6), 0.05)
	tw.tween_property(sp, "modulate", Color(1, 1, 1), 0.12)


func _shoot(m: Dictionary) -> void:
	var dir: String = m["dir"]
	var a := AnimatedSprite2D.new()
	a.sprite_frames = fx_proj
	a.play(str(m["el"]) + "_" + dir)
	var start: Vector2 = m["pos"] + _dir_vec(dir) * 10.0 + Vector2(0, -10 if int(m["w"]) <= 32 else -20)
	a.position = start.round()
	a.z_index = 4
	add_child(a)
	shots.append({"node": a, "pos": start, "v": _dir_vec(dir) * 150.0, "el": m["el"], "life": 1.2})


func _shots(delta: float) -> void:
	for s in shots.duplicate():
		s["life"] = float(s["life"]) - delta
		s["pos"] = s["pos"] + s["v"] * delta
		var n: AnimatedSprite2D = s["node"]
		n.position = Vector2(s["pos"]).round()
		var hit: bool = (Vector2(s["pos"]) - (hero["pos"] + Vector2(0, -10))).length() < 10.0
		if hit or float(s["life"]) <= 0.0 or _is_solid(_cell(s["pos"] + Vector2(0, 10))):
			_fx(fx_imp, str(s["el"]), s["pos"], 6)
			if hit:
				_hit_hero(2)
			n.queue_free()
			shots.erase(s)
