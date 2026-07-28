package com.polyglue.ide.data.local

import com.polyglue.ide.data.local.entity.SnippetEntity

/**
 * 28 built-in Smart Snippets bawaan PolyGlue, dengan `luaTemplate` kode Lua
 * asli (bukan cuma nama+deskripsi seperti versi hardcoded lama di
 * `SnippetsScreen.kt`). Di-seed sekali ke tabel `snippets` lewat
 * `PolyGlueDatabase.Callback.onCreate()`. Lihat HANDOFF.md §12.
 */
object SnippetSeed {

    private fun s(id: String, category: String, name: String, description: String, icon: String, lua: String) =
        SnippetEntity(
            id = id,
            category = category,
            name = name,
            description = description,
            icon = icon,
            luaTemplate = lua.trimIndent()
        )

    val ALL: List<SnippetEntity> = listOf(
        s("tilemap_painter", "Tilemap", "Tilemap Painter", "Kuas untuk melukis tile pada grid", "grid_on", """
            function onPaintTile(layer, x, y, tileId)
                Tilemap.setTile(layer, x, y, tileId)
            end
        """),
        s("tilemap_auto", "Tilemap", "Auto Tile", "Otomatis pilih sprite berdasarkan tetangga", "auto_awesome", """
            function autoTile(layer, x, y)
                local mask = Tilemap.getNeighborMask(layer, x, y)
                Tilemap.setTile(layer, x, y, AutoTileTable[mask])
            end
        """),
        s("camera_follow", "Camera", "Camera Follow", "Kamera mengikuti objek target", "videocam", """
            function update(dt)
                local pos = Entity.getTransform(target)
                Camera.setPosition(pos.x, pos.y, 0.1)
            end
        """),
        s("camera_shake", "Camera", "Camera Shake", "Efek guncangan saat ledakan", "vibration", """
            function shakeCamera(intensity, duration)
                Camera.shake(intensity, duration)
            end
        """),
        s("anim_play", "Animation", "Play Animation", "Memutar animasi sprite", "movie", """
            function playAnim(entity, clipName, loop)
                Entity.playAnimation(entity, clipName, loop)
            end
        """),
        s("anim_tween", "Animation", "Tween System", "Animasi properti over time", "timeline", """
            function tween(entity, prop, from, to, duration, easing)
                Tween.start(entity, prop, from, to, duration, easing)
            end
        """),
        s("audio_sfx", "Audio", "Play SFX", "Memainkan suara efek", "volume_up", """
            function playSfx(name, volume)
                Audio.play(name, volume or 1.0, false)
            end
        """),
        s("audio_spatial", "Audio", "Spatial Audio", "Suara sesuai jarak & posisi", "surround_sound", """
            function playSpatial(name, x, y)
                Audio.playAt(name, x, y)
            end
        """),
        s("particle_emitter", "Particle", "Particle Emitter", "Ledakan, asap, api, hujan", "grain", """
            function emit(x, y, presetName, count)
                Particles.emit(presetName, x, y, count)
            end
        """),
        s("particle_trail", "Particle", "Trail Renderer", "Jejak di belakang objek", "gesture", """
            function update(dt)
                Particles.updateTrail(self, dt)
            end
        """),
        s("collision_on", "Collision", "On Collision", "Trigger event saat tabrakan", "swap_calls", """
            function onCollision(self, other)
                if other.tag == "Enemy" then
                    Entity.destroy(self)
                end
            end
        """),
        s("collision_raycast", "Collision", "Raycast", "Deteksi objek dalam garis", "linear_scale", """
            function raycast(x1, y1, x2, y2)
                return Physics.raycast(x1, y1, x2, y2)
            end
        """),
        s("ctrl_platformer", "Controller", "Platformer Ctrl", "Lompat, gravitasi, platform", "directions_run", """
            function update(dt)
                local vx = Input.axisX() * speed
                Physics.setVelocityX(self, vx)
                if Input.jumpPressed() and Physics.isGrounded(self) then
                    Physics.applyImpulse(self, 0, jumpForce)
                end
            end
        """),
        s("ctrl_topdown", "Controller", "Top-Down Ctrl", "Gerak 8 arah joystick", "control_camera", """
            function update(dt)
                local dx, dy = Input.joystick()
                Physics.setVelocity(self, dx * speed, dy * speed)
            end
        """),
        s("ai_patrol", "AI", "Patrol AI", "Pola dasar perilaku musuh", "route", """
            function update(dt)
                moveToward(waypoints[currentWaypoint])
                if reachedWaypoint() then
                    currentWaypoint = (currentWaypoint % #waypoints) + 1
                end
            end
        """),
        s("ai_chase", "AI", "Chase AI", "Kejar target dalam range", "directions_run", """
            function update(dt)
                if distanceTo(target) < detectRange then
                    moveToward(target)
                end
            end
        """),
        s("path_astar", "Pathfinding", "A* Pathfinding", "Jalur terpendek hindari rintangan", "alt_route", """
            function findPath(startX, startY, goalX, goalY)
                return Pathfinding.aStar(startX, startY, goalX, goalY)
            end
        """),
        s("rpg_inventory", "RPG", "Inventory", "Tambah, hapus, stack item", "backpack", """
            function addItem(itemId, count)
                Inventory.add(itemId, count or 1)
            end
        """),
        s("rpg_quest", "RPG", "Quest System", "Misi & percakapan bercabang", "menu_book", """
            function startQuest(questId)
                Quest.start(questId)
            end
        """),
        s("ui_healthbar", "UI", "Health Bar", "Bar progres dengan animasi", "favorite", """
            function setHealth(current, max)
                UI.setProgress("healthBar", current / max)
            end
        """),
        s("ui_dialogue", "UI", "Dialogue Box", "Kotak teks efek typewriter", "chat", """
            function showDialogue(text, speaker)
                UI.typewriter("dialogueBox", text, speaker)
            end
        """),
        s("data_save", "Data", "Save Game", "Simpan status ke file", "save", """
            function saveGame(slot)
                SaveSystem.write(slot, gameState)
            end
        """),
        s("mobile_vibrate", "Mobile", "Vibration", "Getar saat dampak", "vibration", """
            function onImpact(force)
                if force > threshold then
                    Device.vibrate(50)
                end
            end
        """),
        s("shader_postfx", "Shader", "Post-Processing", "Bloom, blur, pixelate, CRT", "gradient", """
            function setPostFx(name, enabled)
                Shader.setUniform("u_" .. name, enabled and 1.0 or 0.0)
            end
        """),
        s("ai_gen_dungeon", "AI Tools", "Gen Dungeon", "Peta prosedural otomatis", "dungeon", """
            function generateDungeon(seed, width, height)
                return Procedural.generateDungeon(seed, width, height)
            end
        """),
        s("build_apk", "Build", "Build APK", "Kompilasi final project", "build", """
            -- Trigger dari BuildScreen, bukan dipanggil dari gameplay script.
            -- Lihat BuildRepository.buildAPK() / Export Project (HANDOFF.md §6).
        """),
        s("debug_fps", "Debug", "FPS Monitor", "Overlay statistik real-time", "speed", """
            function update(dt)
                UI.setText("fpsLabel", string.format("FPS: %d", math.floor(1 / dt)))
            end
        """),
        s("productivity_prefab", "Productivity", "Prefab System", "Template objek reusable", "content_copy", """
            function spawnPrefab(name, x, y)
                return Entity.instantiate(name, x, y)
            end
        """)
    )
}
