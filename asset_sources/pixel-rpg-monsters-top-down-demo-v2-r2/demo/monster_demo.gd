extends Node2D

const HEROES := ["knight_blue", "barbarian_red", "paladin_gold"]
const TITLE := "Pixel RPG Monsters Top-Down Free Demo"

var data: Dictionary = {}
var size := Vector2i.ZERO
var solid: Array = []
var world: Node2D
var ground: Sprite2D
var ground_tex: Array = []
var ground_t := 0.0
var cam: Camera2D
var label: Label
var hint: Label
var hpbar: ColorRect
var astar := AStarGrid2D.new()
var rng := RandomNumberGenerator.new()
var hero: Dictionary = {}
var hero_i := 0
var mobs: Array = []
var autoplay := false
var idle_t := 0.0
var route_i := 0
var path: Array = []
var kills := 0


func _ready() -> void:
	rng.seed = 21
	autoplay = "--autoplay" in OS.get_cmdline_user_args()
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
	astar.region = Rect2i(0, 0, size.x, size.y)
	astar.cell_size = Vector2(16, 16)
	astar.diagonal_mode = AStarGrid2D.DIAGONAL_MODE_NEVER
	astar.update()
	for y in size.y:
		var row: String = solid[y]
		for x in size.x:
			if row[x] == "1":
				astar.set_point_solid(Vector2i(x, y), true)
	var sp: Array = data["spawn"]
	hero = _make("res://demo/heroes/" + HEROES[0] + ".tres", 32, 28, _center(Vector2i(int(sp[0]), int(sp[1]))))
	hero["hp"] = 30
	hero["max"] = 30
	hero["atk"] = "attack1"
	hero["is_hero"] = true
	hero["dead_t"] = 0.0
	cam = Camera2D.new()
	hero["node"].add_child(cam)
	cam.limit_left = 0
	cam.limit_top = 0
	cam.limit_right = size.x * 16
	cam.limit_bottom = size.y * 16
	cam.position_smoothing_enabled = true
	cam.make_current()
	for s in data["monsters"]:
		var m := _make(str(s["tres"]), int(s["w"]), int(s["feet"]), _center(Vector2i(int(s["x"]), int(s["y"]))))
		m["boss"] = bool(s["boss"])
		m["hp"] = 8 if m["boss"] else 3
		m["max"] = m["hp"]
		m["home"] = m["pos"]
		m["area"] = Rect2i(int(s["home"][0]), int(s["home"][1]), int(s["home"][2]), int(s["home"][3]))
		m["name"] = str(s["name"])
		m["wait"] = rng.randf_range(0.2, 2.5)
		m["path"] = []
		m["atk"] = "attack"
		m["dead_t"] = 0.0
		mobs.append(m)
	_hud()


func _make(tres: String, w: int, feet: int, pos: Vector2) -> Dictionary:
	var a := AnimatedSprite2D.new()
	a.sprite_frames = load(tres)
	a.offset = Vector2(0, w / 2.0 - feet)
	a.position = pos
	world.add_child(a)
	var d := {"node": a, "pos": pos, "dir": "down", "lock": 0.0, "dead": false, "cool": 0.0, "hit_done": true, "w": w}
	_play(d, "idle")
	return d


func _hud() -> void:
	var hud := CanvasLayer.new()
	add_child(hud)
	label = Label.new()
	label.position = Vector2(6, 3)
	label.add_theme_font_size_override("font_size", 10)
	label.add_theme_color_override("font_outline_color", Color.BLACK)
	label.add_theme_constant_override("outline_size", 3)
	hud.add_child(label)
	hint = Label.new()
	hint.position = Vector2(6, 254)
	hint.add_theme_font_size_override("font_size", 8)
	hint.add_theme_color_override("font_outline_color", Color.BLACK)
	hint.add_theme_constant_override("outline_size", 3)
	hint.text = "Arrows/WASD move  J or Space attack  Tab hero  (idle 3 s: autoplay)"
	hud.add_child(hint)
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


func _dir_of(v: Vector2) -> String:
	if absf(v.x) > absf(v.y):
		return "right" if v.x > 0.0 else "left"
	return "down" if v.y > 0.0 else "up"


func _dir_vec(s: String) -> Vector2:
	return {"down": Vector2(0, 1), "up": Vector2(0, -1), "left": Vector2(-1, 0), "right": Vector2(1, 0)}[s]


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


