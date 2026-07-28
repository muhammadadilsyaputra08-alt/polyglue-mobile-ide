#include <jni.h>
#include <android/log.h>
#include "engine/polyglue_engine.h"

#define LOG_TAG "PolyGlueJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace polyglue;

static Engine* g_engine = nullptr;

extern "C" {

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeInit(JNIEnv* env, jobject thiz, jstring assetPath) {
    const char* path = env->GetStringUTFChars(assetPath, nullptr);
    g_engine = new Engine();
    g_engine->initialize(path);
    env->ReleaseStringUTFChars(assetPath, path);
    LOGI("Engine initialized");
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeShutdown(JNIEnv* env, jobject thiz) {
    if (g_engine) { g_engine->shutdown(); delete g_engine; g_engine = nullptr; }
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeRenderFrame(JNIEnv* env, jobject thiz, jfloat deltaTime) {
    if (g_engine) g_engine->render(deltaTime);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeResize(JNIEnv* env, jobject thiz, jint width, jint height) {
    if (g_engine) g_engine->resize(width, height);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeTouchEvent(JNIEnv* env, jobject thiz, jint action, jfloat x, jfloat y, jint pointerId) {
    if (g_engine) g_engine->onTouch(action, x, y, pointerId);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeLoadScene(JNIEnv* env, jobject thiz, jstring sceneJson) {
    const char* json = env->GetStringUTFChars(sceneJson, nullptr);
    if (g_engine) g_engine->loadScene(json);
    env->ReleaseStringUTFChars(sceneJson, json);
}

JNIEXPORT jlong JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeCreateEntity(JNIEnv* env, jobject thiz, jstring name) {
    const char* entityName = env->GetStringUTFChars(name, nullptr);
    jlong id = g_engine ? g_engine->createEntity(entityName) : 0;
    env->ReleaseStringUTFChars(name, entityName);
    return id;
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeSetTransform(JNIEnv* env, jobject thiz, jlong entityId, jfloat x, jfloat y, jfloat rotation, jfloat scaleX, jfloat scaleY) {
    if (g_engine) g_engine->setTransform(entityId, x, y, rotation, scaleX, scaleY);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeAddSpriteComponent(JNIEnv* env, jobject thiz, jlong entityId, jstring texturePath, jfloat width, jfloat height) {
    const char* path = env->GetStringUTFChars(texturePath, nullptr);
    if (g_engine) g_engine->addSprite(entityId, path, width, height);
    env->ReleaseStringUTFChars(texturePath, path);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeSetSpriteAnimation(JNIEnv* env, jobject thiz, jlong entityId, jstring animationName) {
    const char* anim = env->GetStringUTFChars(animationName, nullptr);
    if (g_engine) g_engine->setSpriteAnimation(entityId, anim);
    env->ReleaseStringUTFChars(animationName, anim);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeAddRigidbody(JNIEnv* env, jobject thiz, jlong entityId, jint bodyType, jfloat density, jfloat friction, jfloat restitution) {
    if (g_engine) g_engine->addRigidbody(entityId, bodyType, density, friction, restitution);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeAddBoxCollider(JNIEnv* env, jobject thiz, jlong entityId, jfloat width, jfloat height, jfloat offsetX, jfloat offsetY) {
    if (g_engine) g_engine->addBoxCollider(entityId, width, height, offsetX, offsetY);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeApplyForce(JNIEnv* env, jobject thiz, jlong entityId, jfloat fx, jfloat fy) {
    if (g_engine) g_engine->applyForce(entityId, fx, fy);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeSetVelocity(JNIEnv* env, jobject thiz, jlong entityId, jfloat vx, jfloat vy) {
    if (g_engine) g_engine->setVelocity(entityId, vx, vy);
}

JNIEXPORT jstring JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeRaycast(JNIEnv* env, jobject thiz, jfloat startX, jfloat startY, jfloat endX, jfloat endY) {
    std::string result = g_engine ? g_engine->raycast(startX, startY, endX, endY) : "{}";
    return env->NewStringUTF(result.c_str());
}

JNIEXPORT jint JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeLoadSound(JNIEnv* env, jobject thiz, jstring path) {
    const char* soundPath = env->GetStringUTFChars(path, nullptr);
    jint soundId = g_engine ? g_engine->loadSound(soundPath) : -1;
    env->ReleaseStringUTFChars(path, soundPath);
    return soundId;
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativePlaySound(JNIEnv* env, jobject thiz, jint soundId, jfloat volume, jboolean loop) {
    if (g_engine) g_engine->playSound(soundId, volume, loop);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeStopSound(JNIEnv* env, jobject thiz, jint soundId) {
    if (g_engine) g_engine->stopSound(soundId);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeSetMasterVolume(JNIEnv* env, jobject thiz, jfloat volume) {
    if (g_engine) g_engine->setMasterVolume(volume);
}

JNIEXPORT jlong JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeCreateParticleSystem(JNIEnv* env, jobject thiz, jstring configJson) {
    const char* config = env->GetStringUTFChars(configJson, nullptr);
    jlong id = g_engine ? g_engine->createParticleSystem(config) : 0;
    env->ReleaseStringUTFChars(configJson, config);
    return id;
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeEmitParticles(JNIEnv* env, jobject thiz, jlong systemId, jfloat x, jfloat y, jint count) {
    if (g_engine) g_engine->emitParticles(systemId, x, y, count);
}

JNIEXPORT jlong JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeCreateTilemap(JNIEnv* env, jobject thiz, jint width, jint height, jfloat tileSize) {
    return g_engine ? g_engine->createTilemap(width, height, tileSize) : 0;
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeSetTile(JNIEnv* env, jobject thiz, jlong tilemapId, jint x, jint y, jint tileId, jint layer) {
    if (g_engine) g_engine->setTile(tilemapId, x, y, tileId, layer);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeSetCameraPosition(JNIEnv* env, jobject thiz, jfloat x, jfloat y) {
    if (g_engine) g_engine->setCameraPosition(x, y);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeSetCameraZoom(JNIEnv* env, jobject thiz, jfloat zoom) {
    if (g_engine) g_engine->setCameraZoom(zoom);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeSetCameraBounds(JNIEnv* env, jobject thiz, jfloat minX, jfloat minY, jfloat maxX, jfloat maxY) {
    if (g_engine) g_engine->setCameraBounds(minX, minY, maxX, maxY);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeShakeCamera(JNIEnv* env, jobject thiz, jfloat intensity, jfloat duration) {
    if (g_engine) g_engine->shakeCamera(intensity, duration);
}

JNIEXPORT jlong JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeAddLight(JNIEnv* env, jobject thiz, jfloat x, jfloat y, jfloat radius, jfloat r, jfloat g, jfloat b, jfloat intensity) {
    return g_engine ? g_engine->addLight(x, y, radius, r, g, b, intensity) : 0;
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeRemoveLight(JNIEnv* env, jobject thiz, jlong lightId) {
    if (g_engine) g_engine->removeLight(lightId);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeSetAmbientLight(JNIEnv* env, jobject thiz, jfloat r, jfloat g, jfloat b, jfloat intensity) {
    if (g_engine) g_engine->setAmbientLight(r, g, b, intensity);
}

JNIEXPORT jint JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeLoadShader(JNIEnv* env, jobject thiz, jstring vertexSrc, jstring fragmentSrc) {
    const char* vert = env->GetStringUTFChars(vertexSrc, nullptr);
    const char* frag = env->GetStringUTFChars(fragmentSrc, nullptr);
    jint id = g_engine ? g_engine->loadShader(vert, frag) : -1;
    env->ReleaseStringUTFChars(vertexSrc, vert);
    env->ReleaseStringUTFChars(fragmentSrc, frag);
    return id;
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeUseShader(JNIEnv* env, jobject thiz, jint shaderId) {
    if (g_engine) g_engine->useShader(shaderId);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeSetDebugDraw(JNIEnv* env, jobject thiz, jboolean enabled) {
    if (g_engine) g_engine->setDebugDraw(enabled);
}

JNIEXPORT jfloat JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeGetFPS(JNIEnv* env, jobject thiz) {
    return g_engine ? g_engine->getFPS() : 0.0f;
}

JNIEXPORT jlong JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeGetMemoryUsage(JNIEnv* env, jobject thiz) {
    return g_engine ? g_engine->getMemoryUsage() : 0;
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeDrawLine(JNIEnv* env, jobject thiz, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat r, jfloat g, jfloat b, jfloat a) {
    if (g_engine) g_engine->drawLine(x1, y1, x2, y2, r, g, b, a);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeDrawRect(JNIEnv* env, jobject thiz, jfloat x, jfloat y, jfloat w, jfloat h, jfloat r, jfloat g, jfloat b, jfloat a, jboolean filled) {
    if (g_engine) g_engine->drawRect(x, y, w, h, r, g, b, a, filled);
}

JNIEXPORT void JNICALL
Java_com_polyglue_ide_core_engine_NativeEngine_nativeDrawCircle(JNIEnv* env, jobject thiz, jfloat x, jfloat y, jfloat radius, jfloat r, jfloat g, jfloat b, jfloat a, jboolean filled) {
    if (g_engine) g_engine->drawCircle(x, y, radius, r, g, b, a, filled);
}

} // extern "C"
