package com.polyglue.ide.core.lua

import android.util.Log
import com.polyglue.ide.core.engine.NativeEngine
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.ZeroArgFunction
import org.luaj.vm2.lib.jse.JsePlatform
import java.io.File

class LuaRuntime(private val engine: NativeEngine) {

    companion object { const val TAG = "LuaRuntime" }

    private lateinit var globals: Globals
    private val loadedScripts = mutableMapOf<String, LuaValue>()
    private val coroutines = mutableListOf<LuaCoroutine>()

    fun initialize() {
        globals = JsePlatform.standardGlobals()
        bindEngineAPI()
        bindSnippetLibrary()
        Log.d(TAG, "Lua runtime initialized")
    }

    private fun bindEngineAPI() {
        globals.set("Entity", LuaValue.tableOf().apply {
            set("create", object : OneArgFunction() {
                override fun call(name: LuaValue): LuaValue {
                    return LuaValue.valueOf(engine.nativeCreateEntity(name.tojstring()).toDouble())
                }
            })
            set("destroy", object : OneArgFunction() {
                override fun call(id: LuaValue): LuaValue {
                    engine.nativeDestroyEntity(id.tolong())
                    return LuaValue.NIL
                }
            })
            set("setPosition", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    engine.nativeSetTransform(arg.get(1).tolong(), arg.get(2).tofloat(), arg.get(3).tofloat(), 0f, 1f, 1f)
                    return LuaValue.NIL
                }
            })
        })

        globals.set("Input", LuaValue.tableOf().apply {
            set("getAxis", object : OneArgFunction() {
                override fun call(axisName: LuaValue): LuaValue {
                    return LuaValue.valueOf(0.0)
                }
            })
            set("getButton", object : OneArgFunction() {
                override fun call(buttonName: LuaValue): LuaValue {
                    return LuaValue.valueOf(false)
                }
            })
        })

        globals.set("Mathf", LuaValue.tableOf().apply {
            set("lerp", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    val a = arg.get(1).todouble()
                    val b = arg.get(2).todouble()
                    val t = arg.get(3).todouble()
                    return LuaValue.valueOf(a + (b - a) * t)
                }
            })
            set("clamp", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    val v = arg.get(1).todouble()
                    val min = arg.get(2).todouble()
                    val max = arg.get(3).todouble()
                    return LuaValue.valueOf(v.coerceIn(min, max))
                }
            })
            set("distance", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    return LuaValue.valueOf(kotlin.math.hypot(
                        arg.get(3).todouble() - arg.get(1).todouble(),
                        arg.get(4).todouble() - arg.get(2).todouble()
                    ))
                }
            })
        })

        globals.set("Time", LuaValue.tableOf().apply {
            set("deltaTime", LuaValue.valueOf(0.016))
            set("time", LuaValue.valueOf(0.0))
            set("frameCount", LuaValue.valueOf(0))
        })

        globals.set("Camera", LuaValue.tableOf().apply {
            set("setPosition", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    engine.nativeSetCameraPosition(arg.get(1).tofloat(), arg.get(2).tofloat())
                    return LuaValue.NIL
                }
            })
            set("setZoom", object : OneArgFunction() {
                override fun call(zoom: LuaValue): LuaValue {
                    engine.nativeSetCameraZoom(zoom.tofloat())
                    return LuaValue.NIL
                }
            })
            set("shake", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    engine.nativeShakeCamera(arg.get(1).tofloat(), arg.get(2).tofloat())
                    return LuaValue.NIL
                }
            })
        })

        globals.set("Audio", LuaValue.tableOf().apply {
            set("playSFX", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    val path = arg.get(1).tojstring()
                    val volume = if (arg.get(2).isnil()) 1.0f else arg.get(2).tofloat()
                    val soundId = engine.nativeLoadSound(path)
                    engine.nativePlaySound(soundId, volume, false)
                    return LuaValue.valueOf(soundId)
                }
            })
            set("playMusic", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    val path = arg.get(1).tojstring()
                    val volume = if (arg.get(2).isnil()) 1.0f else arg.get(2).tofloat()
                    val soundId = engine.nativeLoadSound(path)
                    engine.nativePlaySound(soundId, volume, true)
                    return LuaValue.valueOf(soundId)
                }
            })
            set("setVolume", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    engine.nativeSetMasterVolume(arg.get(1).tofloat())
                    return LuaValue.NIL
                }
            })
        })

        globals.set("Physics", LuaValue.tableOf().apply {
            set("raycast", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    return LuaValue.valueOf(engine.nativeRaycast(
                        arg.get(1).tofloat(), arg.get(2).tofloat(),
                        arg.get(3).tofloat(), arg.get(4).tofloat()
                    ))
                }
            })
            set("applyForce", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    engine.nativeApplyForce(arg.get(1).tolong(), arg.get(2).tofloat(), arg.get(3).tofloat())
                    return LuaValue.NIL
                }
            })
        })

        globals.set("Particle", LuaValue.tableOf().apply {
            set("create", object : OneArgFunction() {
                override fun call(config: LuaValue): LuaValue {
                    return LuaValue.valueOf(engine.nativeCreateParticleSystem(config.tojstring()).toDouble())
                }
            })
            set("emit", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    engine.nativeEmitParticles(arg.get(1).tolong(), arg.get(2).tofloat(), arg.get(3).tofloat(), arg.get(4).toint())
                    return LuaValue.NIL
                }
            })
        })

        globals.set("Debug", LuaValue.tableOf().apply {
            set("log", object : OneArgFunction() {
                override fun call(msg: LuaValue): LuaValue {
                    Log.d("Lua", msg.tojstring())
                    return LuaValue.NIL
                }
            })
            set("drawLine", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    engine.nativeDrawLine(
                        arg.get(1).tofloat(), arg.get(2).tofloat(),
                        arg.get(3).tofloat(), arg.get(4).tofloat(),
                        arg.get(5).tofloat(), arg.get(6).tofloat(), arg.get(7).tofloat(), 1.0f
                    )
                    return LuaValue.NIL
                }
            })
            set("getFPS", object : ZeroArgFunction() {
                override fun call(): LuaValue {
                    return LuaValue.valueOf(engine.nativeGetFPS().toDouble())
                }
            })
        })
    }

    private fun bindSnippetLibrary() {
        globals.set("Movement", LuaValue.tableOf().apply {
            set("joystickControl", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    return LuaValue.NIL
                }
            })
            set("platformerController", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    return LuaValue.NIL
                }
            })
        })

        globals.set("AI", LuaValue.tableOf().apply {
            set("patrol", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    return LuaValue.NIL
                }
            })
            set("chase", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    return LuaValue.NIL
                }
            })
        })

        globals.set("Animator", LuaValue.tableOf().apply {
            set("play", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    engine.nativeSetSpriteAnimation(arg.get(1).tolong(), arg.get(2).tojstring())
                    return LuaValue.NIL
                }
            })
            set("flipX", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    return LuaValue.NIL
                }
            })
        })

        globals.set("Tween", LuaValue.tableOf().apply {
            set("to", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    return LuaValue.NIL
                }
            })
        })

        globals.set("Inventory", LuaValue.tableOf().apply {
            set("addItem", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    return LuaValue.valueOf(true)
                }
            })
            set("removeItem", object : OneArgFunction() {
                override fun call(arg: LuaValue): LuaValue {
                    return LuaValue.valueOf(true)
                }
            })
        })

        globals.set("SaveSystem", LuaValue.tableOf().apply {
            set("save", object : OneArgFunction() {
                override fun call(slot: LuaValue): LuaValue {
                    return LuaValue.valueOf(true)
                }
            })
            set("load", object : OneArgFunction() {
                override fun call(slot: LuaValue): LuaValue {
                    return LuaValue.valueOf(true)
                }
            })
        })
    }

    fun loadScript(path: String): Boolean {
        return try {
            val file = File(path)
            if (!file.exists()) return false
            val chunk = globals.load(file.readText(), path)
            loadedScripts[path] = chunk.call()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load script: $path", e)
            false
        }
    }

    fun executeScript(code: String): LuaValue? {
        return try {
            globals.load(code).call()
        } catch (e: Exception) {
            Log.e(TAG, "Script execution error", e)
            null
        }
    }

    fun callFunction(scriptPath: String, functionName: String, vararg args: Any): LuaValue? {
        return try {
            val script = loadedScripts[scriptPath] ?: return null
            val func = script.get(functionName) ?: globals.get(functionName)
            if (func.isfunction()) {
                val luaArgs = args.map { arg ->
                    when (arg) {
                        is String -> LuaValue.valueOf(arg)
                        is Number -> LuaValue.valueOf(arg.toDouble())
                        is Boolean -> LuaValue.valueOf(arg)
                        else -> LuaValue.NIL
                    }
                }.toTypedArray()
                val varargs: Varargs = LuaValue.varargsOf(luaArgs)
                func.invoke(varargs).arg1()
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Function call error: $functionName", e)
            null
        }
    }

    fun update(deltaTime: Float) {
        globals.get("Time").set("deltaTime", LuaValue.valueOf(deltaTime.toDouble()))
        globals.get("Time").set("time", LuaValue.valueOf(globals.get("Time").get("time").todouble() + deltaTime))
        globals.get("Time").set("frameCount", LuaValue.valueOf(globals.get("Time").get("frameCount").toint() + 1))
        coroutines.removeAll { !it.resume(deltaTime) }
    }

    fun shutdown() {
        loadedScripts.clear()
        coroutines.clear()
    }
}

class LuaCoroutine(private val thread: org.luaj.vm2.LuaThread) {
    fun resume(deltaTime: Float): Boolean {
        return try {
            val result = thread.resume(LuaValue.valueOf(deltaTime.toDouble()))
            result.arg1().toboolean()
        } catch (e: Exception) {
            false
        }
    }
}