func _len(d: Dictionary, anim: String) -> float:
	var full: String = anim + "_" + str(d["dir"])
	var sp: AnimatedSprite2D = d["node"]
	if not sp.sprite_frames.has_animation(full):
		return 0.3
	return sp.sprite_frames.get_frame_count(full) / sp.sprite_frames.get_animation_speed(full)


func _plan(a: Vector2i, b: Vector2i) -> Array:
	if _is_solid(a) or _is_solid(b):
		return []
	var ids: Array = astar.get_id_path(a, b)
	var out: Array = []
	for i in ids:
		out.append(_center(i))
	if not out.is_empty():
		out.pop_front()
	return out


func _follow(d: Dictionary, p: Array) -> Vector2:
	while not p.is_empty():
		var t: Vector2 = p[0]
		var to: Vector2 = t - d["pos"]
		if to.length() < 1.5:
			p.pop_front()
			continue
		return to.normalized()
	return Vector2.ZERO


func _attack(d: Dictionary) -> void:
	var an: String = d["atk"]
	_play(d, an, _len(d, an))
	d["hit_done"] = false
	d["hit_at"] = _len(d, an) * 0.45


func _reach(d: Dictionary) -> float:
	return 30.0 if int(d["w"]) > 32 else 20.0


func _strike(a: Dictionary, targets: Array) -> void:
	for t in targets:
		if t["dead"]:
			continue
		var to: Vector2 = t["pos"] - a["pos"]
		if to.length() < _reach(a) + _reach(t) * 0.5 and (to.length() < 8.0 or _dir_of(to) == str(a["dir"])):
			_hurt(t, 1)


func _hurt(t: Dictionary, n: int) -> void:
	t["hp"] = int(t["hp"]) - n
	var sp: AnimatedSprite2D = t["node"]
	var tw := create_tween()
	tw.tween_property(sp, "modulate", Color(1.6, 1.6, 1.6), 0.05)
	tw.tween_property(sp, "modulate", Color(1, 1, 1), 0.12)
	if int(t["hp"]) <= 0:
		t["dead"] = true
		_play(t, "death", 99.0)
		t["dead_t"] = 0.0
		if not t.get("is_hero", false):
			kills += 1
	elif str(t.get("state", "")) != str(t["atk"]):
		_play(t, "hurt", _len(t, "hurt"))


func _process(delta: float) -> void:
	ground_t += delta
	ground.texture = ground_tex[int(ground_t * 4.0) % 4]
	_hero(delta)
	for m in mobs:
		_mob(m, delta)
	hero["node"].position = hero["pos"]
	for m in mobs:
		m["node"].position = m["pos"]
	hpbar.size.x = 60.0 * clampf(float(hero["hp"]) / float(hero["max"]), 0.0, 1.0)
	label.text = TITLE + "  -  " + _biome() + "   defeated: " + str(kills)


func _biome() -> String:
	var c := _cell(hero["pos"])
	for k in data["biomes"]:
		var r: Array = data["biomes"][k]
		if Rect2i(int(r[0]), int(r[1]), int(r[2]), int(r[3])).has_point(c):
			return str(data["biome_names"][k])
	return "Village"


func _nearest() -> Dictionary:
	var best := {}
	var bd := 1e9
	for m in mobs:
		if m["dead"]:
			continue
		var d: float = (m["pos"] - hero["pos"]).length()
		if d < bd:
			bd = d
			best = m
	return best


func _hero(delta: float) -> void:
	if hero["dead"]:
		hero["dead_t"] = float(hero["dead_t"]) + delta
		if float(hero["dead_t"]) > 2.0:
			hero["dead"] = false
			hero["hp"] = hero["max"]
			var sp: Array = data["spawn"]
			hero["pos"] = _center(Vector2i(int(sp[0]), int(sp[1])))
			path = []
			_play(hero, "idle")
		return
	if float(hero["lock"]) > 0.0:
		hero["lock"] = float(hero["lock"]) - delta
		if not bool(hero["hit_done"]) and float(hero["lock"]) <= float(hero.get("hit_at", 0.0)):
			hero["hit_done"] = true
			_strike(hero, mobs)
		return
	hero["hp"] = minf(float(hero["max"]), float(hero["hp"]) + delta * 0.8)
	var v := Vector2.ZERO
	var atk := false
	if not autoplay:
		v = Input.get_vector("ui_left", "ui_right", "ui_up", "ui_down")
		if Input.is_key_pressed(KEY_A):
			v.x -= 1.0
		if Input.is_key_pressed(KEY_D):
			v.x += 1.0
		if Input.is_key_pressed(KEY_W):
			v.y -= 1.0
		if Input.is_key_pressed(KEY_S):
			v.y += 1.0
		atk = Input.is_key_pressed(KEY_J) or Input.is_key_pressed(KEY_SPACE)
	if v != Vector2.ZERO or atk:
		idle_t = 0.0
		path = []
	else:
		idle_t += delta
	if v == Vector2.ZERO and not atk and (autoplay or idle_t > 3.0):
		var r := _auto()
		v = r[0]
		atk = r[1]
	if atk:
		_attack(hero)
		return
	if v.length() > 1.0:
		v = v.normalized()
	_move(hero, v * 64.0 * delta)
	if v != Vector2.ZERO:
		hero["dir"] = _dir_of(v)
		_play(hero, "walk")
	else:
		_play(hero, "idle")


