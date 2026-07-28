package com.polyglue.ide.core.engine

import android.content.Context
import android.util.Log

class NativeEngine {

    companion object {
        init { System.loadLibrary("polyglue_engine") }
        const val TAG = "PolyGlueEngine"
    }

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        val assetPath = context.applicationInfo.sourceDir
        nativeInit(assetPath)
        isInitialized = true
        Log.d(TAG, "Native engine initialized")
    }

    fun shutdown() {
        if (!isInitialized) return
        nativeShutdown()
        isInitialized = false
    }

    external fun nativeInit(assetPath: String)
    external fun nativeShutdown()
    external fun nativeRenderFrame(deltaTime: Float)
    external fun nativeResize(width: Int, height: Int)
    external fun nativeTouchEvent(action: Int, x: Float, y: Float, pointerId: Int)

    external fun nativeLoadScene(sceneJson: String)
    external fun nativeSaveScene(): String
    external fun nativeCreateEntity(name: String): Long
    external fun nativeDestroyEntity(entityId: Long)
    external fun nativeSetTransform(entityId: Long, x: Float, y: Float, rotation: Float, scaleX: Float, scaleY: Float)
    external fun nativeGetTransform(entityId: Long): FloatArray

    external fun nativeAddSpriteComponent(entityId: Long, texturePath: String, width: Float, height: Float)
    external fun nativeSetSpriteAnimation(entityId: Long, animationName: String)
    external fun nativeAddSpriteFrame(entityId: Long, frameX: Float, frameY: Float, frameW: Float, frameH: Float)

    external fun nativeAddRigidbody(entityId: Long, bodyType: Int, density: Float, friction: Float, restitution: Float)
    external fun nativeAddBoxCollider(entityId: Long, width: Float, height: Float, offsetX: Float, offsetY: Float)
    external fun nativeAddCircleCollider(entityId: Long, radius: Float, offsetX: Float, offsetY: Float)
    external fun nativeApplyForce(entityId: Long, fx: Float, fy: Float)
    external fun nativeSetVelocity(entityId: Long, vx: Float, vy: Float)
    external fun nativeRaycast(startX: Float, startY: Float, endX: Float, endY: Float): String

    external fun nativeLoadSound(path: String): Int
    external fun nativePlaySound(soundId: Int, volume: Float, loop: Boolean)
    external fun nativeStopSound(soundId: Int)
    external fun nativeSetMasterVolume(volume: Float)
    external fun nativeSetGroupVolume(group: Int, volume: Float)

    external fun nativeCreateParticleSystem(configJson: String): Long
    external fun nativeEmitParticles(systemId: Long, x: Float, y: Float, count: Int)
    external fun nativeDestroyParticleSystem(systemId: Long)

    external fun nativeCreateTilemap(width: Int, height: Int, tileSize: Float): Long
    external fun nativeSetTile(tilemapId: Long, x: Int, y: Int, tileId: Int, layer: Int)
    external fun nativeSetTileCollision(tilemapId: Long, x: Int, y: Int, solid: Boolean)
    external fun nativeDestroyTilemap(tilemapId: Long)

    external fun nativeSetCameraPosition(x: Float, y: Float)
    external fun nativeSetCameraZoom(zoom: Float)
    external fun nativeSetCameraBounds(minX: Float, minY: Float, maxX: Float, maxY: Float)
    external fun nativeShakeCamera(intensity: Float, duration: Float)

    external fun nativeAddLight(x: Float, y: Float, radius: Float, r: Float, g: Float, b: Float, intensity: Float): Long
    external fun nativeRemoveLight(lightId: Long)
    external fun nativeSetAmbientLight(r: Float, g: Float, b: Float, intensity: Float)

    external fun nativeLoadShader(vertexSrc: String, fragmentSrc: String): Int
    external fun nativeUseShader(shaderId: Int)
    external fun nativeSetShaderUniformFloat(shaderId: Int, name: String, value: Float)
    external fun nativeSetShaderUniformVec2(shaderId: Int, name: String, x: Float, y: Float)

    external fun nativeLoadScript(scriptPath: String): Boolean
    external fun nativeCallFunction(functionName: String, args: String): String
    external fun nativeSetGlobal(name: String, value: String)
    external fun nativeGetGlobal(name: String): String

    external fun nativeSetDebugDraw(enabled: Boolean)
    external fun nativeGetFPS(): Float
    external fun nativeGetMemoryUsage(): Long
    external fun nativeDrawLine(x1: Float, y1: Float, x2: Float, y2: Float, r: Float, g: Float, b: Float, a: Float)
    external fun nativeDrawRect(x: Float, y: Float, w: Float, h: Float, r: Float, g: Float, b: Float, a: Float, filled: Boolean)
    external fun nativeDrawCircle(x: Float, y: Float, radius: Float, r: Float, g: Float, b: Float, a: Float, filled: Boolean)
}