func _auto() -> Array:
	var t := _nearest()
	if not t.is_empty():
		var to: Vector2 = t["pos"] - hero["pos"]
		var d := to.length()
		if d < 110.0:
			path = []
			if d < _reach(t) * 0.5 + 18.0:
				hero["dir"] = _dir_of(to)
				return [Vector2.ZERO, true]
			var c := _plan(_cell(hero["pos"]), _cell(t["pos"]))
			if c.is_empty():
				return [to.normalized(), false]
			return [_follow(hero, c), false]
	if path.is_empty():
		var r: Array = data["route"][route_i % data["route"].size()]
		var goal := Vector2i(int(r[0]), int(r[1]))
		if _cell(hero["pos"]) == goal:
			route_i += 1
			return [Vector2.ZERO, false]
		path = _plan(_cell(hero["pos"]), goal)
		if path.is_empty():
			route_i += 1
	return [_follow(hero, path), false]


func _mob(m: Dictionary, delta: float) -> void:
	if m["dead"]:
		m["dead_t"] = float(m["dead_t"]) + delta
		var sp: AnimatedSprite2D = m["node"]
		if float(m["dead_t"]) > 1.6:
			sp.modulate.a = maxf(0.0, 1.0 - (float(m["dead_t"]) - 1.6) * 2.0)
		if float(m["dead_t"]) > 7.0:
			m["dead"] = false
			m["hp"] = m["max"]
			m["pos"] = m["home"]
			m["path"] = []
			sp.modulate.a = 1.0
			_play(m, "idle")
		return
	if float(m["lock"]) > 0.0:
		m["lock"] = float(m["lock"]) - delta
		if not bool(m["hit_done"]) and float(m["lock"]) <= float(m.get("hit_at", 0.0)):
			m["hit_done"] = true
			if not hero["dead"]:
				_strike(m, [hero])
		return
	m["cool"] = float(m["cool"]) - delta
	var to: Vector2 = hero["pos"] - m["pos"]
	var d := to.length()
	if not hero["dead"] and d < 96.0:
		m["dir"] = _dir_of(to)
		if d < _reach(m) + 6.0:
			if float(m["cool"]) <= 0.0:
				m["cool"] = 1.6 if m["boss"] else 1.3
				_attack(m)
			else:
				_play(m, "idle")
			return
		var c := _plan(_cell(m["pos"]), _cell(hero["pos"]))
		var v := _follow(m, c) if not c.is_empty() else to.normalized()
		_move(m, v * (30.0 if m["boss"] else 38.0) * delta)
		_play(m, "walk")
		return
	var p: Array = m["path"]
	if p.is_empty():
		m["wait"] = float(m["wait"]) - delta
		_play(m, "idle")
		if float(m["wait"]) <= 0.0:
			var area: Rect2i = m["area"]
			var home := _cell(m["home"])
			for k in 20:
				var c := home + Vector2i(rng.randi_range(-4, 4), rng.randi_range(-3, 3))
				if area.has_point(c) and not _is_solid(c):
					m["path"] = _plan(_cell(m["pos"]), c)
					break
			m["wait"] = rng.randf_range(1.0, 3.5)
		return
	var v2 := _follow(m, p)
	if v2 == Vector2.ZERO:
		return
	m["dir"] = _dir_of(v2)
	_move(m, v2 * 24.0 * delta)
	_play(m, "walk")


func _unhandled_input(e: InputEvent) -> void:
	if e is InputEventKey and e.pressed and not e.echo:
		idle_t = 0.0
		if e.keycode == KEY_TAB:
			hero_i = (hero_i + 1) % HEROES.size()
			var sp: AnimatedSprite2D = hero["node"]
			sp.sprite_frames = load("res://demo/heroes/" + HEROES[hero_i] + ".tres")
			_play(hero, "idle", 0.0)
